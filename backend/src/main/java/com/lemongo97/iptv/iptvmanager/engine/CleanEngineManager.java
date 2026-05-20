package com.lemongo97.iptv.iptvmanager.engine;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * [原始 M3U8 数据]
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
 * [标准 M3U8 输出]
 */
@Data
@AllArgsConstructor
public class CleanEngineManager {

    private final CleaningEngineFactory factory;

    public List<Object> process(List<Object> channels, List<EngineConfig> configList){
        List<EngineConfig> filterConfig = configList.stream().filter(config -> config.getRuleType() == RuleType.FILTER).toList();
        List<EngineConfig> nameConfig = configList.stream().filter(config -> config.getRuleType() == RuleType.NAME).toList();
        List<EngineConfig> mergeConfig = configList.stream().filter(config -> config.getRuleType() == RuleType.MERGE).toList();
        List<EngineConfig> delayConfig = configList.stream().filter(config -> config.getRuleType() == RuleType.DELAY).toList();
        List<EngineConfig> groupConfig = configList.stream().filter(config -> config.getRuleType() == RuleType.GROUP).toList();

        List<Object> processingChannels;

        // 移除政治、色情或无法解析的非法频道
        processingChannels = this.doProcess(channels, filterConfig);
        // 将 "CCTV1 综合", "CCTV-1 高清" 统一为 "CCTV-1"
        processingChannels = this.doProcess(processingChannels, nameConfig);
        // 将相同名称的频道合并，保留多个播放源（备用线路）
        processingChannels = this.doProcess(processingChannels, mergeConfig);
        // 测试测速，剔除超时或死链，按延迟对播放源排序
        processingChannels = this.doProcess(processingChannels, delayConfig);
        // 根据名称或标签分类，如 "央视频道", "卫视频道"
        processingChannels = this.doProcess(processingChannels, groupConfig);

        return processingChannels;
    }

    private List<Object> doProcess(List<Object> channels, List<EngineConfig> filterConfig) {
        List<Object> processingChannels = null;
        for (EngineConfig config : filterConfig) {
            CleaningEngine engine = factory.getEngine(config.getEngine());
            if (engine == null) continue;

            processingChannels = engine.process(channels, config.getConfigParams());
        }
        return processingChannels;
    }

}
