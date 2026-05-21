package com.lemongo97.iptv.iptvmanager.controller;

import com.lemongo97.iptv.iptvmanager.common.ApiResponse;
import com.lemongo97.iptv.iptvmanager.entity.CleanupEngine;
import com.lemongo97.iptv.iptvmanager.entity.CleanupRule;
import com.lemongo97.iptv.iptvmanager.service.CleanRuleService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 获取所有规则（支持按 ruleType 过滤）
     */
    @GetMapping("/rules")
    public ApiResponse<List<CleanupRule>> listRules(
            @RequestParam(required = false) String ruleType) {
        return ApiResponse.ok(cleanRuleService.findAll(ruleType));
    }

    /**
     * 获取单个规则详情
     */
    @GetMapping("/rules/{id}")
    public ApiResponse<CleanupRule> getRule(@PathVariable Long id) {
        return ApiResponse.ok(cleanRuleService.findById(id));
    }

    /**
     * 创建规则
     */
    @PostMapping("/rules")
    public ApiResponse<CleanupRule> createRule(@RequestBody CleanupRule rule) {
        return ApiResponse.ok(cleanRuleService.create(rule));
    }

    /**
     * 更新规则
     */
    @PutMapping("/rules/{id}")
    public ApiResponse<CleanupRule> updateRule(
            @PathVariable Long id,
            @RequestBody CleanupRule rule) {
        return ApiResponse.ok(cleanRuleService.update(id, rule));
    }

    /**
     * 删除规则（逻辑删除）
     */
    @DeleteMapping("/rules/{id}")
    public ApiResponse<Void> deleteRule(@PathVariable Long id) {
        cleanRuleService.deleteById(id);
        return ApiResponse.ok(null);
    }
}
