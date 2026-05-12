package com.lemongo97.iptv.iptvmanager.quartz;

import com.lemongo97.iptv.iptvmanager.entity.M3U8Provider;
import com.lemongo97.iptv.iptvmanager.entity.M3U8RefreshTask;
import com.lemongo97.iptv.iptvmanager.mapper.M3U8ProviderMapper;
import com.lemongo97.iptv.iptvmanager.mapper.M3U8RefreshTaskMapper;
import com.lemongo97.iptv.iptvmanager.parser.M3U8ParserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

/**
 * M3U8 刷新定时任务
 * 使用 Quartz 框架定期刷新 M3U8 提供者
 * 支持记录任务执行状态和结果到数据库
 */
@Slf4j
@Component
@DisallowConcurrentExecution
@RequiredArgsConstructor
public class M3U8RefreshJob implements Job {

    private final M3U8ProviderMapper providerMapper;
    private final M3U8ParserService parserService;
    private final M3U8RefreshTaskMapper taskMapper;

    @Override
    public void execute(JobExecutionContext context) {
        Long providerId = context.getJobDetail().getJobDataMap().getLong("providerId");
        Long taskId = getLongFromJobData(context.getJobDetail().getJobDataMap(), "taskId");
        String triggerType = context.getJobDetail().getJobDataMap().getString("triggerType");

        log.debug("Executing M3U8 refresh job for provider: {}, task: {}, trigger: {}", providerId, taskId, triggerType);

        // 获取 provider 信息
        M3U8Provider provider = providerMapper.findById(providerId).orElse(null);
        if (provider == null) {
            log.error("Provider not found: {}", providerId);
            if (taskId != null && taskId > 0) {
                updateTaskAsFailed(taskId, null, "Provider not found");
            }
            return;
        }

        // 记录开始时间
        long startTime = System.currentTimeMillis();
        // 任务记录已在 M3U8ProviderService 中创建，直接使用传入的 taskId
        Long actualTaskId = taskId;

        // 执行解析
        try {
            int channelCount = parserService.parse(provider, actualTaskId);

            long endTime = System.currentTimeMillis();
            log.info("M3U8 provider refreshed successfully: provider={}, channels={}, duration={}ms",
                providerId, channelCount, endTime - startTime);

            // 更新任务记录为成功
            if (actualTaskId != null && actualTaskId > 0) {
                updateTaskAsSuccess(actualTaskId, endTime - startTime, channelCount);
            }

        } catch (Exception e) {
            log.error("M3U8 refresh job failed for provider: {}", providerId, e);

            // 更新任务记录为失败
            if (actualTaskId != null && actualTaskId > 0) {
                updateTaskAsFailed(actualTaskId, startTime, e.getMessage());
            }
        }
    }

    /**
     * 创建任务记录
     */
    private Long createTaskRecord(Long taskId, Long providerId, String providerName, String triggerType, long startTime) {
        try {
            var task = new M3U8RefreshTask(
                taskId,
                providerId,
                null, // providerName 通过关联查询获取
                triggerType != null ? triggerType : "scheduled",
                "running",
                startTime,
                null,
                null,
                null,
                null,
                null,
                null,
                null
            );
            taskMapper.insert(task);
            return taskId;
        } catch (Exception e) {
            log.warn("Failed to create task record: {}", taskId, e);
            return null;
        }
    }

    /**
     * 更新任务为成功状态
     */
    private void updateTaskAsSuccess(Long taskId, long duration, int channelCount) {
        try {
            var existing = taskMapper.findById(taskId);
            if (existing != null) {
                var updated = new M3U8RefreshTask(
                    taskId,
                    existing.getProviderId(),
                    null, // providerName 通过关联查询获取
                    existing.getTriggerType(),
                    "success",
                    existing.getStartTime(),
                    System.currentTimeMillis(),
                    duration,
                    channelCount,
                    null,
                    existing.getRawContent(),
                    existing.getCreatedAt(),
                    java.time.LocalDateTime.now()
                );
                taskMapper.update(updated);
            }
        } catch (Exception e) {
            log.warn("Failed to update task as success: {}", taskId, e);
        }
    }

    /**
     * 更新任务为失败状态
     */
    private void updateTaskAsFailed(Long taskId, Long startTime, String errorMessage) {
        try {
            var existing = taskMapper.findById(taskId);
            long actualStartTime = startTime != null ? startTime : (existing != null ? existing.getStartTime() : System.currentTimeMillis());

            String truncatedError = errorMessage != null && errorMessage.length() > 1000
                ? errorMessage.substring(0, 1000)
                : errorMessage;

            if (existing == null) {
                // 任务不存在，创建失败记录
                var newTask = new M3U8RefreshTask(
                    taskId,
                    null,
                    null,
                    "manual",
                    "failed",
                    actualStartTime,
                    System.currentTimeMillis(),
                    System.currentTimeMillis() - actualStartTime,
                    0,
                    truncatedError,
                    null,
                    java.time.LocalDateTime.now(),
                    java.time.LocalDateTime.now()
                );
                taskMapper.insert(newTask);
            } else {
                // 更新现有任务为失败
                var updated = new M3U8RefreshTask(
                    taskId,
                    existing.getProviderId(),
                    null, // providerName 通过关联查询获取
                    existing.getTriggerType(),
                    "failed",
                    existing.getStartTime(),
                    System.currentTimeMillis(),
                    System.currentTimeMillis() - actualStartTime,
                    0,
                    truncatedError,
                    existing.getRawContent(),
                    existing.getCreatedAt(),
                    java.time.LocalDateTime.now()
                );
                taskMapper.update(updated);
            }
        } catch (Exception e) {
            log.warn("Failed to update task as failed: {}", taskId, e);
        }
    }

    /**
     * 安全地从 JobDataMap 获取 Long 值
     */
    private Long getLongFromJobData(JobDataMap jobDataMap, String key) {
        Object value = jobDataMap.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
