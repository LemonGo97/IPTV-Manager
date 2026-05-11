package com.lemongo97.iptv.iptvmanager.entity;

import java.time.LocalDateTime;

/**
 * 频道组
 */
public record ChannelGroup(
        Long id,
        String name,
        Integer sortOrder,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Boolean deleted
) {
}
