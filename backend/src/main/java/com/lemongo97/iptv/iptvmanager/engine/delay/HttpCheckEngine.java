package com.lemongo97.iptv.iptvmanager.engine.delay;

import com.lemongo97.iptv.iptvmanager.engine.CleaningEngine;
import com.lemongo97.iptv.iptvmanager.entity.Channel;
import com.lemongo97.iptv.iptvmanager.utils.JSONUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.util.Timeout;

import java.util.List;

@Slf4j
public class HttpCheckEngine implements CleaningEngine {



    @Override
    public List<Channel> process(List<Channel> channels, String paramsJson) {

        HttpCheckEngineParam param = JSONUtil.fromJsonString(paramsJson, HttpCheckEngineParam.class);

        final Timeout timeout = Timeout.ofMilliseconds(param.getDelayMillisecond() == null ? 5000 : param.getDelayMillisecond());
        // 1. 创建连接管理器
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();

        // 2. 使用 ConnectionConfig 配置连接超时
        connectionManager.setDefaultConnectionConfig(ConnectionConfig.custom()
                .setConnectTimeout(timeout) // 替代了旧的写法
                .setSocketTimeout(timeout)
                .build());
        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .build()) {
            for (Channel channel : channels) {

                if (channel.getStatus() == Channel.Status.invalid){
                    log.debug("Channel {} has status invalid, next", channel);
                    continue;
                }

                log.debug("开始使用 HTTP {} 检测 {} 的 URL链接: {}", param.getType(), channel.getName(), channel.getUrl());
                try{
                    // 创建 HEAD 请求对象
                    ClassicHttpRequest request;

                    if (param.getType() == null) {
                        continue;
                    }

                    switch (param.getType()) {
                        case GET -> request = new HttpGet(channel.getUrl());
                        case HEAD -> request = new HttpHead(channel.getUrl());
                        default -> request = new HttpHead(channel.getUrl());
                    }

                    // 记录开始时间
                    long startTime = System.currentTimeMillis();

                    // 执行请求
                    httpClient.execute(request, response -> {
                        // 必须消耗掉响应内容，即使是 HEAD 请求
                        EntityUtils.consume(response.getEntity());
                        return null;
                    });

                    // 记录结束时间
                    long endTime = System.currentTimeMillis();
                    log.debug("请求耗时: {} 毫秒", (endTime - startTime));
                    channel.setHttpDetectDelayMilliseconds((endTime - startTime));
                }catch (Exception e){
                    channel.setStatus(Channel.Status.invalid);
                    log.error(e.getMessage(), e);
                    continue;
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return channels;
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
