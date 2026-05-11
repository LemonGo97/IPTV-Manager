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
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

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

    /**
     * 从 URL 解析 M3U8
     */
    @Transactional
    public void parseFromUrl(M3U8Provider provider) {
        log.info("Parsing M3U8 from URL: {}", provider.url());

        try {
            // 构建请求头
            HttpHeaders headers = new HttpHeaders();
            if (provider.headers() != null) {
                provider.headers().forEach(headers::add);
            }

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    provider.url(),
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String content = response.getBody();
                // 保存原始数据到历史记录（在解析前保存，确保即使解析失败也能保留原始数据）
                rawDataService.saveRawData(provider.id(), content);

                MultivariantPlaylist playlist = multivariantPlaylistParser.readPlaylist(content);
                int count = playlist.variants().size();
                parseAndSave(playlist, provider);
                log.info("Parsed {} channels from URL", count);
            } else {
                throw new RuntimeException("Failed to fetch M3U8: HTTP " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Failed to parse M3U8 from URL: {}", provider.url(), e);
            throw new RuntimeException("Failed to parse M3U8 from URL: " + e.getMessage(), e);
        }
    }

    /**
     * 从本地文件解析 M3U8
     */
    @Transactional
    public void parseFromFile(M3U8Provider provider) {
        log.info("Parsing M3U8 from file: {}", provider.filePath());

        try {
            Path filePath = Path.of(provider.filePath());
            if (!Files.exists(filePath)) {
                throw new RuntimeException("File not found: " + provider.filePath());
            }

            String content = Files.readString(filePath);
            // 保存原始数据到历史记录
            rawDataService.saveRawData(provider.id(), content);

            MultivariantPlaylist playlist = multivariantPlaylistParser.readPlaylist(content);
            parseAndSave(playlist, provider);
            int count = playlist.variants().size();
            log.info("Parsed {} channels from file", count);
        } catch (Exception e) {
            log.error("Failed to parse M3U8 from file: {}", provider.filePath(), e);
            throw new RuntimeException("Failed to parse M3U8 from file: " + e.getMessage(), e);
        }
    }

    /**
     * 解析并保存频道
     */
    private void parseAndSave(MultivariantPlaylist playlist, M3U8Provider provider) {
        try {
            int sortOrder = 0;

            for (Variant variant : playlist.variants()) {
                saveChannelFromVariant(variant, provider, sortOrder++);
            }

            log.info("Successfully saved {} channels from provider: {}", playlist.variants().size(), provider.name());
        } catch (Exception e) {
            log.error("Failed to parse M3U8 content", e);
            throw new RuntimeException("Failed to parse M3U8 content: " + e.getMessage(), e);
        }
    }

    /**
     * 从变体保存频道
     */
    private void saveChannelFromVariant(Variant variant, M3U8Provider provider, int sortOrder) {
        try {
            String name = extractChannelNameFromVariant(variant);

            var channel = new Channel(
                    null,
                    name,
                    null,
                    variant.uri(),
                    null,
                    null,
                    true,
                    provider.id(),
                    null,
                    null,
                    sortOrder,
                    null,
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    false
            );

            channelMapper.insert(channel);
            log.debug("Saved channel: {}", name);
        } catch (Exception e) {
            log.error("Failed to save channel: {}", variant.uri(), e);
        }
    }

    /**
     * 从变体中提取频道名称
     */
    private String extractChannelNameFromVariant(Variant variant) {
        String uri = variant.uri();
        if (uri != null) {
            int lastSlash = uri.lastIndexOf('/');
            if (lastSlash > 0) {
                return uri.substring(lastSlash + 1);
            }
            return uri;
        }

        return "Unknown Channel";
    }
}
