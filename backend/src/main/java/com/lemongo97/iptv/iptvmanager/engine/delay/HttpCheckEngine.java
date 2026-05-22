package com.lemongo97.iptv.iptvmanager.engine.delay;

import com.lemongo97.iptv.iptvmanager.engine.CleaningEngine;
import com.lemongo97.iptv.iptvmanager.entity.Channel;

import java.util.List;

public class HttpCheckEngine implements CleaningEngine {

    /**
     * 检测方式
     */
    private Type type;

    /**
     * 延迟时间，高于此延迟时间的将被过滤
     */
    private Integer delayMinutes;

    @Override
    public List<Channel> process(List<Channel> channels, String paramsJson) {
        return List.of();
    }

    public enum Type {
        GET,
        HEAD
    }
}
