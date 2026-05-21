package com.lemongo97.iptv.iptvmanager.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 数据清洗规则
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CleanupRule {

    private Long id;
    private String name;
    private String engine;
    private String ruleType;
    private Boolean enabled;
    private String params;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean deleted;
}
