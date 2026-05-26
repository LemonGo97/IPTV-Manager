package com.lemongo97.iptv.iptvmanager.cleanup.config;

import com.lemongo97.iptv.iptvmanager.cleanup.rule.RuleType;
import lombok.Data;

@Data
public class CleanupEngineConfig {
    private Long id;
    private RuleType ruleType;
    private String engine;
    private String configParams;
    private Integer sortOrder;
}
