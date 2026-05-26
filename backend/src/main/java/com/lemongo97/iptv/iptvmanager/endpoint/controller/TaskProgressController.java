package com.lemongo97.iptv.iptvmanager.endpoint.controller;

import com.lemongo97.iptv.iptvmanager.common.ApiResponse;
import com.lemongo97.iptv.iptvmanager.entity.TaskProgress;
import com.lemongo97.iptv.iptvmanager.service.TaskProgressService;
import org.springframework.web.bind.annotation.*;

/**
 * 任务进度控制器
 */
@RestController
@RequestMapping("/task")
public class TaskProgressController {

    private final TaskProgressService taskProgressService;

    public TaskProgressController(TaskProgressService taskProgressService) {
        this.taskProgressService = taskProgressService;
    }

    /**
     * 获取任务进度
     */
    @GetMapping("/progress/{taskId}")
    public ApiResponse<TaskProgress> getProgress(@PathVariable String taskId) {
        return taskProgressService.getTaskProgress(taskId)
                .map(ApiResponse::ok)
                .orElseGet(() -> ApiResponse.error("Task not found: " + taskId));
    }

    /**
     * 获取最新任务进度
     */
    @GetMapping("/progress/latest/{taskType}")
    public ApiResponse<TaskProgress> getLatestProgress(@PathVariable String taskType) {
        return taskProgressService.getLatestTask(taskType)
                .map(ApiResponse::ok)
                .orElseGet(() -> ApiResponse.error("No task found for type: " + taskType));
    }
}
