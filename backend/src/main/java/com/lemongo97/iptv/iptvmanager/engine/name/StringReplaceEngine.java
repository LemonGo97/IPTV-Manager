package com.lemongo97.iptv.iptvmanager.engine.name;

import com.lemongo97.iptv.iptvmanager.engine.CleaningEngine;
import com.lemongo97.iptv.iptvmanager.entity.Channel;

import java.util.List;

public class StringReplaceEngine implements CleaningEngine {

    private String target;
    private String text;

    @Override
    public List<Channel> process(List<Channel> channels, String paramsJson) {
        return List.of();
    }
}
