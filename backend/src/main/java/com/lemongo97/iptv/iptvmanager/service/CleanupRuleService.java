package com.lemongo97.iptv.iptvmanager.service;

import com.github.houbb.opencc4j.util.ZhConverterUtil;
import com.lemongo97.iptv.iptvmanager.common.BusinessException;
import com.lemongo97.iptv.iptvmanager.engine.CleanEngineManager;
import com.lemongo97.iptv.iptvmanager.engine.CleanUpRuleParam;
import com.lemongo97.iptv.iptvmanager.engine.EngineConfig;
import com.lemongo97.iptv.iptvmanager.engine.RuleType;
import com.lemongo97.iptv.iptvmanager.entity.CleanupEngine;
import com.lemongo97.iptv.iptvmanager.entity.CleanupRule;
import com.lemongo97.iptv.iptvmanager.entity.Channel;
import com.lemongo97.iptv.iptvmanager.mapper.CleanupEngineMapper;
import com.lemongo97.iptv.iptvmanager.mapper.CleanupRuleMapper;
import com.lemongo97.iptv.iptvmanager.mapper.ChannelCleaningTempMapper;
import com.lemongo97.iptv.iptvmanager.mapper.ChannelMapper;
import com.lemongo97.iptv.iptvmanager.mapper.OriginalChannelMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
    private final ChannelMapper channelMapper;
    private final OriginalChannelMapper originalChannelMapper;
    private final CleanEngineManager cleanEngineManager;
    private final ChannelCleaningTempMapper channelCleaningTempMapper;

    // ===== 引擎相关方法 =====

    /**
     * 获取所有清洗引擎列表
     */
    public List<CleanupEngine> getEngineList() {
        return cleanupEngineMapper.findAll();
    }

    /**
     * 黑名单引擎参数
     */
    public List<CleanUpRuleParam> blacklistParams() {
        return List.of(new CleanUpRuleParam.DynamicInputParam("keyword", "关键字"));
    }

    /**
     * FFprobe 过滤引擎参数
     */
    public List<CleanUpRuleParam> ffprobeFilterParams() {
        return List.of(
                new CleanUpRuleParam.NumberParam("delayMillisecond", "最高延迟时间"),
                new CleanUpRuleParam.SwitchParam("discardNoVideo", "丢弃无视频"),
                new CleanUpRuleParam.SwitchParam("discardNoAudio", "丢弃无音频"),
                new CleanUpRuleParam.NumberParam("minVideoFrameWidth", "最小视频帧宽度"),
                new CleanUpRuleParam.NumberParam("minVideoFrameHeight", "最小视频帧高度")
        );
    }

    /**
     * OpenCC 引擎参数
     */
    public List<CleanUpRuleParam> openccParams() {
        List<CleanUpRuleParam.SelectParam.SelectParamOption> options = List.of(
                new CleanUpRuleParam.SelectParam.SelectParamOption("simple", "简体"),
                new CleanUpRuleParam.SelectParam.SelectParamOption("traditional", "繁体")
        );

        CleanUpRuleParam.SelectParam input = new CleanUpRuleParam.SelectParam("input", "输入语言");
        input.setOptions(options);

        CleanUpRuleParam.SelectParam output = new CleanUpRuleParam.SelectParam("output", "输出语言");
        output.setOptions(options);
        return List.of(input, output);
    }

    /**
     * 大小写转换引擎参数
     */
    public List<CleanUpRuleParam> caseCoverParams() {
        List<CleanUpRuleParam.SelectParam.SelectParamOption> options = List.of(
                new CleanUpRuleParam.SelectParam.SelectParamOption("uppercase", "大写"),
                new CleanUpRuleParam.SelectParam.SelectParamOption("lowercase", "小写")
        );

        CleanUpRuleParam.SelectParam input = new CleanUpRuleParam.SelectParam("input", "输入");
        input.setOptions(options);

        CleanUpRuleParam.SelectParam output = new CleanUpRuleParam.SelectParam("output", "输出");
        output.setOptions(options);
        return List.of(input, output);
    }

    /**
     * 正则替换引擎参数
     */
    public List<CleanUpRuleParam> regexParams() {
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

    /**
     * 字符串替换引擎参数
     */
    public List<CleanUpRuleParam> stringParams() {
        return List.of(
                new CleanUpRuleParam.InputParam("target", "匹配值"),
                new CleanUpRuleParam.InputParam("text", "替换文字")
        );
    }

    /**
     * HTTP 检测引擎参数
     */
    public List<CleanUpRuleParam> httpParams() {
        List<CleanUpRuleParam.SelectParam.SelectParamOption> options = List.of(
                new CleanUpRuleParam.SelectParam.SelectParamOption("GET", "GET"),
                new CleanUpRuleParam.SelectParam.SelectParamOption("HEAD", "HEAD")
        );

        CleanUpRuleParam.SelectParam input = new CleanUpRuleParam.SelectParam("type", "检测方式");
        input.setOptions(options);
        return List.of(
                input,
                new CleanUpRuleParam.NumberParam("delayMillisecond", "最大延迟时间")
        );
    }

    // ===== 数据清洗执行方法 =====

    /**
     * 获取所有启用的清洗规则配置
     */
    public List<EngineConfig> getEnabledEngineConfigs() {
        List<CleanupRule> rules = cleanupRuleMapper.findAll();
        return rules.stream()
                .filter(rule -> rule.getEnabled() != null && rule.getEnabled())
                .map(this::toEngineConfig)
                .collect(Collectors.toList());
    }

    /**
     * 执行数据清洗
     * 从原始频道元数据转换为频道，并应用清洗规则
     * 新流程：使用中间表，逐个频道处理，避免清洗中断导致数据丢失
     */
    public int executeDataCleanup() {
        log.info("Starting data cleanup process");

        // 1. 清空中间表（物理删除）
        channelCleaningTempMapper.truncate();
        log.info("Cleaned temp table: channel_cleaning_temp");

        // 2. 获取所有启用的清洗规则
        List<EngineConfig> configs = getEnabledEngineConfigs();
        log.debug("Found {} enabled cleanup rules", configs.size());

        // 3. 获取原始频道元数据并转换为 Channel
        List<Channel> channels = convertOriginalChannelsToChannels();
        log.info("Converted {} original channels to Channel format", channels.size());

        // 4. 逐个处理频道并立即插入中间表
        int processedCount = 0;
        for (int i = 0; i < channels.size(); i++) {
            Channel channel = channels.get(i);

            // 单频道处理（包装为单元素 List）
            List<Channel> input = List.of(channel);
            List<Channel> cleaned = cleanEngineManager.process(input, configs);

            // 如果未被过滤，插入中间表
            if (!cleaned.isEmpty()) {
                channelCleaningTempMapper.insert(cleaned.getFirst());
                processedCount++;
            }

            // 每处理 100 个频道记录一次日志
            if ((i + 1) % 100 == 0) {
                log.info("Processed {}/{} channels, {} valid so far", i + 1, channels.size(), processedCount);
            }
        }

        log.info("Channel processing completed: {}/{} valid channels", processedCount, channels.size());

        // 5. 从中间表转移到正式表
        List<Channel> tempChannels = channelCleaningTempMapper.findAll();
        channelMapper.truncate();
        channelMapper.insert(tempChannels);

        log.info("Data cleanup completed successfully, {} channels saved to main table", tempChannels.size());
        return tempChannels.size();
    }

    /**
     * 将原始频道元数据转换为频道对象
     */
    private List<Channel> convertOriginalChannelsToChannels() {
        return originalChannelMapper.findAll().stream()
                .map(o -> {

                    boolean tvgNameZh = ZhConverterUtil.containsChinese(o.getTvGuideName());
                    boolean tvgIdZh = ZhConverterUtil.containsChinese(o.getTvGuideId());
                    boolean cname = ZhConverterUtil.containsChinese(o.getName());

                    String name;
                    if (tvgNameZh){
                        name = o.getTvGuideName();
                    } else {
                        if (tvgIdZh && cname){
                            name = o.getTvGuideId();
                        }else {
                            name = o.getName();
                        }
                    }


//                    if (StringUtils.isNotBlank(o.getTvGuideName())){
//                        name = o.getTvGuideName();
//                    } else {
//                        String tvGuideName = o.getTvGuideName();
//                        String tvGuideId = o.getTvGuideId();
//                        String name1 = o.getName();
//                        if (StringUtils.isBlank(tvGuideId)) {
//                            name = name1;
//                        } else {
//                            if (ZhConverterUtil.containsChinese(tvGuideId)) {
//                                name = tvGuideId;
//                            } else {
//                                name = name1;
//                            }
//                        }
//                    }

                    Channel channel = new Channel()
                            .setName(name)
                            .setLogo(o.getTvGuideLogo())
                            .setUrl(o.getUrl())
                            .setProviderId(o.getProviderId())
                            .setGroupId(0L)
                            .setEpgSourceId(o.getTvGuideId())
                            .setStatus(Channel.Status.valid)
                            .setCountry(o.getTvGuideCountry())
                            .setLanguage(o.getTvGuideLanguage())
                            .setScore(100L)
                            .setCreatedAt(o.getCreatedAt())
                            .setUpdatedAt(o.getUpdatedAt());
                    return channel;
                })
                .toList();
    }

    /**
     * 将 CleanupRule 转换为 EngineConfig
     */
    private EngineConfig toEngineConfig(CleanupRule rule) {
        RuleType ruleType;
        try {
            ruleType = RuleType.valueOf(rule.getRuleType().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid rule type: {}, defaulting to FILTER", rule.getRuleType());
            ruleType = RuleType.FILTER;
        }

        EngineConfig config = new EngineConfig();
        config.setId(rule.getId());
        config.setRuleType(ruleType);
        config.setEngine(rule.getEngine());
        config.setConfigParams(rule.getParams());
        config.setSortOrder(rule.getSortOrder());
        return config;
    }

    // ===== CRUD 方法 =====

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
