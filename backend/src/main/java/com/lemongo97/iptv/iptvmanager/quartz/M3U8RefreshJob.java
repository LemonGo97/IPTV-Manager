package com.lemongo97.iptv.iptvmanager.quartz;

import com.lemongo97.iptv.iptvmanager.service.M3U8ProviderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

/**
 * M3U8 刷新定时任务
 * 使用 Quartz 框架定期刷新 M3U8 提供者
 */
@Slf4j
@Component
@DisallowConcurrentExecution
@RequiredArgsConstructor
public class M3U8RefreshJob implements Job {

    private final M3U8ProviderService providerService;

    @Override
    public void execute(JobExecutionContext context) {
        Long providerId = context.getJobDetail().getJobDataMap().getLong("providerId");
        log.debug("Executing M3U8 refresh job for provider: {}", providerId);

        try {
            providerService.refresh(providerId);
        } catch (Exception e) {
            log.error("M3U8 refresh job failed for provider: {}", providerId, e);
        }
    }
}
