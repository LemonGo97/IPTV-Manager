package com.lemongo97.iptv.iptvmanager.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * IPTV 原始数据历史记录
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IPTVProviderRawData {
    private Long id;
    private Long providerId;
    private String content;
    private LocalDateTime fetchedAt;
    private Boolean deleted;
}
