package com.lemongo97.iptv.iptvmanager.mapper;

import com.lemongo97.iptv.iptvmanager.entity.TaskProgress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

/**
 * 任务进度 Mapper
 */
@Mapper
public interface TaskProgressMapper {

    /**
     * 插入任务进度记录
     */
    int insert(TaskProgress taskProgress);

    /**
     * 更新任务进度
     */
    int updateProgress(TaskProgress taskProgress);

    /**
     * 更新任务状态
     */
    int updateStatus(@Param("taskId") String taskId,
                     @Param("status") TaskProgress.Status status,
                     @Param("completedAt") String completedAt,
                     @Param("message") String message);

    /**
     * 根据任务ID查询
     */
    Optional<TaskProgress> findByTaskId(@Param("taskId") String taskId);

    /**
     * 根据任务类型查询最新任务
     */
    Optional<TaskProgress> findLatestByTaskType(@Param("taskType") String taskType);
}
