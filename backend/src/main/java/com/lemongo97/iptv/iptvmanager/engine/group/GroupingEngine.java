package com.lemongo97.iptv.iptvmanager.engine.group;

import com.lemongo97.iptv.iptvmanager.engine.CleaningEngine;
import com.lemongo97.iptv.iptvmanager.entity.Channel;
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
    public List<Channel> process(List<Channel> channels, String paramsJson) {
        return List.of();
    }
}
