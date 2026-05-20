package com.lemongo97.iptv.iptvmanager.engine;

import java.util.HashMap;
import java.util.Map;

public class CleaningEngineFactory {

    private final Map<String, CleaningEngine> engines = new HashMap<>();

    public CleaningEngineFactory addEngine(String name, CleaningEngine engine) {
        engines.put(name, engine);
        return this;
    }

    public CleaningEngine getEngine(String engine){
        return engines.get(engine);
    }

}
