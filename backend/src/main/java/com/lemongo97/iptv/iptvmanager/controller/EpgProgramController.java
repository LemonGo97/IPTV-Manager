package com.lemongo97.iptv.iptvmanager.controller;

import com.lemongo97.iptv.iptvmanager.common.ApiResponse;
import com.lemongo97.iptv.iptvmanager.entity.EpgProgram;
import com.lemongo97.iptv.iptvmanager.mapper.EpgProgramMapper;
import com.lemongo97.iptv.iptvmanager.service.EpgProgramService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * EPG 节目控制器
 */
@RestController
@RequestMapping("/epg/program")
public class EpgProgramController {

    private final EpgProgramService epgProgramService;

    public EpgProgramController(EpgProgramService epgProgramService) {
        this.epgProgramService = epgProgramService;
    }

    /**
     * 获取指定源的频道列表（按频道名分组）
     * 用于前端树形数据的第一层展示
     */
    @GetMapping("/channels")
    public ApiResponse<List<EpgProgramMapper.ChannelGroup>> getChannels(
            @RequestParam Long sourceId) {
        return ApiResponse.ok(epgProgramService.getChannelGroups(sourceId));
    }

    /**
     * 获取指定频道的节目列表
     * 用于前端树形数据的第二层展示（懒加载）
     */
    @GetMapping("/list")
    public ApiResponse<List<EpgProgram>> getPrograms(
            @RequestParam Long sourceId,
            @RequestParam String channelId) {
        return ApiResponse.ok(epgProgramService.findByChannel(sourceId, channelId));
    }
}
