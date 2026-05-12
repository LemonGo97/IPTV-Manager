package com.lemongo97.iptv.iptvmanager.controller;

import com.lemongo97.iptv.iptvmanager.common.ApiResponse;
import com.lemongo97.iptv.iptvmanager.entity.ChannelGroup;
import com.lemongo97.iptv.iptvmanager.service.ChannelGroupService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 频道组控制器
 */
@RestController
@RequestMapping("/channel/group")
public class ChannelGroupController {

    private final ChannelGroupService groupService;

    public ChannelGroupController(ChannelGroupService groupService) {
        this.groupService = groupService;
    }

    /**
     * 获取所有频道组
     */
    @GetMapping
    public ApiResponse<List<ChannelGroup>> findAll(
            @RequestParam(required = false) String name) {
        if (name != null && !name.isEmpty()) {
            return ApiResponse.ok(groupService.findByCondition(name));
        }
        return ApiResponse.ok(groupService.findAll());
    }

    /**
     * 根据 ID 获取频道组
     */
    @GetMapping("/{id}")
    public ApiResponse<ChannelGroup> findById(@PathVariable Long id) {
        return ApiResponse.ok(groupService.findById(id));
    }

    /**
     * 创建频道组
     */
    @PostMapping
    public ApiResponse<ChannelGroup> create(@RequestBody ChannelGroup group) {
        return ApiResponse.ok(groupService.create(group), "Channel group created successfully");
    }

    /**
     * 更新频道组
     */
    @PutMapping("/{id}")
    public ApiResponse<ChannelGroup> update(@PathVariable Long id, @RequestBody ChannelGroup group) {
        return ApiResponse.ok(groupService.update(id, group), "Channel group updated successfully");
    }

    /**
     * 删除频道组
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteById(@PathVariable Long id) {
        groupService.deleteById(id);
        return ApiResponse.ok("Channel group deleted successfully");
    }
}
