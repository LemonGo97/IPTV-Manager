package com.lemongo97.iptv.iptvmanager.engine;

import lombok.Data;

@Data
public class EngineConfig {
    private Long id;
    private RuleType ruleType;
    private String engine;
    private String configParams;
    private Integer sortOrder;
}
