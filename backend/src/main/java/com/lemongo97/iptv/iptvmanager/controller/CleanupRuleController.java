package com.lemongo97.iptv.iptvmanager.controller;

import com.lemongo97.iptv.iptvmanager.common.ApiResponse;
import com.lemongo97.iptv.iptvmanager.entity.CleanupEngine;
import com.lemongo97.iptv.iptvmanager.service.CleanRuleService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/channel/cleanup")
@AllArgsConstructor
public class CleanupRuleController {

    private final CleanRuleService cleanRuleService;

    /**
     * 获取页面规则新增弹窗参数
     */
    @GetMapping("/engines")
    public ApiResponse<List<CleanupEngine>> listEngine(){
        return ApiResponse.ok(cleanRuleService.getEngineList());
    }
}
