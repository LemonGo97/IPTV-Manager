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
    public List<Channel> process(List<Channel> channels, String paramsJson) {

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

        // 2. 创建一个包含 20 个线程的线程池
        ExecutorService executor = Executors.newFixedThreadPool(20);

        // 3. 将 HttpClient 放入 try-with-resources 中
        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .build()) {

            // 4. 将每个 channel 的 HTTP 检测任务提交到线程池中异步运行
            List<CompletableFuture<Channel>> futures = channels.stream()
                    .map(channel -> CompletableFuture.supplyAsync(() -> {

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
                        return channel;
                    }, executor))
                    .toList();

            log.debug("所有 HTTP 检测任务已提交到线程池，正在进行 20 并发处理...");

            // 5. 组合所有任务，并阻塞等待 10,000 个请求全部结束
            CompletableFuture<Void> allCombinedTask = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0])
            );
            allCombinedTask.join();
            List<Channel> result = futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());
            log.debug("所有任务检测完毕，统一返回结果");
            return result;
        } catch (Exception e) {
            log.error("HttpClient 初始化或运行出错", e);
            return channels;
        } finally {
            // 6. 在所有任务结束后，手动关闭线程池，释放系统资源
            executor.shutdown();
        }
    }

//    @Override
    public List<Channel> processOld(List<Channel> channels, String paramsJson) {

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
