package com.lemongo97.iptv.iptvmanager.service;

import com.lemongo97.iptv.iptvmanager.common.BusinessException;
import com.lemongo97.iptv.iptvmanager.entity.EpgProvider;
import com.lemongo97.iptv.iptvmanager.entity.TaskProgress;
import com.lemongo97.iptv.iptvmanager.mapper.EpgProviderMapper;
import com.lemongo97.iptv.iptvmanager.quartz.ScheduledTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * EPG 源服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EpgProviderService {

    private final EpgProviderMapper epgProviderMapper;
    private final ScheduledTaskService scheduledTaskService;
    private final TaskProgressService taskProgressService;

    public List<EpgProvider> findAll() {
        return epgProviderMapper.findAll();
    }

    public EpgProvider findById(Long id) {
        return epgProviderMapper.findById(id)
                .orElseThrow(() -> new BusinessException("EPG Provider not found: id=" + id));
    }

    public List<EpgProvider> findEnabled() {
        return epgProviderMapper.findEnabled();
    }

    /**
     * 根据条件搜索 EPG 源
     */
    public List<EpgProvider> findByCondition(String name) {
        return epgProviderMapper.findByCondition(name);
    }

    @Transactional
    public EpgProvider create(EpgProvider epgProvider) {
        log.info("Creating EPG Provider: {}", epgProvider.getName());

        var now = LocalDateTime.now();
        var newEpgProvider = new EpgProvider(
                null,
                epgProvider.getName(),
                epgProvider.getUrl(),
                epgProvider.getType() != null ? epgProvider.getType() : EpgProvider.Type.XMLTV,
                epgProvider.getEnabled() != null ? epgProvider.getEnabled() : true,
                epgProvider.getDescription(),
                now,
                now,
                false
        );

        epgProviderMapper.insert(newEpgProvider);
        log.info("EPG Provider created: id={}", newEpgProvider.getId());
        return newEpgProvider;
    }

    @Transactional
    public EpgProvider update(Long id, EpgProvider epgProvider) {
        var existing = findById(id);
        log.info("Updating EPG Provider: id={}", id);

        EpgProvider updated = new EpgProvider(
                id,
                epgProvider.getName() != null ? epgProvider.getName() : existing.getName(),
                epgProvider.getUrl() != null ? epgProvider.getUrl() : existing.getUrl(),
                epgProvider.getType() != null ? epgProvider.getType() : existing.getType(),
                epgProvider.getEnabled() != null ? epgProvider.getEnabled() : existing.getEnabled(),
                epgProvider.getDescription(),
                existing.getCreatedAt(),
                LocalDateTime.now(),
                existing.getDeleted()
        );

        epgProviderMapper.update(updated);
        log.info("EPG Provider updated: id={}", id);
        return updated;
    }

    @Transactional
    public void deleteById(Long id) {
        findById(id);
        log.info("Deleting EPG Provider: id={}", id);
        epgProviderMapper.deleteById(id);
    }

    /**
     * 刷新 EPG 源（异步）
     * 创建任务记录并提交给 Quartz 执行，立即返回任务信息
     *
     * @param id EPG 源 ID
     * @return 任务进度对象
     */
    @Transactional
    public TaskProgress refresh(Long id) {
        EpgProvider epgProvider = findById(id);
        log.info("Submitting EPG Provider refresh task: id={}, url={}", id, epgProvider.getUrl());

        if (!epgProvider.getEnabled()) {
            throw new BusinessException("Cannot refresh disabled EPG Provider: id=" + id);
        }

        // 创建任务进度记录
        TaskProgress task = taskProgressService.createTask("EPG_REFRESH", null, "EPG 刷新任务已创建");

        // 提交给 Quartz 执行
        scheduledTaskService.triggerManualEpgRefreshJob(epgProvider.getId(), task.getTaskId());
        log.info("Submitted EPG refresh to Quartz: taskId={}, providerId={}", task.getTaskId(), epgProvider.getId());

        return task;
    }
}
