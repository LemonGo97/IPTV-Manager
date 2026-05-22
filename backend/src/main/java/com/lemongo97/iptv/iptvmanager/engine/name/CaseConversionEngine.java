package com.lemongo97.iptv.iptvmanager.engine.name;

import com.lemongo97.iptv.iptvmanager.engine.CleaningEngine;
import com.lemongo97.iptv.iptvmanager.entity.Channel;
import lombok.Data;

import java.util.List;

/**
 * 大小写转换
 */
@Data
public class CaseConversionEngine implements CleaningEngine {

    private Type input;
    private Type output;

    @Override
    public List<Channel> process(List<Channel> channels, String paramsJson) {
        return List.of();
    }

    public enum Type{
        uppercase,
        lowercase
    }
}
