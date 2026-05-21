package com.lemongo97.iptv.iptvmanager.service;

import com.lemongo97.iptv.iptvmanager.common.BusinessException;
import com.lemongo97.iptv.iptvmanager.engine.CleanUpRuleParam;
import com.lemongo97.iptv.iptvmanager.entity.CleanupEngine;
import com.lemongo97.iptv.iptvmanager.entity.CleanupRule;
import com.lemongo97.iptv.iptvmanager.mapper.CleanupEngineMapper;
import com.lemongo97.iptv.iptvmanager.mapper.CleanupRuleMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class CleanRuleService {

    private final CleanupEngineMapper cleanupEngineMapper;
    private final CleanupRuleMapper cleanupRuleMapper;

    public List<CleanupEngine> getEngineList(){
        return cleanupEngineMapper.findAll();
    }

//    public static void main(String[] args) {
//        CleanRuleService cleanRuleService = new CleanRuleService(null);
//        ObjectMapper objectMapper = new ObjectMapper();
//        String json = objectMapper.writeValueAsString(cleanRuleService.caseCover());
//        System.out.println(json);
//    }

    private List<CleanUpRuleParam> blacklist() {
        return List.of(new CleanUpRuleParam.DynamicInputParam("keyword", "关键字"));
    }

    private List<CleanUpRuleParam> ffprobeFilter() {
        return List.of(
                new CleanUpRuleParam.NumberParam("delayMinutes", "最高延迟时间"),
                new CleanUpRuleParam.SwitchParam("discardNoVideo", "丢弃无视频"),
                new CleanUpRuleParam.SwitchParam("discardNoAudio", "丢弃无音频"),
                new CleanUpRuleParam.NumberParam("minVideoFrameWidth", "最小视频帧宽度"),
                new CleanUpRuleParam.NumberParam("minVideoFrameHeight", "最小视频帧高度")
        );
    }

    public List<CleanUpRuleParam> opencc() {

        List<CleanUpRuleParam.SelectParam.SelectParamOption> options = List.of(
                new CleanUpRuleParam.SelectParam.SelectParamOption("simple", "简体"),
                new CleanUpRuleParam.SelectParam.SelectParamOption("traditional", "繁体")
        );

        CleanUpRuleParam.SelectParam input = new CleanUpRuleParam.SelectParam("input", "输入语言");
        input.setOptions(options);

        CleanUpRuleParam.SelectParam output = new CleanUpRuleParam.SelectParam("output", "输出语言");
        output.setOptions(options);
        return List.of(
                input, output
        );
    }

    public List<CleanUpRuleParam> caseCover() {

        List<CleanUpRuleParam.SelectParam.SelectParamOption> options = List.of(
                new CleanUpRuleParam.SelectParam.SelectParamOption("uppercase", "大写"),
                new CleanUpRuleParam.SelectParam.SelectParamOption("lowercase", "小写")
        );

        CleanUpRuleParam.SelectParam input = new CleanUpRuleParam.SelectParam("input", "输入");
        input.setOptions(options);

        CleanUpRuleParam.SelectParam output = new CleanUpRuleParam.SelectParam("output", "输出");
        output.setOptions(options);
        return List.of(
                input, output
        );
    }

    public List<CleanUpRuleParam> regex() {
        CleanUpRuleParam.DynamicInputPairParam dynamicInputPairParam = new CleanUpRuleParam.DynamicInputPairParam("groups", "分组替换设置");
        dynamicInputPairParam.setKeyField("groupId");
        dynamicInputPairParam.setKeyPlaceholder("分组ID");
        dynamicInputPairParam.setValueField("text");
        dynamicInputPairParam.setValuePlaceholder("替换值");
        return List.of(
                new CleanUpRuleParam.InputParam("regex", "正则表达式"),
                dynamicInputPairParam
        );
    }

    public List<CleanUpRuleParam> string() {
        return List.of(
                new CleanUpRuleParam.InputParam("target", "匹配值"),
                new CleanUpRuleParam.InputParam("text", "替换文字")
        );
    }
    public List<CleanUpRuleParam> http() {

        List<CleanUpRuleParam.SelectParam.SelectParamOption> options = List.of(
                new CleanUpRuleParam.SelectParam.SelectParamOption("GET", "GET"),
                new CleanUpRuleParam.SelectParam.SelectParamOption("HEAD", "HEAD")
        );

        CleanUpRuleParam.SelectParam input = new CleanUpRuleParam.SelectParam("type", "检测方式");
        input.setOptions(options);
        return List.of(
                input,
                new CleanUpRuleParam.NumberParam("delayMinutes", "最大延迟时间")
        );
    }

    // ===== CRUD 方法 =====

    public List<CleanupRule> findAll(String ruleType) {
        if (ruleType == null || ruleType.isEmpty()) {
            return cleanupRuleMapper.findAll();
        }
        return cleanupRuleMapper.findByRuleType(ruleType);
    }

    public CleanupRule findById(Long id) {
        return cleanupRuleMapper.findById(id)
                .orElseThrow(() -> new BusinessException("Cleanup rule not found: id=" + id));
    }

    @Transactional
    public CleanupRule create(CleanupRule rule) {
        log.info("Creating cleanup rule: {}", rule.getName());
        var now = LocalDateTime.now();
        var newRule = new CleanupRule(
            null,
            rule.getName(),
            rule.getEngine(),
            rule.getRuleType(),
            rule.getEnabled() != null ? rule.getEnabled() : true,
            rule.getParams(),
            0,
            now,
            now,
            false
        );
        cleanupRuleMapper.insert(newRule);
        log.info("Cleanup rule created: id={}", newRule.getId());
        return newRule;
    }

    @Transactional
    public CleanupRule update(Long id, CleanupRule rule) {
        var existing = findById(id);
        log.info("Updating cleanup rule: id={}", id);

        var updated = new CleanupRule(
            id,
            rule.getName() != null ? rule.getName() : existing.getName(),
            rule.getEngine() != null ? rule.getEngine() : existing.getEngine(),
            rule.getRuleType() != null ? rule.getRuleType() : existing.getRuleType(),
            rule.getEnabled() != null ? rule.getEnabled() : existing.getEnabled(),
            rule.getParams() != null ? rule.getParams() : existing.getParams(),
            existing.getSortOrder(),
            existing.getCreatedAt(),
            LocalDateTime.now(),
            existing.getDeleted()
        );

        cleanupRuleMapper.update(updated);
        log.info("Cleanup rule updated: id={}", id);
        return updated;
    }

    @Transactional
    public void deleteById(Long id) {
        findById(id);
        log.info("Deleting cleanup rule: id={}", id);
        cleanupRuleMapper.deleteById(id);
    }

    @Transactional
    public void reorderRules(String ruleType, java.util.List<Long> ruleIds) {
        log.info("Reordering {} rules: {}", ruleType, ruleIds);

        var rules = new java.util.ArrayList<CleanupRule>();

        for (int i = 0; i < ruleIds.size(); i++) {
            Long id = ruleIds.get(i);
            var rule = findById(id);

            if (!rule.getRuleType().equals(ruleType)) {
                throw new BusinessException(
                    "Rule id=" + id + " has type " + rule.getRuleType() +
                    ", expected " + ruleType
                );
            }

            // Create updated rule with new sort order
            var updatedRule = new CleanupRule(
                rule.getId(),
                rule.getName(),
                rule.getEngine(),
                rule.getRuleType(),
                rule.getEnabled(),
                rule.getParams(),
                i, // new sort order
                rule.getCreatedAt(),
                LocalDateTime.now(),
                rule.getDeleted()
            );

            rules.add(updatedRule);
        }

        // Batch update all sort orders
        cleanupRuleMapper.batchUpdateSortOrder(rules);
        log.info("Reordered {} {} rules successfully", ruleIds.size(), ruleType);
    }
}
