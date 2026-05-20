package com.lemongo97.iptv.iptvmanager.engine;

import java.util.List;

public interface CleaningEngine {

    List<Object> process(List<Object> channels, String paramsJson);

}
