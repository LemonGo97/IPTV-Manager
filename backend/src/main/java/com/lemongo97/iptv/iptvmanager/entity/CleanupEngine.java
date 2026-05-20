package com.lemongo97.iptv.iptvmanager.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CleanupEngine {

    private Long id;
    private String name;
    private String engine;
    private String ruleType;
    private String fullClassName;
    private String params;
    private String description;

}
