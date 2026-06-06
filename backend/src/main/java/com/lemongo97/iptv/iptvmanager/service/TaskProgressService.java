package com.lemongo97.iptv.iptvmanager.service;

import com.lemongo97.iptv.iptvmanager.entity.TaskProgress;
import com.lemongo97.iptv.iptvmanager.mapper.TaskProgressMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

/**
 * 任务进度服务
 * 管理任务进度的创建、更新和查询
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskProgressService implements InitializingBean {

    private final TaskProgressMapper taskProgressMapper;
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * 创建新任务
     */
    @Transactional
    public TaskProgress createTask(String taskType, Integer totalItems, String message) {
        String taskId = UUID.randomUUID().toString();
        String now = LocalDateTime.now().format(ISO_FORMATTER);

        TaskProgress task = new TaskProgress(
                null,
                taskId,
                taskType,
                0d,
                TaskProgress.Status.NOT_RUNNING,
                message,
                totalItems,
                0,
                now,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        taskProgressMapper.insert(task);
        log.info("Created task: type={}, id={}, totalItems={}", taskType, taskId, totalItems);
        return task;
    }

    /**
     * 开始任务
     */
    @Transactional
    public void startTask(String taskId) {
        taskProgressMapper.updateStatus(taskId, TaskProgress.Status.RUNNING, null, "任务开始执行");
        log.info("Task started: {}", taskId);
    }

    /**
     * 更新任务进度
     */
    @Transactional
    public void updateProgress(String taskId, int processedItems, Double progress, String message) {
        TaskProgress task = new TaskProgress();
        task.setTaskId(taskId);
        task.setProcessedItems(processedItems);
        task.setProgress(progress);
        task.setMessage(message);
        task.setStatus(TaskProgress.Status.RUNNING);
        taskProgressMapper.updateProgress(task);

        if (log.isDebugEnabled()) {
            log.debug("Task progress updated: id={}, progress={}%, processed={}",
                    taskId, progress, processedItems);
        }
    }

    /**
     * 完成任务
     */
    @Transactional
    public void completeTask(String taskId, String message) {
        String completedAt = LocalDateTime.now().format(ISO_FORMATTER);
        taskProgressMapper.updateStatus(taskId, TaskProgress.Status.SUCCESS, completedAt, message);
        log.info("Task completed: {}", taskId);
    }

    /**
     * 标记任务失败
     */
    @Transactional
    public void failTask(String taskId, String errorMessage) {
        String completedAt = LocalDateTime.now().format(ISO_FORMATTER);
        taskProgressMapper.updateStatus(taskId, TaskProgress.Status.ERROR, completedAt, errorMessage);
        log.error("Task failed: {}, error={}", taskId, errorMessage);
    }

    /**
     * 获取任务进度
     */
    public Optional<TaskProgress> getTaskProgress(String taskId) {
        return taskProgressMapper.findByTaskId(taskId);
    }

    /**
     * 获取最新任务
     */
    public Optional<TaskProgress> getLatestTask(String taskType) {
        return taskProgressMapper.findLatestByTaskType(taskType);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        taskProgressMapper.failUnfinishedTasks();
    }
}
