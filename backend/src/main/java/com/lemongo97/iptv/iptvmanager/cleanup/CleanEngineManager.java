package com.lemongo97.iptv.iptvmanager.cleanup;

import com.lemongo97.iptv.iptvmanager.cleanup.config.CleanupEngineConfig;
import com.lemongo97.iptv.iptvmanager.cleanup.engine.CleaningEngine;
import com.lemongo97.iptv.iptvmanager.cleanup.rule.RuleType;
import com.lemongo97.iptv.iptvmanager.entity.Channel;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * [原始 IPTV 数据]
 *        │
 *        ▼
 *  1. 过滤规则 (Filter)       ───► 移除政治、色情或无法解析的非法频道
 *        │
 *        ▼
 *  2. 名称统一规则 (Name)     ───► 将 "CCTV1 综合", "CCTV-1 高清" 统一为 "CCTV-1"
 *        │
 *        ▼
 *  3. 合并规则 (Merge)        ───► 将相同名称的频道合并，保留多个播放源（备用线路）
 *        │
 *        ▼
 *  4. 延迟检测规则 (Delay)     ───► 测试测速，剔除超时或死链，按延迟对播放源排序
 *        │
 *        ▼
 *  5. 分组规则 (Group)        ───► 根据名称或标签分类，如 "央视频道", "卫视频道"
 *        │
 *        ▼
 * [标准 IPTV 输出]
 */
@Data
@AllArgsConstructor
public class CleanEngineManager {

    private final CleaningEngineFactory factory;

    public Channel process(Channel channel, List<CleanupEngineConfig> configList){
        List<CleanupEngineConfig> filterConfig = configList.stream().filter(config -> config.getRuleType() == RuleType.FILTER).toList();
        List<CleanupEngineConfig> nameConfig = configList.stream().filter(config -> config.getRuleType() == RuleType.NAME).toList();
        List<CleanupEngineConfig> mergeConfig = configList.stream().filter(config -> config.getRuleType() == RuleType.MERGE).toList();
        List<CleanupEngineConfig> delayConfig = configList.stream().filter(config -> config.getRuleType() == RuleType.DELAY).toList();
        List<CleanupEngineConfig> groupConfig = configList.stream().filter(config -> config.getRuleType() == RuleType.GROUP).toList();

        Channel processingChannel;

        // 移除政治、色情或无法解析的非法频道
        processingChannel = this.doProcess(channel, filterConfig);
        // 将 "CCTV1 综合", "CCTV-1 高清" 统一为 "CCTV-1"
        processingChannel = this.doProcess(processingChannel, nameConfig);
        // 将相同名称的频道合并，保留多个播放源（备用线路）
        processingChannel = this.doProcess(processingChannel, mergeConfig);
        // 测试测速，剔除超时或死链，按延迟对播放源排序
        processingChannel = this.doProcess(processingChannel, delayConfig);
        // 根据名称或标签分类，如 "央视频道", "卫视频道"
        processingChannel = this.doProcess(processingChannel, groupConfig);

        return processingChannel;
    }

    private Channel doProcess(Channel channel, List<CleanupEngineConfig> filterConfig) {
        if (channel == null) return null;
        Channel processingChannel = null;
        for (CleanupEngineConfig config : filterConfig) {
            CleaningEngine engine = factory.getEngine(config.getEngine());
            if (engine == null) continue;

            processingChannel = engine.process(channel, config.getConfigParams());
        }
        return processingChannel == null ? channel : processingChannel;
    }

}
