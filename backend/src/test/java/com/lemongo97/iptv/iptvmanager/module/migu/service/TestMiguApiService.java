package com.lemongo97.iptv.iptvmanager.module.migu.service;

import com.lemongo97.iptv.iptvmanager.module.migu.configutation.MiguModuleConfiguration;
import com.lemongo97.iptv.iptvmanager.module.migu.configutation.MiguModuleProperties;
import com.lemongo97.iptv.iptvmanager.module.migu.entity.LiveCategory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

@Slf4j
public class TestMiguApiService {

    MiguApiService miguApiService;
    MiguModuleProperties properties;

    @BeforeEach
    void init(){
        MiguModuleConfiguration moduleConfiguration = new MiguModuleConfiguration();
        this.properties = new MiguModuleProperties();
        this.miguApiService = moduleConfiguration.miguApiService(moduleConfiguration.okHttpClient(properties), properties);
    }

    @Test
    void test_fetchCateList(){
        List<LiveCategory> liveCategories = this.miguApiService.fetchCateList();
        log.info("{}", liveCategories);
    }

}
