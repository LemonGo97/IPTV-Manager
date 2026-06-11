package com.lemongo97.iptv.iptvmanager.service;

import com.lemongo97.iptv.iptvmanager.entity.Channel;
import com.lemongo97.iptv.iptvmanager.entity.IPTVProvider;
import com.lemongo97.iptv.iptvmanager.entity.IPTVProviderRawData;
import com.lemongo97.iptv.iptvmanager.entity.OriginalChannelMetadata;
import com.lemongo97.iptv.iptvmanager.mapper.ChannelMapper;
import com.lemongo97.iptv.iptvmanager.mapper.OriginalChannelMapper;
import com.lemongo97.iptv.iptvmanager.okhttp.exception.OkHttpRequestException;
import com.lemongo97.iptv.iptvmanager.parser.m3u8.IPTVM3U8Parser;
import com.lemongo97.iptv.iptvmanager.parser.txt.IPTVTXTParser;
import com.lemongo97.iptv.iptvmanager.utils.OkHttpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * IPTV 解析服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IPTVProviderContentParserService implements OriginalChannelCover{

    private final OriginalChannelMapper originalChannelMapper;
    private final ChannelMapper channelMapper;
    private final IPTVProviderRawDataService rawDataService;
    private final IPTVM3U8Parser IPTVM3U8Parser;
    private final IPTVTXTParser IPTVTXTParser;

    public int parse(IPTVProvider provider, Long taskId) {
        String content;
        switch (provider.getType()) {
            case online -> {
                content = getContentFromUrl(provider);
            }
            case file -> {
                content = getContentFromFile(provider);
            }
            default -> {
                throw new IllegalArgumentException("Unknown provider type: " + provider.getType());
            }
        }
        // 保存原始数据到历史记录
        rawDataService.saveRawData(provider.getId(), content);

        List<OriginalChannelMetadata> channels = this.parseFromContent(content, provider.getId(), provider.getContentType());
        originalChannelMapper.deleteByProviderId(provider.getId());
        if (channels.isEmpty()) return 0;

        originalChannelMapper.insert(channels, taskId);

        // 直接更新到频道表中
        List<Channel> converted = this.toChannel(channels);
        channelMapper.deleteByProviderId(provider.getId());
        channelMapper.insert(converted, 100);
        return channels.size();
    }

    /**
     * 从 URL 解析 IPTV
     *
     * @return 解析的频道数量
     */
    public String getContentFromUrl(IPTVProvider provider) {
        log.info("Parsing IPTV from URL: {}", provider.getUrl());

        OkHttpClient client = OkHttpUtil.getClient();

        Headers.Builder headerBuilder = new Headers.Builder();
        if (provider.getHeaders() != null) {
            provider.getHeaders().forEach(headerBuilder::add);
        }
        headerBuilder.set("Accept-Charset", StandardCharsets.UTF_8.name());

        try (Response response = client.newCall(new Request.Builder()
                .get()
                .url(provider.getUrl())
                .headers(headerBuilder.build()).build()).execute()){

            if (!response.isSuccessful()) {
                throw new OkHttpRequestException("Failed to fetch IPTV: HTTP " + response.code());
            }

            String content = response.body().string();
            if (StringUtils.isBlank(content)) {
                throw new RuntimeException("Failed to fetch IPTV: empty content from IPTV provider");
            }
            return content;
        } catch (Exception e) {
            log.error("Failed to parse IPTV from URL: {}", provider.getUrl(), e);
            throw new RuntimeException("Failed to parse IPTV from URL: " + e.getMessage(), e);
        }
    }

    /**
     * 从本地文件解析 IPTV
     *
     * @return 解析的频道数量
     */
    public String getContentFromFile(IPTVProvider provider) {
        log.info("Parsing IPTV from file: {}", provider.getFilename());

        try {
            IPTVProviderRawData providerRawData = rawDataService.getLatest(provider.getId());
            if (StringUtils.isBlank(providerRawData.getContent())) {
                throw new RuntimeException("Failed to parse IPTV from file: " + provider.getFilename());
            }
            return providerRawData.getContent();
        } catch (Exception e) {
            log.error("Failed to parse IPTV from file: {}", provider.getFilename(), e);
            throw new RuntimeException("Failed to parse IPTV from file: " + e.getMessage(), e);
        }
    }

    private List<OriginalChannelMetadata> parseFromContent(String content, Long providerId, IPTVProvider.ContentType contentType) {
        List<OriginalChannelMetadata> channels;
        switch (contentType) {
            case M3U8 -> {
                channels = IPTVM3U8Parser.parseContent(content, OriginalChannelMetadata.class);
            }
            case TXT -> {
                channels = IPTVTXTParser.parseContent(content, OriginalChannelMetadata.class);
            }
            default -> {
                channels = Collections.emptyList();
            }
        }

        // 使用简单解析器解析内容
        LocalDateTime now = LocalDateTime.now();
        channels.forEach(channel -> {
            channel.setProviderId(providerId);
            channel.setCreatedAt(now);
            channel.setUpdatedAt(now);
        });

        return channels;
    }

}
