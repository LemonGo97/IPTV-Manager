package com.lemongo97.iptv.iptvmanager.cleanup.engine;

import com.lemongo97.iptv.iptvmanager.entity.Channel;

import java.util.List;

public interface CleaningEngine {

    List<Channel> process(List<Channel> channels, String paramsJson);

}
