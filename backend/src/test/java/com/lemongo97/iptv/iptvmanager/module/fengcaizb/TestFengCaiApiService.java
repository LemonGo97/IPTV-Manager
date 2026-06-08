package com.lemongo97.iptv.iptvmanager.module.fengcaizb;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

@Slf4j
public class TestFengCaiApiService {

    FengCaiApiService fengCaiApiService;
    FengCaiModuleProperties properties;

    @BeforeEach
    void init(){
        FengCaiModuleConfiguration moduleConfiguration = new FengCaiModuleConfiguration();
        this.properties = new FengCaiModuleProperties();
        this.fengCaiApiService = moduleConfiguration.fengCaiApiService(moduleConfiguration.okHttpClient(properties), properties);
    }

    @Test
    void test_fetchChannelList(){
        List<FengCaiChannel> channels = this.fengCaiApiService.fetchChannelList();
        log.info("{}", channels);
    }

    @Test
    void test_decryptChannelUrl(){
        String decrypted = this.fengCaiApiService.decryptChannelUrl("aU7jyGmG8Y8a26ePQcR8E9Eo3l3YncZZA8G3IPUuWcw=");
        log.info(decrypted);
    }

}
