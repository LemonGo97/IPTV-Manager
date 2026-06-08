package com.lemongo97.iptv.iptvmanager.module.migu.service;

import com.lemongo97.iptv.iptvmanager.module.migu.Constants;
import com.lemongo97.iptv.iptvmanager.module.migu.configutation.MiguModuleConfiguration;
import com.lemongo97.iptv.iptvmanager.module.migu.configutation.MiguModuleProperties;
import com.lemongo97.iptv.iptvmanager.module.migu.entity.LiveCategory;
import com.lemongo97.iptv.iptvmanager.module.migu.entity.LiveChannel;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import org.junit.jupiter.api.Assertions;
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
        List<LiveChannel> liveChannels = this.miguApiService.fetchChannelList();
        log.info("{}", liveChannels);
    }

    @Test
    void test_fetchRealChannelUrl(){
        String realChannelUrl = this.miguApiService.fetchRealChannelUrl("608807420");
        log.info("{}", realChannelUrl);
    }

    @Test
    void test_getddCalcu720p(){
        String res = MiguApiService.getddCalcu720p("b2e56ef6d7efaf8bef767b138317a81d", "608807420");
        log.info("{}", res);
        Assertions.assertEquals("db12v8eaa5y76a1e3f863d17be7f6a7ff8eb", res);
    }

    @Test
    void test_getClientId(){
        String clientId = MiguApiService.getClientId();
        log.info("{}", clientId);
    }
}
