package com.lemongo97.iptv.iptvmanager.quartz;

import com.lemongo97.iptv.iptvmanager.entity.EpgProgram;
import com.lemongo97.iptv.iptvmanager.entity.EpgSource;
import com.lemongo97.iptv.iptvmanager.mapper.EpgSourceMapper;
import com.lemongo97.iptv.iptvmanager.parser.EpgParserService;
import com.lemongo97.iptv.iptvmanager.service.EpgProgramService;
import com.lemongo97.iptv.iptvmanager.service.TaskProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

import java.util.List;

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

    private final EpgSourceMapper epgSourceMapper;
    private final EpgParserService epgParserService;
    private final EpgProgramService epgProgramService;
    private final TaskProgressService taskProgressService;

    @Override
    public void execute(JobExecutionContext context) {
        Long sourceId = context.getJobDetail().getJobDataMap().getLong("sourceId");
        String taskId = context.getJobDetail().getJobDataMap().getString("taskId");

        log.debug("Executing EPG refresh job for source: {}, task: {}", sourceId, taskId);

        // 获取 EPG 源信息
        EpgSource epgSource = epgSourceMapper.findById(sourceId).orElse(null);
        if (epgSource == null) {
            log.error("EPG source not found: {}", sourceId);
            if (taskId != null) {
                taskProgressService.failTask(taskId, "EPG source not found");
            }
            return;
        }

        // 开始任务
        if (taskId != null) {
            taskProgressService.startTask(taskId);
        }

        // 执行刷新
        try {
            int programCount = refreshEpgSource(epgSource, taskId);

            log.info("EPG source refreshed successfully: source={}, programs={}", sourceId, programCount);

            // 标记任务成功
            if (taskId != null) {
                taskProgressService.completeTask(taskId, "刷新完成，共 " + programCount + " 个节目");
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
    private int refreshEpgSource(EpgSource epgSource, String taskId) throws Exception {
        // 更新进度：开始获取数据
        if (taskId != null) {
            taskProgressService.updateProgress(taskId, 0, 10.0, "正在获取 EPG 数据...");
        }

        // 1. 获取 EPG 数据
        String xmlContent = fetchEpgData(epgSource.getUrl());

        // 更新进度：开始解析
        if (taskId != null) {
            taskProgressService.updateProgress(taskId, 1, 30.0, "正在解析 EPG 数据...");
        }

        // 2. 解析 EPG 数据
        List<EpgProgram> programs = epgParserService.parse(xmlContent, epgSource.getId());

        // 更新进度：开始保存
        if (taskId != null) {
            taskProgressService.updateProgress(taskId, programs.size() / 2, 60.0, "正在保存节目数据...");
        }

        // 3. 删除旧的节目数据
        epgProgramService.deleteBySourceId(epgSource.getId());

        // 更新进度：开始插入
        if (taskId != null) {
            taskProgressService.updateProgress(taskId, programs.size(), 90.0, "正在插入节目数据...");
        }

        // 4. 批量插入新的节目数据
        epgProgramService.insertBatch(programs);

        return programs.size();
    }

    /**
     * 从 URL 获取 EPG 数据
     */
    private String fetchEpgData(String url) throws Exception {
        log.info("Fetching EPG data from: {}", url);

        var request = new org.apache.hc.client5.http.classic.methods.HttpGet(url);
        try (var httpClient = org.apache.hc.client5.http.impl.classic.HttpClients.createDefault()) {
            var response = httpClient.execute(request);
            var entity = response.getEntity();
            if (entity == null) {
                throw new Exception("No content returned from EPG source");
            }

            return org.apache.hc.core5.http.io.entity.EntityUtils.toString(entity, "UTF-8");
        }
    }
}
