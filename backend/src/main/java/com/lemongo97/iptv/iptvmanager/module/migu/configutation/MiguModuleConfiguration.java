package com.lemongo97.iptv.iptvmanager.module.migu.configutation;

import com.lemongo97.iptv.iptvmanager.module.migu.service.MiguApiService;
import okhttp3.OkHttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MiguModuleProperties.class)
public class MiguModuleConfiguration {
    @Bean
    @ConditionalOnMissingBean(OkHttpClient.class)
    public OkHttpClient okHttpClient(MiguModuleProperties miguModuleProperties) {
        MiguModuleProperties.OkHttpClientConfiguration okhttp = miguModuleProperties.getOkhttp();
        return new OkHttpClient.Builder()
                .connectTimeout(okhttp.getConnectTimeout().getTimeout(), okhttp.getConnectTimeout().getUnit())
                .readTimeout(okhttp.getReadTimeout().getTimeout(), okhttp.getConnectTimeout().getUnit())
                .writeTimeout(okhttp.getWriteTimeout().getTimeout(), okhttp.getConnectTimeout().getUnit())
                .callTimeout(okhttp.getCallTimeout().getTimeout(), okhttp.getConnectTimeout().getUnit())
                .retryOnConnectionFailure(okhttp.isRetryOnConnectionFailure())
                .build();
    }

    @Bean
    public MiguApiService miguApiService(OkHttpClient okHttpClient, MiguModuleProperties properties) {
        return new MiguApiService(properties, okHttpClient);
    }
}
