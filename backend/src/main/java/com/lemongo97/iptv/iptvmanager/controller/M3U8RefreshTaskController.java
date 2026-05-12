package com.lemongo97.iptv.iptvmanager.controller;

import com.lemongo97.iptv.iptvmanager.common.ApiResponse;
import com.lemongo97.iptv.iptvmanager.entity.Channel;
import com.lemongo97.iptv.iptvmanager.entity.M3U8RefreshTask;
import com.lemongo97.iptv.iptvmanager.service.M3U8RefreshTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * M3U8 刷新任务控制器
 */
@RestController
@RequestMapping("/m3u8/task/history")
@RequiredArgsConstructor
public class M3U8RefreshTaskController {

    private final M3U8RefreshTaskService taskService;

    /**
     * 分页查询任务历史
     */
    @GetMapping
    public ApiResponse<List<M3U8RefreshTask>> findAll(
            @RequestParam(required = false) String providerName,
            @RequestParam(required = false) String triggerType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime,
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "20") Integer limit
    ) {
        var tasks = taskService.findAll(providerName, triggerType, status, startTime, endTime, offset, limit);
        return ApiResponse.ok(tasks);
    }

    /**
     * 获取任务总数（用于分页）
     */
    @GetMapping("/count")
    public ApiResponse<Long> count(
            @RequestParam(required = false) String providerName,
            @RequestParam(required = false) String triggerType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime
    ) {
        var total = taskService.count(providerName, triggerType, status, startTime, endTime);
        return ApiResponse.ok(total);
    }

    /**
     * 根据 ID 获取任务详情
     */
    @GetMapping("/{id}")
    public ApiResponse<M3U8RefreshTask> findById(@PathVariable Long id) {
        return ApiResponse.ok(taskService.findById(id));
    }

    /**
     * 获取任务解析的频道列表
     */
    @GetMapping("/{id}/channels")
    public ApiResponse<List<Channel>> getChannelsByTask(@PathVariable Long id) {
        return ApiResponse.ok(taskService.getChannelsByTask(id));
    }
}
