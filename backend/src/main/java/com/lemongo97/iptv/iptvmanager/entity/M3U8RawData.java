package com.lemongo97.iptv.iptvmanager.entity;

import java.time.LocalDateTime;

/**
 * M3U8 原始数据历史记录
 */
public record M3U8RawData(
        Long id,
        Long providerId,
        String content,
        LocalDateTime fetchedAt,
        Boolean deleted
) {
}
