package com.lemongo97.iptv.iptvmanager.service;

import com.lemongo97.iptv.iptvmanager.dto.IPTVRefreshTaskDTO;
import com.lemongo97.iptv.iptvmanager.entity.IPTVProviderRefreshTask;
import com.lemongo97.iptv.iptvmanager.entity.OriginalChannelMetadata;
import com.lemongo97.iptv.iptvmanager.mapper.IPTVProviderRefreshTaskMapper;
import com.lemongo97.iptv.iptvmanager.mapper.OriginalChannelMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * IPTV 刷新任务服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IPTVProviderRefreshTaskService {

    private final IPTVProviderRefreshTaskMapper taskMapper;
    private final OriginalChannelMapper originalChannelMapper;

    /**
     * 分页查询任务历史 - 返回 DTO，包含订阅源名称
     */
    public List<IPTVRefreshTaskDTO> findAll(String providerName, String triggerType, String status,
                                            LocalDateTime startTime, LocalDateTime endTime,
                                            Integer offset, Integer limit) {
        return taskMapper.findAll(providerName, triggerType, status, startTime, endTime, offset, limit);
    }

    /**
     * 统计任务数量
     */
    public Long count(String providerName, String triggerType, String status,
                      LocalDateTime startTime, LocalDateTime endTime) {
        return taskMapper.count(providerName, triggerType, status, startTime, endTime);
    }

    /**
     * 根据 ID 查询 - 返回 DTO，包含订阅源名称
     */
    public IPTVRefreshTaskDTO findById(Long id) {
        return taskMapper.findById(id);
    }

    /**
     * 创建新任务
     */
    public IPTVProviderRefreshTask create(IPTVProviderRefreshTask task) {
        var now = LocalDateTime.now();
        var newTask = new IPTVProviderRefreshTask(
            null,
            task.getProviderId(),
            null, // providerName 通过关联查询获取，不再存储
            task.getTriggerType(),
            task.getStatus(),
            task.getStartTime(),
            task.getEndTime(),
            task.getDuration(),
            task.getChannelCount(),
            task.getErrorMessage(),
            task.getRawContent(),
            now,
            now
        );
        taskMapper.insert(newTask);
        return newTask;
    }

    /**
     * 更新任务
     */
    public IPTVProviderRefreshTask update(Long id, IPTVProviderRefreshTask task) {
        var existing = findById(id);
        var updated = new IPTVProviderRefreshTask(
            id,
            task.getProviderId() != null ?   task.getProviderId() :   existing.getProviderId(),
            null, // providerName 通过关联查询获取，不再更新
            task.getTriggerType() != null ?  task.getTriggerType() :  existing.getTriggerType(),
            task.getStatus() != null ?       task.getStatus() :       existing.getStatus(),
            task.getStartTime() != null ?    task.getStartTime() :    existing.getStartTime(),
            task.getEndTime(),
            task.getDuration(),
            task.getChannelCount(),
            task.getErrorMessage(),
            task.getRawContent(),
            existing.getCreatedAt(),
            LocalDateTime.now()
        );
        taskMapper.update(updated);
        return updated;
    }

    /**
     * 获取任务解析的频道列表
     */
    public List<OriginalChannelMetadata> getChannelsByTask(Long taskId) {
        var refreshTask = findById(taskId);
        return originalChannelMapper.findByTaskId(taskId);
    }
}
