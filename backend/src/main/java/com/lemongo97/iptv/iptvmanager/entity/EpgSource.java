package com.lemongo97.iptv.iptvmanager.entity;

import java.time.LocalDateTime;

/**
 * EPG 源
 */
public record EpgSource(
        Long id,
        String name,
        String url,
        String type,
        Boolean enabled,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Boolean deleted
) {
}
