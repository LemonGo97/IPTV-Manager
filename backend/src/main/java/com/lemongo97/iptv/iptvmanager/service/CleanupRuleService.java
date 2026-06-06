package com.lemongo97.iptv.iptvmanager.service;

import com.lemongo97.iptv.iptvmanager.cleanup.engine.group.GroupingEngine;
import com.lemongo97.iptv.iptvmanager.common.BusinessException;
import com.lemongo97.iptv.iptvmanager.entity.ChannelGroup;
import com.lemongo97.iptv.iptvmanager.entity.CleanupEngine;
import com.lemongo97.iptv.iptvmanager.entity.CleanupRule;
import com.lemongo97.iptv.iptvmanager.mapper.ChannelGroupMapper;
import com.lemongo97.iptv.iptvmanager.mapper.CleanupEngineMapper;
import com.lemongo97.iptv.iptvmanager.mapper.CleanupRuleMapper;
import com.lemongo97.iptv.iptvmanager.utils.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Strings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据清洗规则服务
 * 合并了 CleanRuleService 和 CleanupRuleService 的功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CleanupRuleService {

    private final CleanupEngineMapper cleanupEngineMapper;
    private final CleanupRuleMapper cleanupRuleMapper;
    private final ChannelGroupMapper channelGroupMapper;

    /**
     * 获取所有清洗引擎列表
     */
    public List<CleanupEngine> getEngineList() {
        List<CleanupEngine> engines = cleanupEngineMapper.findAll();
        List<ChannelGroup> groups = channelGroupMapper.findAll();

        List<ObjectNode> groupOptions = new ArrayList<>();
        groups.forEach(group ->
                groupOptions.add(JsonNodeFactory.instance.objectNode()
                        .put("label", group.getName())
                        .put("value", group.getId())));

        for (CleanupEngine engine : engines) {
            if (Strings.CI.equals(engine.getFullClassName(), GroupingEngine.class.getName())){
                JsonNode jsonNode = JSONUtil.parseJson(engine.getParams());
                for (JsonNode node : jsonNode.asArray()) {
                    ObjectNode object = node.asObject();
                    JsonNode field = object.get("field");
                    if (Strings.CI.equals(field.asString(), "groupId")) {
                        ArrayNode options = object.get("options").asArray();
                        options.addAll(groupOptions);
                    }
                }
                engine.setParams(JSONUtil.toJsonString(jsonNode));
            }
        }
        return engines;
    }

    /**
     * 获取所有清洗规则（可选按规则类型过滤）
     */
    public List<CleanupRule> findAll(String ruleType) {
        if (ruleType == null || ruleType.isEmpty()) {
            return cleanupRuleMapper.findAll();
        }
        return cleanupRuleMapper.findByRuleType(ruleType);
    }

    /**
     * 根据 ID 获取清洗规则
     */
    public CleanupRule findById(Long id) {
        return cleanupRuleMapper.findById(id)
                .orElseThrow(() -> new BusinessException("Cleanup rule not found: id=" + id));
    }

    /**
     * 创建清洗规则
     */
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

    /**
     * 更新清洗规则
     */
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

    /**
     * 删除清洗规则（逻辑删除）
     */
    @Transactional
    public void deleteById(Long id) {
        findById(id);
        log.info("Deleting cleanup rule: id={}", id);
        cleanupRuleMapper.deleteById(id);
    }

    /**
     * 批量重新排序规则
     */
    @Transactional
    public void reorderRules(String ruleType, List<Long> ruleIds) {
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
