package com.lemongo97.iptv.iptvmanager.endpoint.controller;

import com.lemongo97.iptv.iptvmanager.common.ApiResponse;
import com.lemongo97.iptv.iptvmanager.entity.IPTVProvider;
import com.lemongo97.iptv.iptvmanager.service.IPTVProviderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * IPTV 源控制器
 */
@RestController
@RequestMapping("/m3u8/provider")
@RequiredArgsConstructor
public class IPTVProviderController {

    private final IPTVProviderService providerService;

    /**
     * 获取所有 IPTV 源
     */
    @GetMapping
    public ApiResponse<List<IPTVProvider>> findAll() {
        return ApiResponse.ok(providerService.findAll());
    }

    /**
     * 根据 ID 获取 IPTV 源
     */
    @GetMapping("/{id}")
    public ApiResponse<IPTVProvider> findById(@PathVariable Long id) {
        return ApiResponse.ok(providerService.findById(id));
    }

    /**
     * 创建 IPTV 源
     */
    @PostMapping
    public ApiResponse<IPTVProvider> create(@RequestBody IPTVProvider provider) {
        return ApiResponse.ok(providerService.create(provider), "IPTV provider created successfully");
    }

    /**
     * 更新 IPTV 源
     */
    @PutMapping("/{id}")
    public ApiResponse<IPTVProvider> update(@PathVariable Long id, @RequestBody IPTVProvider provider) {
        return ApiResponse.ok(providerService.update(id, provider), "IPTV provider updated successfully");
    }

    /**
     * 删除 IPTV 源
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteById(@PathVariable Long id) {
        providerService.deleteById(id);
        return ApiResponse.ok("IPTV provider deleted successfully");
    }

    /**
     * 刷新 IPTV 源（异步）
     * 提交刷新任务到队列，立即返回任务 ID
     */
    @PostMapping("/{id}/refresh")
    public ApiResponse<TaskSubmissionResult> refresh(@PathVariable Long id) {
        Long taskId = providerService.refresh(id);
        return ApiResponse.ok(
            new TaskSubmissionResult(taskId, "Refresh task submitted successfully"),
            "IPTV provider refresh task submitted"
        );
    }

    /**
     * 任务提交结果
     */
    public record TaskSubmissionResult(Long taskId, String message) {}

    /**
     * 上传本地 IPTV 文件
     */
    @PostMapping("/upload")
    public ApiResponse<IPTVProvider> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "description", required = false) String description
    ) {
        return ApiResponse.ok(providerService.uploadFile(file, name, description), "File uploaded successfully");
    }
}
