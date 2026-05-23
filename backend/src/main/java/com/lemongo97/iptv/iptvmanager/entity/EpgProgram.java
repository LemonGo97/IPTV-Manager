package com.lemongo97.iptv.iptvmanager.entity;

import java.time.LocalDateTime;

/**
 * EPG 节目
 */
public record EpgProgram(
    Long id,
    Long epgSourceId,
    String channelId,
    String channelName,
    String startTime,
    String stopTime,
    String title,
    String subTitle,
    String description,
    String categories,  // JSON array string
    String icons,       // JSON array string
    String credits,     // JSON object string
    String ratings,     // JSON array string
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Boolean deleted
) {}
