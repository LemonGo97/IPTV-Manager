package com.lemongo97.iptv.iptvmanager.cleanup.engine.delay;

import com.lemongo97.iptv.iptvmanager.cleanup.engine.CleaningEngine;
import com.lemongo97.iptv.iptvmanager.entity.Channel;
import com.lemongo97.iptv.iptvmanager.utils.JSONUtil;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.ffmpeg.avcodec.AVCodecParameters;
import org.bytedeco.ffmpeg.avformat.AVFormatContext;
import org.bytedeco.ffmpeg.avformat.AVIOInterruptCB;
import org.bytedeco.ffmpeg.avformat.AVStream;
import org.bytedeco.ffmpeg.avutil.AVDictionary;
import org.bytedeco.ffmpeg.global.avformat;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Pointer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.bytedeco.ffmpeg.global.avcodec.avcodec_get_name;
import static org.bytedeco.ffmpeg.global.avformat.*;
import static org.bytedeco.ffmpeg.global.avutil.*;

@Slf4j
public class FFProbeCheckEngine implements CleaningEngine {

    static {
        // 关闭 FFmpeg 默认的控制台大段日志输出，只保留错误日志
        av_log_set_level(AV_LOG_PANIC);
    }

    @Override
    public List<Channel> process(List<Channel> channels, String paramsJson) {
        FFProbeCheckEngineParam param = JSONUtil.fromJsonString(paramsJson, FFProbeCheckEngineParam.class);
        try {
            avformat.avformat_network_init();
        } catch (Throwable t) {
            log.warn("FFmpeg 网络初始化警告（可能某些版本已废弃或自动加载）: " + t.getMessage());
        }
        return this.threadDetect(channels, param);
    }

    public List<Channel> threadDetect(List<Channel> channels, FFProbeCheckEngineParam param){
        // 2. 创建一个固定大小为 20 的线程池。这能保证同时运行的线程正好是 20 个。
        ExecutorService executor = Executors.newFixedThreadPool(20);
        List<CompletableFuture<Channel>> futures = channels.stream()
                .map(channel -> {
                    // 1. 创建核心检测异步任务
                    CompletableFuture<Channel> detectTask = CompletableFuture.supplyAsync(() -> {
                        if (channel.getStatus() == Channel.Status.invalid) {
                            return channel;
                        }

                        String url = channel.getUrl();
                        log.debug("开始检测 URL: {}", url);

                        FFProbeCheckReport checkReport = this.doDetect(url, param);

                        if (checkReport == null) {
                            channel.setStatus(Channel.Status.invalid);
                            return channel;
                        }

                        channel.setFfmpegDetectDelayMilliseconds(checkReport.getDelayMilliseconds());
                        channel.setVideoInfo(checkReport.getVideo() == null ? null : JSONUtil.toJsonString(checkReport.getVideo()));
                        channel.setAudioInfo(checkReport.getAudio() == null ? null : JSONUtil.toJsonString(checkReport.getAudio()));
                        return channel;
                    }, executor);

                    // 2. 【核心杀手锏】在 Java 层面给这个任务套上一个“硬超时”
                    // 如果 doDetect 在指定的毫秒内没有把代码执行完（不管它是卡在 C 语言还是哪里），
                    // 这个 CF 任务会立刻被主线程强制终止，并抛出 TimeoutException
                    return detectTask.orTimeout(param.getDelayMillisecond() + 1000L, TimeUnit.MILLISECONDS) // 稍微比底层的超时宽限 1 秒
                            .exceptionally(throwable -> {
                                // 3. 任务超时后的兜底补救逻辑
                                log.warn("⚠️ 网址检测在 Java 层触发硬超时，强行放弃该网址: {}", channel.getUrl());
                                channel.setStatus(Channel.Status.invalid);
                                return channel; // 超时了也必须返回 channel，确保最后结果能顺利收集
                            });
                })
                .toList();

        log.debug("所有检测任务已分发，等待 20 个线程并发处理中...");

        // 3. 组合所有异步任务，并阻塞等待它们全部执行完毕
        CompletableFuture<Void> allCombinedTask = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
        );
        // 这一步会停在这里，直到 10,000 个网址全部检测完
        allCombinedTask.join();

        // 4. 按顺序统一回收所有线程的处理结果
        List<Channel> result = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        // 5. 关闭线程池，释放系统资源
        executor.shutdown();

        log.debug("所有任务检测完毕，统一返回结果");
        return result;
    }

//    @Override
    public List<Channel> processOld(List<Channel> channels, String paramsJson) {
        FFProbeCheckEngineParam param = JSONUtil.fromJsonString(paramsJson, FFProbeCheckEngineParam.class);
        List<Channel> result = new ArrayList<>();
        for (Channel channel : channels) {
            if (channel.getStatus() == Channel.Status.invalid){
                log.debug("Channel {} has status invalid, next", channel);
                result.add(channel);
                continue;
            }
            String url = channel.getUrl();
            log.debug("开始使用 ffmpeg 检测 {} 的 URL链接: {}", channel.getName(), channel.getUrl());

            FFProbeCheckReport checkReport = this.doDetect(url, param);
            if(checkReport == null) {
                channel.setStatus(Channel.Status.invalid);
                result.add(channel);
                continue;
            }
            channel.setFfmpegDetectDelayMilliseconds(checkReport.getDelayMilliseconds());
            channel.setVideoInfo(checkReport.getVideo() == null ? null : JSONUtil.toJsonString(checkReport.getVideo()));
            channel.setAudioInfo(checkReport.getAudio() == null ? null : JSONUtil.toJsonString(checkReport.getAudio()));
            result.add(channel);
        }
        return result;
    }

    public FFProbeCheckReport doDetect(String url, FFProbeCheckEngineParam param) {

        FFProbeCheckReport checkReport = new FFProbeCheckReport();

        // 1. 分配格式上下文
        AVFormatContext formatContext = avformat_alloc_context();
        if (formatContext == null) {
            return null;
        }
        // 阻止 JavaCPP 自动回收指针，防止与 finally 中的手动释放冲突导致 Double Free
        formatContext.deallocate(false);
        // 2. 强引用保障：显式创建并分配回调函数指针，防止被提前 GC
        TimeoutCallback myCallback = new TimeoutCallback(param.getDelayMillisecond());
        myCallback.deallocate(false);
        // 3. 正确初始化并配置中断回调结构体
        AVIOInterruptCB interruptCB = new AVIOInterruptCB();
        interruptCB.callback(myCallback);
        interruptCB.opaque(null);
        formatContext.interrupt_callback(interruptCB);


        AVDictionary options = new AVDictionary();
        options.deallocate(false);

        // 2. 配置软超时与低延迟参数 (单位均为微秒)
        String timeoutMicros = String.valueOf(param.getDelayMillisecond() * 1000L);
        av_dict_set(options, "timeout", timeoutMicros, 0);
        av_dict_set(options, "stimeout", timeoutMicros, 0);
        av_dict_set(options, "http_persistent", "0", 0);

        // 强行减小分析范围，加速返回
        av_dict_set(options, "fflags", "nobuffer+discardcorrupt", 0);
        av_dict_set(options, "flags", "low_delay", 0);

        // 设置探测大小 (约150KB) 与最大分析时间 (1秒)
        formatContext.probesize(1024000);
        formatContext.max_analyze_duration(3000000);

        long startTick = System.currentTimeMillis();

        try {
            // 3. 步骤一：打开网络输入流（这一步会自动递归追踪多层 m3u8 并完成握手）
            int ret = avformat_open_input(formatContext, url, null, options);
            // 之后的读取流信息、解析音视频参数（最耗时的步骤）依然完全保持 20 个线程的并发
            if (ret < 0) {
                byte[] errBuffer = new byte[1024];
                av_strerror(ret, errBuffer, errBuffer.length);
                throw new RuntimeException("Open input failed: " + new String(errBuffer).trim());
            }

            // 4. 步骤二：尝试读取音视频流的内部编码信息
            // 此时底层会去下载第一个 .ts 切片的头部数据进行解析
            ret = avformat_find_stream_info(formatContext, (AVDictionary) null);
            long endTick = System.currentTimeMillis();

            // 计算综合检测延迟 (握手 + 嵌套解析 + 首片头部加载)
            checkReport.setDelayMilliseconds(endTick - startTick);

            if (ret < 0) {
                throw new RuntimeException("Find stream info failed.");
            }

            // 6. 步骤四：遍历解析出来的流，提取音视频参数
            int nbStreams = formatContext.nb_streams();
            for (int i = 0; i < nbStreams; i++) {
                AVStream stream = formatContext.streams(i);
                AVCodecParameters codecPar = stream.codecpar();

                String codec;
                try(BytePointer bytePointer = avcodec_get_name(codecPar.codec_id())){
                    codec = bytePointer.getString();
                }
                if (codecPar.codec_type() == AVMEDIA_TYPE_VIDEO) {
                    checkReport.setVideo(new FFProbeCheckReport.VideoInfo().setCodec(codec).setWidth(codecPar.width()).setHeight(codecPar.height()));
                } else if (codecPar.codec_type() == AVMEDIA_TYPE_AUDIO) {
                    checkReport.setAudio(new FFProbeCheckReport.AudioInfo().setCodec(codec).setRate(codecPar.sample_rate()));
                }
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            checkReport = null;
        } finally {
            // 7. 严格释放底层 C 语言的指针资源，防止 Java 进程发生内存泄漏
            if (options != null) {
                av_dict_free(options);
                options.deallocate(true); // 释放字典自身指针
            }
            if (formatContext != null) {
                // close 内部会断开网络，此时会最后一次触发 interrupt_callback
                avformat_close_input(formatContext);
                avformat_free_context(formatContext);
                formatContext.deallocate(true);
            }
            // 必须在 context 销毁后，再安全抹去回调函数的内存
            if (interruptCB != null) {
                interruptCB.deallocate(true);
            }
            if (myCallback != null) {
                myCallback.deallocate(true);
            }
        }
        // 👈 核心安全锁：强行在方法最末尾引用一下这两个对象
        // 确保在 avformat_open_input 运行期间，这两个回调指针在 JVM 堆里绝对活着
        java.util.Objects.requireNonNull(myCallback);
        java.util.Objects.requireNonNull(interruptCB);

        return checkReport;
    }

    @Data
    public static class FFProbeCheckEngineParam {

        /**
         * 延迟时间，高于此延迟时间的将被过滤(默认10秒)
         */
        private Long delayMillisecond = 10000L;

        /**
         * 丢弃无视频
         */
        private Boolean discardNoVideo;

        /**
         * 丢弃无音频
         */
        private Boolean discardNoAudio;

        /**
         * 最小视频帧宽
         */
        private Integer minVideoFrameWidth;

        /**
         * 最小视频帧高
         */
        private Integer minVideoFrameHeight;
    }

    @Data
    @Accessors(chain = true)
    public static class FFProbeCheckReport{

        private Long delayMilliseconds;
        private VideoInfo video;
        private AudioInfo audio;

        @Data
        @Accessors(chain = true)
        public static class VideoInfo {
            private Integer width;
            private Integer height;
            private String codec;
        }

        @Data
        @Accessors(chain = true)
        public static class AudioInfo {
            private Integer rate;
            private String codec;
        }
    }

    @Slf4j
    public static class TimeoutCallback extends AVIOInterruptCB.Callback_Pointer {
        private final long timeoutTime;

        public TimeoutCallback(long timeoutMs) {
            this.timeoutTime = System.currentTimeMillis() + timeoutMs;
        }

        // FFmpeg 在执行所有阻塞式网络/解码操作时，每隔几毫秒就会疯狂调用这个方法
        // 如果这个方法返回 1，FFmpeg 就会立刻放弃抵抗，彻底中断并退出，绝对不会卡死
        @Override
        public int call(Pointer arg0) {
            if (System.currentTimeMillis() > timeoutTime) {
                log.debug("ffmpeg 底层网络或解码超时，强制中断！");
                return 1; // 返回 1 表示立即中断所有阻塞的 FFmpeg 操作
            }
            return 0; // 返回 0 表示正常，继续等待
        }
    }
}
