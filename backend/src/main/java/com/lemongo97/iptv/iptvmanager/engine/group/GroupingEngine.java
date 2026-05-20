package com.lemongo97.iptv.iptvmanager.engine.group;

import com.lemongo97.iptv.iptvmanager.engine.CleaningEngine;
import lombok.Data;

import java.util.List;

/**
 * 分组
 */
@Data
public class GroupingEngine implements CleaningEngine {

    private String target;
    private Long groupId;

    @Override
    public List<Object> process(List<Object> channels, String paramsJson) {
        return List.of();
    }
}
