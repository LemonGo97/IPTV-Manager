package com.lemongo97.iptv.iptvmanager.controller;

import com.lemongo97.iptv.iptvmanager.common.ApiResponse;
import com.lemongo97.iptv.iptvmanager.entity.M3U8Provider;
import com.lemongo97.iptv.iptvmanager.service.M3U8ProviderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * M3U8 源控制器
 */
@RestController
@RequestMapping("/m3u8/provider")
@RequiredArgsConstructor
public class M3U8ProviderController {

    private final M3U8ProviderService providerService;

    /**
     * 获取所有 M3U8 源
     */
    @GetMapping
    public ApiResponse<List<M3U8Provider>> findAll() {
        return ApiResponse.ok(providerService.findAll());
    }

    /**
     * 根据 ID 获取 M3U8 源
     */
    @GetMapping("/{id}")
    public ApiResponse<M3U8Provider> findById(@PathVariable Long id) {
        return ApiResponse.ok(providerService.findById(id));
    }

    /**
     * 创建 M3U8 源
     */
    @PostMapping
    public ApiResponse<M3U8Provider> create(@RequestBody M3U8Provider provider) {
        return ApiResponse.ok(providerService.create(provider), "M3U8 provider created successfully");
    }

    /**
     * 更新 M3U8 源
     */
    @PutMapping("/{id}")
    public ApiResponse<M3U8Provider> update(@PathVariable Long id, @RequestBody M3U8Provider provider) {
        return ApiResponse.ok(providerService.update(id, provider), "M3U8 provider updated successfully");
    }

    /**
     * 删除 M3U8 源
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteById(@PathVariable Long id) {
        providerService.deleteById(id);
        return ApiResponse.ok("M3U8 provider deleted successfully");
    }

    /**
     * 刷新 M3U8 源
     */
    @PostMapping("/{id}/refresh")
    public ApiResponse<Void> refresh(@PathVariable Long id) {
        providerService.refresh(id);
        return ApiResponse.ok("M3U8 provider refreshed successfully");
    }

    /**
     * 上传本地 M3U8 文件
     */
    @PostMapping("/upload")
    public ApiResponse<M3U8Provider> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "description", required = false) String description
    ) {
        return ApiResponse.ok(providerService.uploadFile(file, name, description), "File uploaded successfully");
    }
}
