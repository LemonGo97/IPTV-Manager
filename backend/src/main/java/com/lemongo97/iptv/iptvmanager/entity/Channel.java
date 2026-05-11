package com.lemongo97.iptv.iptvmanager.entity;

import java.time.LocalDateTime;

/**
 * 频道实体
 */
public record Channel(
        Long id,
        String name,
        String logo,
        String url,
        String number,
        String channelId,
        Boolean enabled,
        Long providerId,
        Long groupId,
        Long epgSourceId,
        Integer sortOrder,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Boolean deleted
) {
}
