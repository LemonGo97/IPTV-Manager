package com.lemongo97.iptv.iptvmanager.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 分发订阅
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DistributionSubscription {
    private Long id;
    private String name;
    private Long userId;
    private DateType dateType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean deleted;

    public enum DateType {
        /**
         * 一个月
         */
        MONTH,
        /**
         * 一个季度（三个月）
         */
        QUARTER,
        /**
         * 半年
         */
        HALF_YEAR,
        /**
         * 一年
         */
        YEAR,
        /**
         * 永久
         */
        FOREVER,
        /**
         * 自定义
         */
        CUSTOM
    }
}
