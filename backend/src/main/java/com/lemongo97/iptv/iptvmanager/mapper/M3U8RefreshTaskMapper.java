package com.lemongo97.iptv.iptvmanager.mapper;

import com.lemongo97.iptv.iptvmanager.entity.M3U8RefreshTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * M3U8 刷新任务 Mapper
 */
@Mapper
public interface M3U8RefreshTaskMapper {

    /**
     * 查询所有任务历史（分页）
     */
    List<M3U8RefreshTask> findAll(
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
     * 根据 ID 查询
     */
    M3U8RefreshTask findById(Long id);

    /**
     * 插入任务记录
     */
    int insert(M3U8RefreshTask task);

    /**
     * 更新任务记录
     */
    int update(M3U8RefreshTask task);
}
