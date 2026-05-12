package com.lemongo97.iptv.iptvmanager.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * M3U8 原始数据历史记录
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class M3U8RawData{
    private Long id;
    private Long providerId;
    private String content;
    private LocalDateTime fetchedAt;
    private Boolean deleted;
}
