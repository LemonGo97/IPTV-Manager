package com.lemongo97.iptv.iptvmanager.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * IPTV 刷新任务历史
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IPTVProviderRefreshTask {
    private Long id;
    private Long providerId;
    private String providerName;
    private String triggerType;        // manual: 手动刷新; scheduled: 定时任务
    private String status;              // success: 成功; failed: 失败; running: 进行中
    private Long startTime;
    private Long endTime;
    private Long duration;              // 毫秒
    private Integer channelCount;       // 解析的频道数量
    private String errorMessage;
    private String rawContent;          // 原始 IPTV Provider 响应 内容（用于调试）
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
