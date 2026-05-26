package com.lemongo97.iptv.iptvmanager.endpoint.controller;

import com.lemongo97.iptv.iptvmanager.common.ApiResponse;
import com.lemongo97.iptv.iptvmanager.dto.IPTVRefreshTaskDTO;
import com.lemongo97.iptv.iptvmanager.entity.OriginalChannelMetadata;
import com.lemongo97.iptv.iptvmanager.service.IPTVProviderRefreshTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * IPTV 刷新任务控制器
 */
@RestController
@RequestMapping("/m3u8/task/history")
@RequiredArgsConstructor
public class IPTVRefreshTaskController {

    private final IPTVProviderRefreshTaskService taskService;

    /**
     * 分页查询任务历史
     */
    @GetMapping
    public ApiResponse<List<IPTVRefreshTaskDTO>> findAll(
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
    public ApiResponse<IPTVRefreshTaskDTO> findById(@PathVariable Long id) {
        return ApiResponse.ok(taskService.findById(id));
    }

    /**
     * 获取任务解析的频道列表
     */
    @GetMapping("/{id}/channels")
    public ApiResponse<List<OriginalChannelMetadata>> getChannelsByTask(@PathVariable Long id) {
        return ApiResponse.ok(taskService.getChannelsByTask(id));
    }
}
