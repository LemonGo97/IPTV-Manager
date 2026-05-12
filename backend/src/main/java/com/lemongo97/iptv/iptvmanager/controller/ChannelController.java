package com.lemongo97.iptv.iptvmanager.controller;

import com.lemongo97.iptv.iptvmanager.common.ApiResponse;
import com.lemongo97.iptv.iptvmanager.entity.Channel;
import com.lemongo97.iptv.iptvmanager.service.ChannelService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 频道控制器
 */
@RestController
@RequestMapping("/channel")
public class ChannelController {

    private final ChannelService channelService;

    public ChannelController(ChannelService channelService) {
        this.channelService = channelService;
    }

    /**
     * 获取所有频道
     */
    @GetMapping
    public ApiResponse<List<Channel>> findAll() {
        return ApiResponse.ok(channelService.findAll());
    }

    /**
     * 根据 ID 获取频道
     */
    @GetMapping("/{id}")
    public ApiResponse<Channel> findById(@PathVariable Long id) {
        return ApiResponse.ok(channelService.findById(id));
    }

    /**
     * 根据分组获取频道
     */
    @GetMapping("/group/{group}")
    public ApiResponse<List<Channel>> findByGroup(@PathVariable String group) {
        return ApiResponse.ok(channelService.findByGroup(group));
    }

    /**
     * 创建频道
     */
    @PostMapping
    public ApiResponse<Channel> create(@RequestBody Channel channel) {
        return ApiResponse.ok(channelService.create(channel), "Channel created successfully");
    }

    /**
     * 更新频道
     */
    @PutMapping("/{id}")
    public ApiResponse<Channel> update(@PathVariable Long id, @RequestBody Channel channel) {
        return ApiResponse.ok(channelService.update(id, channel), "Channel updated successfully");
    }

    /**
     * 删除频道
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteById(@PathVariable Long id) {
        channelService.deleteById(id);
        return ApiResponse.ok("Channel deleted successfully");
    }
}
