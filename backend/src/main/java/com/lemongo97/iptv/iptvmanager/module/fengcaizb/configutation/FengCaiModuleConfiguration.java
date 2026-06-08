package com.lemongo97.iptv.iptvmanager.module.fengcaizb.configutation;

import com.lemongo97.iptv.iptvmanager.module.fengcaizb.FengCaiApiService;
import com.lemongo97.iptv.iptvmanager.module.fengcaizb.FengCaiLiveService;
import okhttp3.OkHttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(FengCaiModuleProperties.class)
public class FengCaiModuleConfiguration {
    @Bean
    @ConditionalOnMissingBean(OkHttpClient.class)
    public OkHttpClient okHttpClient(FengCaiModuleProperties fengCaiModuleProperties) {
        FengCaiModuleProperties.OkHttpClientConfiguration okhttp = fengCaiModuleProperties.getOkhttp();
        return new OkHttpClient.Builder()
                .connectTimeout(okhttp.getConnectTimeout().getTimeout(), okhttp.getConnectTimeout().getUnit())
                .readTimeout(okhttp.getReadTimeout().getTimeout(), okhttp.getConnectTimeout().getUnit())
                .writeTimeout(okhttp.getWriteTimeout().getTimeout(), okhttp.getConnectTimeout().getUnit())
                .callTimeout(okhttp.getCallTimeout().getTimeout(), okhttp.getConnectTimeout().getUnit())
                .retryOnConnectionFailure(okhttp.isRetryOnConnectionFailure())
                .build();
    }

    @Bean
    public FengCaiApiService fengCaiApiService(OkHttpClient okHttpClient, FengCaiModuleProperties properties) {
        return new FengCaiApiService(properties, okHttpClient);
    }

    @Bean
    public FengCaiLiveService fengCaiLiveService(FengCaiApiService fengCaiApiService, FengCaiModuleProperties properties) {
        return new FengCaiLiveService(fengCaiApiService, properties);
    }
}
