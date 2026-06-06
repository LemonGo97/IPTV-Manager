package com.lemongo97.iptv.iptvmanager.cleanup.engine.delay;

import com.lemongo97.iptv.iptvmanager.cleanup.engine.CleaningEngine;
import com.lemongo97.iptv.iptvmanager.entity.Channel;

import java.util.List;

/**
 * 测算首帧延迟
 */
public class FFMpegCheckEngine implements CleaningEngine {
    @Override
    public Channel process(Channel channel, String paramsJson) {
        return channel;
    }
}
