package com.lemongo97.iptv.iptvmanager.quartz.job;

import com.lemongo97.iptv.iptvmanager.entity.EpgChannel;
import com.lemongo97.iptv.iptvmanager.entity.EpgProgram;
import com.lemongo97.iptv.iptvmanager.entity.EpgProvider;
import com.lemongo97.iptv.iptvmanager.mapper.EpgChannelMapper;
import com.lemongo97.iptv.iptvmanager.mapper.EpgProgramMapper;
import com.lemongo97.iptv.iptvmanager.mapper.EpgProviderMapper;
import com.lemongo97.iptv.iptvmanager.okhttp.exception.OkHttpRequestException;
import com.lemongo97.iptv.iptvmanager.parser.epg.EPGParser;
import com.lemongo97.iptv.iptvmanager.parser.epg.entity.EPGChannel;
import com.lemongo97.iptv.iptvmanager.parser.epg.entity.EPGProgramme;
import com.lemongo97.iptv.iptvmanager.parser.epg.handler.EPGChannelHandler;
import com.lemongo97.iptv.iptvmanager.parser.epg.handler.EPGProgrammeHandler;
import com.lemongo97.iptv.iptvmanager.service.TaskProgressService;
import com.lemongo97.iptv.iptvmanager.utils.OkHttpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * EPG 刷新定时任务
 * 使用 Quartz 框架异步刷新 EPG 源
 * 支持任务进度跟踪和状态管理
 */
@Slf4j
@Component
@DisallowConcurrentExecution
@RequiredArgsConstructor
public class EpgRefreshJob implements Job {

    private final EpgProviderMapper epgProviderMapper;
    private final EpgChannelMapper epgChannelMapper;
    private final EpgProgramMapper epgProgramMapper;
    private final TaskProgressService taskProgressService;

    @Override
    public void execute(JobExecutionContext context) {
        Long sourceId = context.getJobDetail().getJobDataMap().getLong("sourceId");
        String taskId = context.getJobDetail().getJobDataMap().getString("taskId");

        log.debug("Executing EPG refresh job for source: {}, task: {}", sourceId, taskId);

        // 获取 EPG 源信息
        EpgProvider epgProvider = epgProviderMapper.findById(sourceId).orElse(null);
        if (epgProvider == null) {
            log.error("EPG source not found: {}", sourceId);
            if (taskId != null) {
                taskProgressService.failTask(taskId, "EPG source not found");
            }
            return;
        }

        if (epgProvider.getType() == null) {
            taskProgressService.failTask(taskId, "EPG source type not found");
            return;
        }

        // 开始任务
        if (taskId != null) {
            taskProgressService.startTask(taskId);
        }

        // 执行刷新
        try {
            refreshEpgSource(epgProvider, taskId);

            log.info("EPG source refreshed successfully");

            // 标记任务成功
            if (taskId != null) {
                taskProgressService.completeTask(taskId, "刷新完成");
            }

        } catch (Exception e) {
            log.error("EPG refresh job failed for source: {}", sourceId, e);

            // 标记任务失败
            if (taskId != null) {
                taskProgressService.failTask(taskId, "刷新失败: " + e.getMessage());
            }
        }
    }

    /**
     * 刷新 EPG 源
     */
    private void refreshEpgSource(EpgProvider epgProvider, String taskId) throws Exception {
        // 更新进度：开始获取数据
        if (taskId != null) {
            taskProgressService.updateProgress(taskId, 0, 10.0, "正在获取 EPG 数据...");
        }

        EPGChannelHandler epgChannelHandler = new SimpleEPGChannelHandler(epgProvider, epgChannelMapper);
        EPGProgrammeHandler epgProgrammeHandler = new SimpleEPGProgrammeHandler(epgProvider, epgProgramMapper);

        String url = epgProvider.getUrl();
        log.info("Fetching EPG data from: {}", url);
        OkHttpClient client = OkHttpUtil.getClient();
        try (Response response = client.newCall(new Request.Builder().get().url(url).build()).execute()) {
            if (!response.isSuccessful())
                throw new OkHttpRequestException("request failed with code " + response.code());

            InputStream responseInputStream = response.body().byteStream();
            InputStream inputStream;
            if (epgProvider.getType() == EpgProvider.Type.XMLTV_GZIP) {
                inputStream = new GzipCompressorInputStream(responseInputStream);
            } else if (epgProvider.getType() == EpgProvider.Type.XMLTV) {
                inputStream = responseInputStream;
            } else {
                throw new RuntimeException("unknown EPG provider type: " + epgProvider.getType());
            }
            epgChannelMapper.deleteBySourceId(epgProvider.getId());
            epgProgramMapper.deleteBySourceId(epgProvider.getId());
            EPGParser.parse(inputStream, StandardCharsets.UTF_8, epgChannelHandler, epgProgrammeHandler, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private record SimpleEPGChannelHandler(EpgProvider epgProvider,
                                           EpgChannelMapper epgChannelMapper) implements EPGChannelHandler {
        @Override
        public void handle(EPGChannel channel) {
            EpgChannel epgChannel = new EpgChannel()
                    .setChannelId(channel.getId())
                    .setDeleted(false)
                    .setEpgSourceId(epgProvider.getId())
                    .setIcon(channel.getIcon() == null ? null : new EpgChannel.Icon()
                                                                .setSrc(channel.getIcon().getSrc())
                                                                .setWidth(channel.getIcon().getWidth())
                                                                .setHeight(channel.getIcon().getHeight()))
                    .setDisplayName(channel.getDisplayName() == null ? null : channel.getDisplayName().stream().map(n ->
                            new EpgChannel.DisplayName()
                            .setLang(n.getLang())
                            .setValue(n.getValue())).toList());
            epgChannelMapper.insert(epgChannel);
        }
    }

    private record SimpleEPGProgrammeHandler(EpgProvider epgProvider,
                                             EpgProgramMapper epgProgramMapper) implements EPGProgrammeHandler {
        @Override
        public void handle(EPGProgramme programme) {
            EpgProgram epgProgram = new EpgProgram()
                    .setEpgSourceId(epgProvider.getId())
                    .setChannelId(programme.getChannel())
                    .setStartTime(programme.getStart())
                    .setStopTime(programme.getStop())
                    .setTitle(programme.getTitle() == null ? null : new EpgProgram.Title()
                                                                    .setLang(programme.getTitle().getLang())
                                                                    .setValue(programme.getTitle().getValue()))
                    .setDescription(programme.getDesc() == null ? null : programme.getDesc().stream().map(n -> new EpgProgram.Desc()
                                                                                                               .setLang(n.getLang())
                                                                                                               .setValue(n.getValue())).toList());
            epgProgramMapper.insert(epgProgram);
        }
    }
}
