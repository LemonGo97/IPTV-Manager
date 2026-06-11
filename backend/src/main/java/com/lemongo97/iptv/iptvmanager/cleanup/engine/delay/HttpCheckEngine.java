package com.lemongo97.iptv.iptvmanager.cleanup.engine.delay;

import com.lemongo97.iptv.iptvmanager.cleanup.engine.CleaningEngine;
import com.lemongo97.iptv.iptvmanager.entity.Channel;
import com.lemongo97.iptv.iptvmanager.utils.JSONUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.concurrent.TimeUnit;

@Slf4j
public class HttpCheckEngine implements CleaningEngine {

    private static final OkHttpClient okHttpClient =
            new OkHttpClient.Builder()
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .readTimeout(5, TimeUnit.SECONDS)
                    .writeTimeout(5, TimeUnit.SECONDS)
                    .callTimeout(5, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(false)
                    .build();

    @Override
    public Channel process(Channel channel, String paramsJson) {
        log.debug("Channel cleanup: Processing HttpCheckEngine");

        HttpCheckEngineParam param = JSONUtil.fromJsonString(paramsJson, HttpCheckEngineParam.class);

        if (channel.getStatus() == Channel.Status.invalid) {
            log.debug("Channel {} has status invalid, next", channel);
            return channel;
        }

        if (param.getType() == null) {
            return channel;
        }

        log.debug("开始使用 HTTP {} 检测 {} 的 URL链接: {}", param.getType(), channel.getName(), channel.getUrl());

        // 根据参数创建不同的请求
        Request.Builder builder;
        switch (param.getType()) {
            case GET -> builder = new Request.Builder().get();
            case HEAD -> builder = new Request.Builder().head();
            default -> builder = new Request.Builder().get();
        }

        try (Response response = okHttpClient
                .newCall(builder.header("User-Agent", "AptvPlayer/1.4.25").url(channel.getUrl()).build()).execute()) {

            long startTime = response.sentRequestAtMillis();
            long endTime = response.receivedResponseAtMillis();

            log.debug("请求耗时: {} 毫秒", (endTime - startTime));
            channel.setHttpDetectDelayMilliseconds((endTime - startTime));
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
