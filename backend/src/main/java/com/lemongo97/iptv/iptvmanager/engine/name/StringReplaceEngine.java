package com.lemongo97.iptv.iptvmanager.engine.name;

import com.lemongo97.iptv.iptvmanager.engine.CleaningEngine;

import java.util.List;

public class StringReplaceEngine implements CleaningEngine {

    private String target;
    private String text;

    @Override
    public List<Object> process(List<Object> channels, String paramsJson) {
        return List.of();
    }
}
