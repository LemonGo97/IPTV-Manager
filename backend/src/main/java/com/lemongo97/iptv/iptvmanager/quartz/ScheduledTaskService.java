package com.lemongo97.iptv.iptvmanager.quartz;

import com.lemongo97.iptv.iptvmanager.entity.M3U8Provider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * 定时任务服务
 * 管理 Quartz 定时任务的创建、更新、删除、暂停和恢复
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledTaskService {

    private final Scheduler scheduler;

    private static final String JOB_GROUP = "M3U8_REFRESH_JOB";
    private static final String TRIGGER_GROUP = "M3U8_REFRESH_TRIGGER";
    private static final String MANUAL_JOB_GROUP = "M3U8_REFRESH_JOB_MANUAL";
    private static final String MANUAL_TRIGGER_GROUP = "M3U8_REFRESH_TRIGGER_MANUAL";

    /**
     * 创建或更新定时任务
     *
     * @param provider M3U8 提供者
     */
    public void scheduleOrUpdateJob(M3U8Provider provider) {
        if (!provider.getEnabled() || provider.getRefreshRate() == null || provider.getRefreshRate() <= 0) {
            deleteJob(provider.getId());
            return;
        }

        try {
            JobDetail jobDetail = JobBuilder.newJob(M3U8RefreshJob.class)
                    .withIdentity(getJobKey(provider.getId()))
                    .usingJobData("providerId", provider.getId())
                    .usingJobData("triggerType", "scheduled")
                    .storeDurably(false)
                    .build();

            Trigger trigger = newTrigger()
                    .withIdentity(getTriggerKey(provider.getId()))
                    .withSchedule(simpleSchedule()
                            .withIntervalInSeconds(provider.getRefreshRate())
                            .repeatForever())
                    .build();

            scheduler.scheduleJob(jobDetail, trigger);
            log.info("Scheduled M3U8 refresh job: provider={}, interval={}s", provider.getId(), provider.getRefreshRate());
        } catch (SchedulerException e) {
            log.error("Failed to schedule job for provider: {}", provider.getId(), e);
            throw new RuntimeException("Failed to schedule job", e);
        }
    }

    /**
     * 手动触发一次性刷新任务
     *
     * @param providerId M3U8 提供者 ID
     * @param taskId 任务记录 ID
     * @return Quartz Job Key
     */
    public JobKey triggerManualJob(Long providerId, Long taskId) {
        try {
            String jobName = "m3u8-manual-" + providerId + "-" + System.currentTimeMillis();
            JobKey jobKey = JobKey.jobKey(jobName, MANUAL_JOB_GROUP);

            JobDetail jobDetail = JobBuilder.newJob(M3U8RefreshJob.class)
                    .withIdentity(jobKey)
                    .usingJobData("providerId", providerId)
                    .usingJobData("taskId", taskId)
                    .usingJobData("triggerType", "manual")
                    .storeDurably(false)
                    .build();

            // 立即触发的触发器
            Trigger trigger = newTrigger()
                    .withIdentity(TriggerKey.triggerKey(jobName, MANUAL_TRIGGER_GROUP))
                    .startNow()
                    .build();

            scheduler.scheduleJob(jobDetail, trigger);
            log.info("Triggered manual M3U8 refresh job: provider={}, task={}", providerId, taskId);
            return jobKey;
        } catch (SchedulerException e) {
            log.error("Failed to trigger manual job for provider: {}, task={}", providerId, taskId, e);
            throw new RuntimeException("Failed to trigger manual job", e);
        }
    }

    /**
     * 删除定时任务
     *
     * @param providerId M3U8 提供者 ID
     */
    public void deleteJob(Long providerId) {
        try {
            JobKey jobKey = getJobKey(providerId);
            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
                log.info("Deleted M3U8 refresh job: provider={}", providerId);
            }
        } catch (SchedulerException e) {
            log.error("Failed to delete job for provider: {}", providerId, e);
        }
    }

    private JobKey getJobKey(Long providerId) {
        return JobKey.jobKey("m3u8-refresh-" + providerId, JOB_GROUP);
    }

    private TriggerKey getTriggerKey(Long providerId) {
        return TriggerKey.triggerKey("m3u8-refresh-" + providerId, TRIGGER_GROUP);
    }
}
