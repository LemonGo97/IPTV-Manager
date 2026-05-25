package com.lemongo97.iptv.iptvmanager.config;

import com.lemongo97.iptv.iptvmanager.engine.CleanEngineManager;
import com.lemongo97.iptv.iptvmanager.engine.CleaningEngineFactory;
import com.lemongo97.iptv.iptvmanager.engine.delay.FFMpegCheckEngine;
import com.lemongo97.iptv.iptvmanager.engine.delay.FFProbeCheckEngine;
import com.lemongo97.iptv.iptvmanager.engine.delay.HttpCheckEngine;
import com.lemongo97.iptv.iptvmanager.engine.filter.BlackListEngine;
import com.lemongo97.iptv.iptvmanager.engine.group.GroupingEngine;
import com.lemongo97.iptv.iptvmanager.engine.name.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AutoConfiguration {

    @Bean
    public CleaningEngineFactory cleaningEngineFactory() {
        CleaningEngineFactory cleaningEngineFactory = new CleaningEngineFactory();
        cleaningEngineFactory.addEngine("FFMpegCheckEngine", new FFMpegCheckEngine());
        cleaningEngineFactory.addEngine("FFProbeCheckEngine", new FFProbeCheckEngine());
        cleaningEngineFactory.addEngine("HttpCheckEngine", new HttpCheckEngine());

        cleaningEngineFactory.addEngine("BlackListEngine", new BlackListEngine());

        cleaningEngineFactory.addEngine("GroupingEngine", new GroupingEngine());

        cleaningEngineFactory.addEngine("CaseConversionEngine", new CaseConversionEngine());
        cleaningEngineFactory.addEngine("OpenCCEngine", new OpenCCEngine());
        cleaningEngineFactory.addEngine("RegexReplaceEngine", new RegexReplaceEngine());
        cleaningEngineFactory.addEngine("StringReplaceEngine", new StringReplaceEngine());
        cleaningEngineFactory.addEngine("StringRemoveEngine", new StringRemoveEngine());
        return cleaningEngineFactory;
    }

    @Bean
    public CleanEngineManager cleanEngineManager(CleaningEngineFactory cleaningEngineFactory) {
        return new CleanEngineManager(cleaningEngineFactory);
    }

}