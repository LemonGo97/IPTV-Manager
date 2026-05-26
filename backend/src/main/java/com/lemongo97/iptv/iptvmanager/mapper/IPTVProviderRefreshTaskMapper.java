package com.lemongo97.iptv.iptvmanager.mapper;

import com.lemongo97.iptv.iptvmanager.dto.IPTVRefreshTaskDTO;
import com.lemongo97.iptv.iptvmanager.entity.IPTVProviderRefreshTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * IPTV 刷新任务 Mapper
 */
@Mapper
public interface IPTVProviderRefreshTaskMapper {

    /**
     * 查询所有任务历史（分页）- 返回 DTO，包含订阅源名称
     */
    List<IPTVRefreshTaskDTO> findAll(
        @Param("providerName") String providerName,
        @Param("triggerType") String triggerType,
        @Param("status") String status,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        @Param("offset") Integer offset,
        @Param("limit") Integer limit
    );

    /**
     * 统计任务数量
     */
    Long count(
        @Param("providerName") String providerName,
        @Param("triggerType") String triggerType,
        @Param("status") String status,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    /**
     * 根据 ID 查询 - 返回 DTO，包含订阅源名称
     */
    IPTVRefreshTaskDTO findById(Long id);

    /**
     * 插入任务记录
     */
    int insert(IPTVProviderRefreshTask task);

    /**
     * 更新任务记录
     */
    int update(IPTVProviderRefreshTask task);
}
