package com.lemongo97.iptv.iptvmanager.entity;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * M3U8 源提供者
 */
public record M3U8Provider(
        Long id,
        String name,
        String type,
        String url,
        String filePath,
        Map<String, String> headers,
        Integer refreshRate,
        Boolean enabled,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Boolean deleted
) {
}
