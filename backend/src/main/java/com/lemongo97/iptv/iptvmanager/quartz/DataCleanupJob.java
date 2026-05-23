package com.lemongo97.iptv.iptvmanager.quartz;

import com.lemongo97.iptv.iptvmanager.engine.RuleType;
import com.lemongo97.iptv.iptvmanager.service.CleanupRuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

/**
 * 数据清洗定时任务
 * 使用 Quartz 框架定期执行数据清洗
 * 从原始频道元数据转换为清洗后的频道数据
 */
@Slf4j
@Component
@DisallowConcurrentExecution
@RequiredArgsConstructor
public class DataCleanupJob implements org.quartz.Job {

    private final CleanupRuleService cleanupRuleService;

    @Override
    public void execute(JobExecutionContext context) {
        String triggerType = context.getJobDetail().getJobDataMap().getString("triggerType");
        String stepString = context.getJobDetail().getJobDataMap().getString("step");
        log.info("Executing data cleanup job, trigger: {}", triggerType);

        RuleType step = StringUtils.isNotBlank(stepString) ? RuleType.valueOf(stepString) : null;

        long startTime = System.currentTimeMillis();

        try {
            int channelCount = cleanupRuleService.executeDataCleanup(step);

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            log.info("Data cleanup completed successfully: channels={}, duration={}ms", channelCount, duration);

        } catch (Exception e) {
            log.error("Data cleanup job failed", e);
            throw e;
        }
    }
}
