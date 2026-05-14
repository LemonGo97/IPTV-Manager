package com.lemongo97.iptv.iptvmanager.controller;

import com.lemongo97.iptv.iptvmanager.common.ApiResponse;
import com.lemongo97.iptv.iptvmanager.entity.Channel;
import com.lemongo97.iptv.iptvmanager.service.ChannelService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
     * 获取统计信息
     * @return
     */
    @GetMapping("/statistic")
    public ApiResponse<Map<String, Object>> statistic() {
        return ApiResponse.ok(channelService.statistic());
    }

    /**
     * 获取对应的EPG电子节目单时间轴
     * @return
     */
    @GetMapping("/{id}/timeline")
    public ApiResponse<Channel.ChannelEPGTimeline> getEPGTimeline(@PathVariable Long id) {
        return ApiResponse.ok(channelService.getEPGTimeline(id));
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

    @PostMapping("/clean")
    public ApiResponse<Void> clean() {
        channelService.dataClean();
        return ApiResponse.ok(null);
    }
}
