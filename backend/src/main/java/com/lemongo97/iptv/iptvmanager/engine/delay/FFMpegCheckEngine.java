package com.lemongo97.iptv.iptvmanager.engine.delay;

import com.lemongo97.iptv.iptvmanager.engine.CleaningEngine;

import java.util.List;

/**
 * 测算首帧延迟
 */
public class FFMpegCheckEngine implements CleaningEngine {
    @Override
    public List<Object> process(List<Object> channels, String paramsJson) {
        return List.of();
    }
}
