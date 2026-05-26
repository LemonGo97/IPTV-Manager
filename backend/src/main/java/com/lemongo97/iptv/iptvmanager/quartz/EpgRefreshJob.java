package com.lemongo97.iptv.iptvmanager.quartz;

import com.lemongo97.iptv.iptvmanager.entity.EpgChannel;
import com.lemongo97.iptv.iptvmanager.entity.EpgProgram;
import com.lemongo97.iptv.iptvmanager.entity.EpgProvider;
import com.lemongo97.iptv.iptvmanager.mapper.EpgChannelMapper;
import com.lemongo97.iptv.iptvmanager.mapper.EpgProgramMapper;
import com.lemongo97.iptv.iptvmanager.mapper.EpgProviderMapper;
import com.lemongo97.iptv.iptvmanager.parser.epg.EPGParser;
import com.lemongo97.iptv.iptvmanager.parser.epg.handler.EPGChannelHandler;
import com.lemongo97.iptv.iptvmanager.parser.epg.handler.EPGProgrammeHandler;
import com.lemongo97.iptv.iptvmanager.service.TaskProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

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
    private final RestTemplate restTemplate;

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

        String url = epgProvider.getUrl();
        log.info("Fetching EPG data from: {}", url);
        restTemplate.execute(
                url,
                HttpMethod.GET,
                null,
                (response) -> {
                    InputStream inputStream;
                    if (epgProvider.getType() == EpgProvider.Type.XMLTV_GZIP) {
                        inputStream = new GzipCompressorInputStream(response.getBody());
                    } else if (epgProvider.getType() == EpgProvider.Type.XMLTV) {
                        inputStream = response.getBody();
                    } else {
                        return null;
                    }

                    EPGParser.parse(inputStream, StandardCharsets.UTF_8,
                            (EPGChannelHandler) channel -> {
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
                            },
                            (EPGProgrammeHandler) programme -> {
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

                    }, null);
                    return null;
                }
        );

//        var request = new HttpGet(url);
//        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
//
//            var response = httpClient.execute(request);
//            var entity = response.getEntity();
//            if (entity == null) {
//                throw new Exception("No content returned from EPG source");
//            }
//
//            return org.apache.hc.core5.http.io.entity.EntityUtils.toString(entity, "UTF-8");
//        }
    }
}
