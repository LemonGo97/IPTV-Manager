package com.lemongo97.iptv.iptvmanager.parser;

import com.lemongo97.iptv.iptvmanager.entity.Channel;
import com.lemongo97.iptv.iptvmanager.entity.M3U8Provider;
import com.lemongo97.iptv.iptvmanager.mapper.ChannelMapper;
import com.lemongo97.iptv.iptvmanager.service.M3U8RawDataService;
import io.lindstrom.m3u8.model.MultivariantPlaylist;
import io.lindstrom.m3u8.model.Variant;
import io.lindstrom.m3u8.parser.MultivariantPlaylistParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * M3U8 解析服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class M3U8ParserService {

    private final ChannelMapper channelMapper;
    private final RestTemplate restTemplate;
    private final M3U8RawDataService rawDataService;
    private final MultivariantPlaylistParser multivariantPlaylistParser = new MultivariantPlaylistParser();
    private final SimpleM3U8Parser simpleM3U8Parser;

    public int parse(M3U8Provider provider) {
        String content;
        switch (provider.getType()) {
            case "online" -> {
                content = getContentFromUrl(provider);
            }
            case "local" -> {
                content = getContentFromFile(provider);
            }
            default -> {
                throw new IllegalArgumentException("Unknown provider type: " + provider.getType());
            }
        }
        // 保存原始数据到历史记录
        rawDataService.saveRawData(provider.getId(), content);

        List<Channel> channels = this.parseFromContent(content, provider.getId());
        channelMapper.insertBatch(channels);
        return channels.size();
    }

    /**
     * 从 URL 解析 M3U8
     *
     * @return 解析的频道数量
     */
    public String getContentFromUrl(M3U8Provider provider) {
        log.info("Parsing M3U8 from URL: {}", provider.getUrl());

        try {
            // 构建请求头
            HttpHeaders headers = new HttpHeaders();
            if (provider.getHeaders() != null) {
                provider.getHeaders().forEach(headers::add);
            }
            headers.setAcceptCharset(Collections.singletonList(StandardCharsets.UTF_8));

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    provider.getUrl(),
                    HttpMethod.GET,
                    requestEntity,
                    byte[].class
            );

            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                throw new RuntimeException("Failed to fetch M3U8: HTTP " + response.getStatusCode());
            }

            return new String(response.getBody(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Failed to parse M3U8 from URL: {}", provider.getUrl(), e);
            throw new RuntimeException("Failed to parse M3U8 from URL: " + e.getMessage(), e);
        }
    }

    /**
     * 从本地文件解析 M3U8
     *
     * @return 解析的频道数量
     */
    @Transactional
    public String getContentFromFile(M3U8Provider provider) {
        log.info("Parsing M3U8 from file: {}", provider.getFilePath());

        try {
            File file = new File(provider.getFilePath());
            if (!file.exists()) {
                throw new RuntimeException("File not found: " + provider.getFilePath());
            }
            return FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Failed to parse M3U8 from file: {}", provider.getFilePath(), e);
            throw new RuntimeException("Failed to parse M3U8 from file: " + e.getMessage(), e);
        }
    }

    private List<Channel> parseFromContent(String content, Long providerId) {
        // 使用简单解析器解析内容
        var channels = simpleM3U8Parser.parseContent(content);
        AtomicInteger sortOrder = new AtomicInteger();
        return channels.stream().map(c -> new Channel(
                null,                   // id
                c.name(),          // name
                c.logo(),          // logo
                c.url(),           // url
                null,                   // number (not available in M3U8)
                c.tvgId(),         // channelId (tvg-id from M3U8)
                true,                   // enabled
                providerId,             // providerId
                null,                   // groupId (will be set by group matching later if needed)
                null,                   // epgSourceId (not available in M3U8)
                sortOrder.getAndIncrement(),            // sortOrder
                c.group(),         // description (store group-title in description)
                LocalDateTime.now(),    // createdAt
                LocalDateTime.now(),    // updatedAt
                false                   // deleted
        )).toList();
    }

}
