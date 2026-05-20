package com.lemongo97.iptv.iptvmanager.engine.filter;

import com.lemongo97.iptv.iptvmanager.engine.CleaningEngine;
import lombok.Data;

import java.util.List;

@Data
public class BlackListEngine implements CleaningEngine {

    private List<String> keyword;

    @Override
    public List<Object> process(List<Object> channels, String paramsJson) {
        return List.of();
    }
}
