package com.lemongo97.iptv.iptvmanager.service;

import com.lemongo97.iptv.iptvmanager.common.BusinessException;
import com.lemongo97.iptv.iptvmanager.entity.EpgSource;
import com.lemongo97.iptv.iptvmanager.entity.TaskProgress;
import com.lemongo97.iptv.iptvmanager.mapper.EpgSourceMapper;
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
public class EpgSourceService {

    private final EpgSourceMapper epgSourceMapper;
    private final EpgProgramService epgProgramService;
    private final ScheduledTaskService scheduledTaskService;
    private final TaskProgressService taskProgressService;

    public List<EpgSource> findAll() {
        return epgSourceMapper.findAll();
    }

    public EpgSource findById(Long id) {
        return epgSourceMapper.findById(id)
                .orElseThrow(() -> new BusinessException("EPG source not found: id=" + id));
    }

    public List<EpgSource> findEnabled() {
        return epgSourceMapper.findEnabled();
    }

    /**
     * 根据条件搜索 EPG 源
     */
    public List<EpgSource> findByCondition(String name) {
        return epgSourceMapper.findByCondition(name);
    }

    @Transactional
    public EpgSource create(EpgSource epgSource) {
        log.info("Creating EPG source: {}", epgSource.getName());

        var now = LocalDateTime.now();
        var newEpgSource = new EpgSource(
                null,
                epgSource.getName(),
                epgSource.getUrl(),
                epgSource.getType() != null ? epgSource.getType() : "xml",
                epgSource.getEnabled() != null ? epgSource.getEnabled() : true,
                epgSource.getDescription(),
                now,
                now,
                false
        );

        epgSourceMapper.insert(newEpgSource);
        log.info("EPG source created: id={}", newEpgSource.getId());
        return newEpgSource;
    }

    @Transactional
    public EpgSource update(Long id, EpgSource epgSource) {
        var existing = findById(id);
        log.info("Updating EPG source: id={}", id);

        var updated = new EpgSource(
                id,
                epgSource.getName() != null ? epgSource.getName() : existing.getName(),
                epgSource.getUrl() != null ? epgSource.getUrl() : existing.getUrl(),
                epgSource.getType() != null ? epgSource.getType() : existing.getType(),
                epgSource.getEnabled() != null ? epgSource.getEnabled() : existing.getEnabled(),
                epgSource.getDescription(),
                existing.getCreatedAt(),
                LocalDateTime.now(),
                existing.getDeleted()
        );

        epgSourceMapper.update(updated);
        log.info("EPG source updated: id={}", id);
        return updated;
    }

    @Transactional
    public void deleteById(Long id) {
        findById(id);
        log.info("Deleting EPG source: id={}", id);
        epgSourceMapper.deleteById(id);
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
        var epgSource = findById(id);
        log.info("Submitting EPG source refresh task: id={}, url={}", id, epgSource.getUrl());

        if (!epgSource.getEnabled()) {
            throw new BusinessException("Cannot refresh disabled EPG source: id=" + id);
        }

        // 创建任务进度记录
        TaskProgress task = taskProgressService.createTask("EPG_REFRESH", null, "EPG 刷新任务已创建");

        // 提交给 Quartz 执行
        scheduledTaskService.triggerManualEpgRefreshJob(epgSource.getId(), task.getTaskId());
        log.info("Submitted EPG refresh to Quartz: taskId={}, sourceId={}", task.getTaskId(), epgSource.getId());

        return task;
    }
}
