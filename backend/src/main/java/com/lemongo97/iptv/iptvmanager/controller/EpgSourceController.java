package com.lemongo97.iptv.iptvmanager.controller;

import com.lemongo97.iptv.iptvmanager.common.ApiResponse;
import com.lemongo97.iptv.iptvmanager.entity.EpgSource;
import com.lemongo97.iptv.iptvmanager.service.EpgSourceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * EPG 源控制器
 */
@RestController
@RequestMapping("/api/epg/source")
public class EpgSourceController {

    private final EpgSourceService epgSourceService;

    public EpgSourceController(EpgSourceService epgSourceService) {
        this.epgSourceService = epgSourceService;
    }

    /**
     * 获取所有 EPG 源
     */
    @GetMapping
    public ApiResponse<List<EpgSource>> findAll() {
        return ApiResponse.ok(epgSourceService.findAll());
    }

    /**
     * 根据 ID 获取 EPG 源
     */
    @GetMapping("/{id}")
    public ApiResponse<EpgSource> findById(@PathVariable Long id) {
        return ApiResponse.ok(epgSourceService.findById(id));
    }

    /**
     * 创建 EPG 源
     */
    @PostMapping
    public ApiResponse<EpgSource> create(@RequestBody EpgSource epgSource) {
        return ApiResponse.ok(epgSourceService.create(epgSource), "EPG source created successfully");
    }

    /**
     * 更新 EPG 源
     */
    @PutMapping("/{id}")
    public ApiResponse<EpgSource> update(@PathVariable Long id, @RequestBody EpgSource epgSource) {
        return ApiResponse.ok(epgSourceService.update(id, epgSource), "EPG source updated successfully");
    }

    /**
     * 删除 EPG 源
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteById(@PathVariable Long id) {
        epgSourceService.deleteById(id);
        return ApiResponse.ok("EPG source deleted successfully");
    }

    /**
     * 刷新 EPG 源
     */
    @PostMapping("/{id}/refresh")
    public ApiResponse<Void> refresh(@PathVariable Long id) {
        epgSourceService.refresh(id);
        return ApiResponse.ok("EPG source refreshed successfully");
    }
}
