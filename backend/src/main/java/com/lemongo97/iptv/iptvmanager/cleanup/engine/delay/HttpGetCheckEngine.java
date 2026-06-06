package com.lemongo97.iptv.iptvmanager.cleanup.engine.delay;

import com.lemongo97.iptv.iptvmanager.cleanup.engine.CleaningEngine;
import com.lemongo97.iptv.iptvmanager.entity.Channel;
import com.lemongo97.iptv.iptvmanager.okhttp.exception.OkHttpRequestException;
import com.lemongo97.iptv.iptvmanager.utils.JSONUtil;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSource;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
public class HttpGetCheckEngine implements CleaningEngine {

    private static final OkHttpClient okHttpClient =
            new OkHttpClient.Builder()
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(false)
                    .build();

    @Override
    public Channel process(Channel channel, String paramsJson) {
        log.debug("Channel cleanup: Processing HttpGetCheckEngine");

        HttpGetCheckEngineParam param = JSONUtil.fromJsonString(paramsJson, HttpGetCheckEngineParam.class);

        if (channel.getStatus() == Channel.Status.invalid) {
            log.debug("Channel {} has status invalid, next", channel);
            return channel;
        }

        log.debug("开始使用 HTTP GET 检测 {} 的 URL链接: {}", channel.getName(), channel.getUrl());
        try (Response response = okHttpClient
                .newCall(new Request.Builder().get().header("User-Agent", "AptvPlayer/1.4.25").url(channel.getUrl()).build()).execute()) {
            if (!response.isSuccessful()) {
                throw new OkHttpRequestException("HTTP request failed with code " + response.code());
            }

            long startTime = response.sentRequestAtMillis();
            long endTime = response.receivedResponseAtMillis();
            long duration = endTime - startTime;


            log.debug("请求耗时: {} 毫秒", duration);
            channel.setHttpDetectDelayMilliseconds(duration);

            String contentType = response.header("Content-Type");
            if (Strings.CI.containsAny(contentType, "vnd.apple.mpegurl", "x-mpegurl", "audio/mpegurl")) {
                // m3u8 HLS单播
                log.debug("收到 m3u8 HLS 单播响应，Content-Type: {}", contentType);
                String content = response.body().string();
                this.checkM3UContentValid(content);
                SegmentMetrics metric = this.checkHlsMetrics(channel.getUrl(), content);
                log.debug("检测结果：{}", metric);
                channel.setScore(metric.getScore());
                channel.setHttpDetectDelayMilliseconds(metric.getTotalCost());

            } else if (Strings.CI.containsAny(contentType, "video/x-flv", "video/mp2t", "application/octet-stream", "video/mp4", "video/mpeg")) {
                // 原生流式单播 / 组播转单播
                log.debug("收到流式单播/组播转单播响应，Content-Type: {}", contentType);
                StreamMetrics metric = this.checkStreamMetric(response);
                log.debug("检测结果：{}", metric);
                channel.setScore(metric.getScore());
            } else {
                //TODO application/dash+xml
                log.debug("未知的响应，Content-Type: {}", contentType);
            }

        } catch (Exception e) {
            channel.setStatus(Channel.Status.invalid);
            log.warn("检测失败，Channel: {}, 错误信息: {}", channel.getName(), e.getMessage());
        }
        return channel;
    }

    private StreamMetrics checkStreamMetric(Response response) {
        // 默认 30 秒检测时间
        int detectSeconds = 30;
        StreamMetrics metrics = new StreamMetrics();
        long ttfb =
                response.receivedResponseAtMillis()
                        - response.sentRequestAtMillis();

        metrics.setTtfb(ttfb);

        BufferedSource source = response.body().source();

        byte[] buffer = new byte[8192];

        long startTime = System.currentTimeMillis();
        long endTime = startTime + detectSeconds * 1000;

        long totalBytes = 0;
        long secondBytes = 0;

        long secondStart = System.currentTimeMillis();

        List<Long> speedList = new ArrayList<>();

        long lastDataTime = System.currentTimeMillis();

        int stallCount = 0;
        long maxStallMs = 0;

        while (System.currentTimeMillis() < endTime) {

            int len;

            try {
                source.timeout().timeout(500, TimeUnit.MILLISECONDS);
                len = source.read(
                        buffer,
                        0,
                        buffer.length);
            } catch (IOException e) {
                long stall =
                        System.currentTimeMillis()
                                - lastDataTime;
                stallCount++;
                maxStallMs =
                        Math.max(maxStallMs, stall);
                continue;
            }

            if (len < 0) {
                break;
            }

            long now =
                    System.currentTimeMillis();

            totalBytes += len;
            secondBytes += len;
            lastDataTime = now;

            if (now - secondStart >= 1000) {
                speedList.add(secondBytes);
                secondBytes = 0;
                secondStart = now;
            }
        }

        metrics.setDurationMs(
                        System.currentTimeMillis()
                                - startTime)
                .setTotalBytes(totalBytes)
                .setStallCount(stallCount)
                .setMaxStallMs(maxStallMs);

        calculateSpeed(metrics,
                speedList);

        return metrics;
    }

    private void calculateSpeed(StreamMetrics metrics, List<Long> speedList) {

        if (speedList.isEmpty()) {
            return;
        }

        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;

        long sum = 0;

        for (Long speed : speedList) {
            min = Math.min(min, speed);
            max = Math.max(max, speed);
            sum += speed;
        }

        metrics.setMinSpeed(min)
                .setMaxSpeed(max)
                .setAvgSpeed(
                        sum * 1.0 / speedList.size());
    }

    private List<String> fetchHlsRealPlayList(String url, String content) {

        if (!Strings.CI.contains(content, "#EXT-X-STREAM-INF")) {
            return List.of(url, content);
        }

        log.debug("此列表为嵌套播放列表，准备获取真实播放列表");

        String[] lines = content.split("\\r?\\n");

        boolean isInnerPlaylist = false;
        List<String> urls = new ArrayList<>();
        for (String line : lines) {
            if (StringUtils.isBlank(line)) continue;
            line = line.trim();
            if (line.startsWith("#EXT-X-STREAM-INF")) {
                // 说明是嵌套的playlist
                isInnerPlaylist = true;
            }
            if (!line.startsWith("#")) {
                String playlistUri;
                if (line.startsWith("http")) {
                    playlistUri = line;
                } else {
                    playlistUri = this.resolveUri(url, line);
                }
                urls.add(playlistUri);
            }
        }

        if (!isInnerPlaylist) {
            return List.of(url, content);
        }

        String innerPlaylistUrl = urls.getFirst();

        try (Response response = okHttpClient.newCall(
                new Request.Builder().get().url(innerPlaylistUrl).build()).execute()) {
            if (!response.isSuccessful()) throw new OkHttpRequestException("failed to fetch playlist");
            String contentType = response.header("Content-Type");
            if (!Strings.CI.containsAny(contentType, "vnd.apple.mpegurl", "x-mpegurl"))
                throw new OkHttpRequestException("content type not supported");

            return fetchHlsRealPlayList(innerPlaylistUrl, response.body().string());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    private SegmentMetrics checkHlsMetrics(String url, String content) {

        List<String> realPlayList = this.fetchHlsRealPlayList(url, content);
        String readUrl = realPlayList.get(0);
        String readContent = realPlayList.get(1);

        String[] lines = readContent.split("\\r?\\n");

        LinkedHashMap<String, Double> urls = new LinkedHashMap<>();

        int urlCount = 0;

        Double duration = null;
        for (String line : lines) {
            if (StringUtils.isBlank(line)) continue;
            line = line.trim();
            if (line.startsWith("#EXTINF:")) {
                String durationStr = StringUtils.substringBetween(line, "#EXTINF:", ",");
                duration = Double.parseDouble(durationStr);
            }
            if (!line.startsWith("#")) {
                if (line.startsWith("http")) {
                    urls.put(line, duration);
                } else {
                    String uri = this.resolveUri(readUrl, line);
                    urls.put(uri, duration);
                }
                urlCount++;
            }
        }

        if (urls.size() != urlCount) {
            // 有重复分片
            throw new RuntimeException("has repeated urls: " + urlCount + " != " + urls.size());
        }

        // 收集指标并评分
        int size = Math.min(urls.size(), 5);
        List<String> testUrls = new ArrayList<>(urls.sequencedKeySet());
        int startIndex = testUrls.size() - size;

        List<SegmentMetrics> metrics = new ArrayList<>();
        for (int i = startIndex; i < testUrls.size(); i++) {
            String testUrl = testUrls.get(i);
            SegmentMetrics metric = this.testDownloadSegment(testUrl, urls.get(testUrl));
            metrics.add(metric);
        }

        long count1 = metrics.stream().map(SegmentMetrics::getDigest).distinct().count();
        if (count1 != metrics.size()) {
            throw new RuntimeException("has repeated segment!");
        }

        // 去除极值并评分
        DoubleSummaryStatistics summaryStatistics = metrics.stream().mapToDouble(SegmentMetrics::getPlaybackRatio).summaryStatistics();
        double score;
        if (metrics.size() < 4) {
            double avgRatio = summaryStatistics.getAverage();
            score = this.score(avgRatio);
        } else {
            double min = summaryStatistics.getMin();
            double max = summaryStatistics.getMax();
            long count = summaryStatistics.getCount();
            double avgRatio = (summaryStatistics.getSum() - min - max) / (count - 2);
            score = this.score(avgRatio);
        }

        SegmentMetrics result = new SegmentMetrics();
        for (SegmentMetrics metric : metrics) {
            if (result.getTotalCost() == null) {
                result.setTotalCost(metric.getTotalCost());
            } else {
                result.setTotalCost(result.getTotalCost() + metric.getTotalCost());
            }
            if (result.getTtfb() == null) {
                result.setTtfb(metric.getTtfb());
            } else {
                result.setTtfb(result.getTtfb() + metric.getTtfb());
            }
            if (result.getDownloadCost() == null) {
                result.setDownloadCost(metric.getDownloadCost());
            } else {
                result.setDownloadCost(result.getDownloadCost() + metric.getDownloadCost());
            }
            if (result.getBytes() == null) {
                result.setBytes(metric.getBytes());
            } else {
                result.setBytes(result.getBytes() + metric.getBytes());
            }
            if (result.getSegmentDuration() == null) {
                result.setSegmentDuration(metric.getSegmentDuration());
            } else {
                result.setSegmentDuration(result.getSegmentDuration() + metric.getSegmentDuration());
            }
            if (result.getSpeed() == null) {
                result.setSpeed(metric.getSpeed());
            } else {
                result.setSpeed(result.getSpeed() + metric.getSpeed());
            }
            if (result.getPlaybackRatio() == null) {
                result.setPlaybackRatio(metric.getPlaybackRatio());
            } else {
                result.setPlaybackRatio(result.getPlaybackRatio() + metric.getPlaybackRatio());
            }
        }

        result.setTtfb(result.getTtfb() / metrics.size())
                .setDownloadCost(result.getDownloadCost() / metrics.size())
                .setTotalCost(result.getTotalCost() / metrics.size())
                .setBytes(result.getBytes() / metrics.size())
                .setSegmentDuration(result.getSegmentDuration() / metrics.size())
                .setSpeed(result.getSpeed() / metrics.size())
                .setPlaybackRatio(result.getPlaybackRatio() / metrics.size())
                .setScore(score);

        return result;
    }

    public int score(double avgRatio) {
        if (avgRatio >= 10) return 100;
        if (avgRatio >= 8) return 95;
        if (avgRatio >= 6) return 90;
        if (avgRatio >= 4) return 80;
        if (avgRatio >= 3) return 70;
        if (avgRatio >= 2) return 60;
        if (avgRatio >= 1) return 40;
        return (int) (avgRatio * 40);
    }

    private SegmentMetrics testDownloadSegment(String url, Double segmentDuration) {
        try (Response response = okHttpClient.newCall(new Request.Builder().get().url(url).build()).execute()) {
            if (!response.isSuccessful())
                throw new OkHttpRequestException("HTTP request failed with code " + response.code());

            byte[] bytes = response.body().bytes();

            // 计算首包头响应延迟
            long now = System.currentTimeMillis();

            SegmentMetrics segmentMetrics = new SegmentMetrics()
                    .setTtfb(response.receivedResponseAtMillis() - response.sentRequestAtMillis())
                    .setDownloadCost(now - response.receivedResponseAtMillis())
                    .setTotalCost(now - response.sentRequestAtMillis())
                    .setBytes(response.body().contentLength())
                    .setSegmentDuration(segmentDuration);

            segmentMetrics.setSpeed((double) ((segmentMetrics.getBytes() * 1000) / segmentMetrics.getDownloadCost()));
            segmentMetrics.setPlaybackRatio(
                    segmentDuration * 1000
                            / segmentMetrics.getTotalCost());
            segmentMetrics.setDigest(DigestUtils.md5Hex(bytes));
            return segmentMetrics;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Data
    @Accessors(chain = true)
    public static class StreamMetrics {

        /**
         * 首包延迟
         */
        private Long ttfb;

        /**
         * 检测时长
         */
        private Long durationMs;

        /**
         * 总接收字节数
         */
        private Long totalBytes;

        /**
         * 平均速率(B/s)
         */
        private Double avgSpeed;

        /**
         * 最小速率(B/s)
         */
        private Long minSpeed;

        /**
         * 最大速率(B/s)
         */
        private Long maxSpeed;

        /**
         * 卡顿次数
         */
        private Integer stallCount;

        /**
         * 最大卡顿时间(ms)
         */
        private Long maxStallMs;

        // getter/setter
        private double stallScore() {

            if (this.getStallCount() == 0) {
                return 100;
            }

            double score =
                    100 - this.getStallCount() * 20;

            if (this.getMaxStallMs() > 5000) {
                score -= 20;
            }

            if (this.getMaxStallMs() > 10000) {
                score -= 30;
            }

            return Math.max(0, score);
        }

        private double ttfbScore() {

            if (ttfb <= 100) return 100;

            if (ttfb <= 300) return 90;

            if (ttfb <= 500) return 80;

            if (ttfb <= 1000) return 60;

            if (ttfb <= 2000) return 40;

            return 20;
        }

        private double stabilityScore() {

            double ratio =
                    minSpeed / avgSpeed;

            if (ratio >= 0.9) {
                return 100;
            }

            if (ratio >= 0.8) {
                return 90;
            }

            if (ratio >= 0.6) {
                return 70;
            }

            if (ratio >= 0.4) {
                return 50;
            }

            if (ratio >= 0.2) {
                return 30;
            }

            return 10;
        }

        public double getScore() {

            double stall =
                    stallScore();

            double ttfb =
                    ttfbScore();

            double stability =
                    stabilityScore();

            double score =
                    stall * 0.6
                            + ttfb * 0.2
                            + stability * 0.2;
            return Math.round(score);
        }

    }

    @Data
    @Accessors(chain = true)
    private static class SegmentMetrics {
        // 首包延迟
        private Long ttfb;

        // 下载耗时
        private Long downloadCost;

        // 总耗时
        private Long totalCost;

        // TS大小
        private Long bytes;

        // 下载速度(B/s)
        private Double speed;

        // TS时长(EXTINF)
        private Double segmentDuration;

        // 安全系数
        private Double playbackRatio;

        // 计算摘要hash
        private String digest;

        private Double score;

    }

    /**
     * 计算完整路径
     */
    private String resolveUri(String baseUrl, String relativeUrl) {
        try {
            URI baseUri = new URI(baseUrl);
            URI relativeUri = new URI(relativeUrl);
            URI resolvedUri = baseUri.resolve(relativeUri);
            return resolvedUri.toString();
        } catch (Exception e) {
            return relativeUrl;
        }
    }

    public void checkM3UContentValid(String content) {
        String[] lines = content.split("\\r?\\n");
        for (String line : lines) {
            if (StringUtils.isBlank(line) || line.startsWith("#")) {
                continue;
            }
            if (Strings.CI.contains(line, "no_signal")) {
                throw new RuntimeException("Invalid m3u url: " + line);
            }
        }
    }


    public enum Type {
        GET,
        HEAD
    }


    @Data
    public static class HttpGetCheckEngineParam {
        /**
         * 同时检测个数
         */
        private Integer parallelNum;
    }
}
