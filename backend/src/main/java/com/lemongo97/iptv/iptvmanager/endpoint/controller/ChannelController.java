package com.lemongo97.iptv.iptvmanager.endpoint.controller;

import com.lemongo97.iptv.iptvmanager.common.ApiResponse;
import com.lemongo97.iptv.iptvmanager.common.PageResult;
import com.lemongo97.iptv.iptvmanager.endpoint.controller.request.ChannelQuery;
import com.lemongo97.iptv.iptvmanager.cleanup.rule.RuleType;
import com.lemongo97.iptv.iptvmanager.entity.Channel;
import com.lemongo97.iptv.iptvmanager.service.ChannelService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 频道控制器
 */
@RestController
@AllArgsConstructor
@RequestMapping("/channel")
public class ChannelController {

    private final ChannelService channelService;

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
    @GetMapping("/{id:\\d+}/timeline")
    public ApiResponse<Channel.ChannelEPGTimeline> getEPGTimeline(@PathVariable Long id) {
        return ApiResponse.ok(channelService.getEPGTimeline(id));
    }

    /**
     * 获取所有频道
     */
    @GetMapping
    public ApiResponse<PageResult<Channel>> find(ChannelQuery query) {
        return ApiResponse.ok(channelService.findByQuery(query));
    }

    @GetMapping("/options")
    public ApiResponse<Map<String, ?>> options() {
        return ApiResponse.ok(channelService.getOptions());
    }

    @PostMapping("/clean/{step}")
    public ApiResponse<Void> clean(@PathVariable RuleType step) {
        channelService.dataClean(step);
        return ApiResponse.ok(null);
    }
}
