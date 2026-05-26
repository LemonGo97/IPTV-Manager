package com.lemongo97.iptv.iptvmanager.endpoint.controller;

import com.lemongo97.iptv.iptvmanager.common.ApiResponse;
import com.lemongo97.iptv.iptvmanager.entity.EpgProvider;
import com.lemongo97.iptv.iptvmanager.entity.TaskProgress;
import com.lemongo97.iptv.iptvmanager.service.EpgProviderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * EPG 源控制器
 */
@RestController
@RequestMapping("/epg/source")
public class EpgProviderController {

    private final EpgProviderService epgProviderService;

    public EpgProviderController(EpgProviderService epgProviderService) {
        this.epgProviderService = epgProviderService;
    }

    /**
     * 获取所有 EPG 源
     */
    @GetMapping
    public ApiResponse<List<EpgProvider>> findAll(@RequestParam(required = false) String name) {
        List<EpgProvider> providers;
        if (name != null && !name.isEmpty()) {
            providers = epgProviderService.findByCondition(name);
        } else {
            providers = epgProviderService.findAll();
        }
        return ApiResponse.ok(providers);
    }

    /**
     * 根据 ID 获取 EPG 源
     */
    @GetMapping("/{id}")
    public ApiResponse<EpgProvider> findById(@PathVariable Long id) {
        return ApiResponse.ok(epgProviderService.findById(id));
    }

    /**
     * 创建 EPG 源
     */
    @PostMapping
    public ApiResponse<EpgProvider> create(@RequestBody EpgProvider epgProvider) {
        return ApiResponse.ok(epgProviderService.create(epgProvider), "EPG Provider created successfully");
    }

    /**
     * 更新 EPG 源
     */
    @PutMapping("/{id}")
    public ApiResponse<EpgProvider> update(@PathVariable Long id, @RequestBody EpgProvider epgProvider) {
        return ApiResponse.ok(epgProviderService.update(id, epgProvider), "EPG Provider updated successfully");
    }

    /**
     * 删除 EPG 源
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteById(@PathVariable Long id) {
        epgProviderService.deleteById(id);
        return ApiResponse.ok("EPG Provider deleted successfully");
    }

    /**
     * 刷新 EPG 源（异步）
     * 返回任务进度对象，前端可轮询任务状态
     */
    @PostMapping("/{id}/refresh")
    public ApiResponse<TaskProgress> refresh(@PathVariable Long id) {
        TaskProgress task = epgProviderService.refresh(id);
        return ApiResponse.ok(task, "EPG Provider refresh task submitted");
    }
}
