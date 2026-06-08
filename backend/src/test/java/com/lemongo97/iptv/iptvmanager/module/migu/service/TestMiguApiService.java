package com.lemongo97.iptv.iptvmanager.module.migu.service;

import com.lemongo97.iptv.iptvmanager.module.migu.configutation.MiguModuleConfiguration;
import com.lemongo97.iptv.iptvmanager.module.migu.configutation.MiguModuleProperties;
import com.lemongo97.iptv.iptvmanager.module.migu.entity.LiveCategory;
import com.lemongo97.iptv.iptvmanager.module.migu.entity.MiguChannel;
import com.lemongo97.iptv.iptvmanager.module.migu.service.MiguApiService;
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

    @Test
    void test_fetchChannelList(){
        List<MiguChannel> channels = this.miguApiService.fetchChannelList();
        log.info("{}", channels);
    }

    @Test
    void test_decryptChannelUrl(){
        String decrypted = this.miguApiService.decryptChannelUrl("aU7jyGmG8Y8a26ePQcR8E9Eo3l3YncZZA8G3IPUuWcw=");
        log.info(decrypted);
    }

}
