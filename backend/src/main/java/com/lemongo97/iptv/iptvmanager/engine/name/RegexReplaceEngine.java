package com.lemongo97.iptv.iptvmanager.engine.name;

import com.lemongo97.iptv.iptvmanager.engine.CleaningEngine;
import com.lemongo97.iptv.iptvmanager.entity.Channel;
import lombok.Data;

import java.util.List;

@Data
public class RegexReplaceEngine implements CleaningEngine {

    private String regex;
    private List<RegexReplaceGroupMatcher> groups;

    @Override
    public List<Channel> process(List<Channel> channels, String paramsJson) {
        return List.of();
    }

    @Data
    public static class RegexReplaceGroupMatcher {
        private Integer groupId;
        private String text;
    }
}
