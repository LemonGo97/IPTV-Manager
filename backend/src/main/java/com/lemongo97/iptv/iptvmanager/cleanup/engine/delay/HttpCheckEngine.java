package com.lemongo97.iptv.iptvmanager.cleanup.engine.delay;

import com.lemongo97.iptv.iptvmanager.cleanup.engine.CleaningEngine;
import com.lemongo97.iptv.iptvmanager.entity.Channel;
import com.lemongo97.iptv.iptvmanager.utils.JSONUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.util.Timeout;

import java.util.List;


import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.util.Timeout;

@Slf4j
public class HttpCheckEngine implements CleaningEngine {

    @Override
    public Channel process(Channel channel, String paramsJson) {
        log.debug("Channel cleanup: Processing HttpCheckEngine");

        HttpCheckEngineParam param = JSONUtil.fromJsonString(paramsJson, HttpCheckEngineParam.class);

        final Timeout timeout = Timeout.ofMilliseconds(param.getDelayMillisecond() == null ? 5000 : param.getDelayMillisecond());

        // 1. 创建连接管理器，并设置最大并发连接数
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setDefaultConnectionConfig(ConnectionConfig.custom()
                .setConnectTimeout(timeout)
                .setSocketTimeout(timeout)
                .build());
        // 关键配置：因为有 20 个线程同时请求，连接池的总连接数和单路由连接数也要支持 20 以上
        connectionManager.setMaxTotal(50);
        connectionManager.setDefaultMaxPerRoute(50);
        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .build()) {

            if (channel.getStatus() == Channel.Status.invalid) {
                log.debug("Channel {} has status invalid, next", channel);
                return channel;
            }

            if (param.getType() == null) {
                return channel;
            }

            log.debug("开始使用 HTTP {} 检测 {} 的 URL链接: {}", param.getType(), channel.getName(), channel.getUrl());
            try {
                // 根据参数创建不同的请求
                ClassicHttpRequest request;
                switch (param.getType()) {
                    case GET -> request = ClassicRequestBuilder.get(channel.getUrl()).build();
                    case HEAD -> request = ClassicRequestBuilder.head(channel.getUrl()).build();
                    default -> request = ClassicRequestBuilder.head(channel.getUrl()).build();
                }

                long startTime = System.currentTimeMillis();

                // 使用同一个 httpClient 实例执行请求
                httpClient.execute(request, response -> {
                    EntityUtils.consume(response.getEntity());
                    return null;
                });

                long endTime = System.currentTimeMillis();
                log.debug("请求耗时: {} 毫秒", (endTime - startTime));
                channel.setHttpDetectDelayMilliseconds((endTime - startTime));
            } catch (Exception e) {
                channel.setStatus(Channel.Status.invalid);
                log.error("检测失败，Channel: {}, 错误信息: {}", channel.getName(), e.getMessage());
            }

        } catch (Exception e) {
            channel.setStatus(Channel.Status.invalid);
            log.error("检测失败，Channel: {}, 错误信息: {}", channel.getName(), e.getMessage());
        }

        return channel;
    }

    public enum Type {
        GET,
        HEAD
    }


    @Data
    public static class HttpCheckEngineParam {
        /**
         * 检测方式
         */
        private Type type;

        /**
         * 延迟时间，高于此延迟时间的将被过滤
         */
        private Integer delayMillisecond;
    }
}
