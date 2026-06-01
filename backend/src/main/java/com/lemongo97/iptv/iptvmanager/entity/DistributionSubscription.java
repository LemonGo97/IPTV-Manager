package com.lemongo97.iptv.iptvmanager.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

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
    // 高级设置
    private Boolean filterInvalidChannels;
    private Long filterHttpHighDelay;
    private Long filterFfmpegHighDelay;
    private Boolean filterNoVideoStream;
    private Boolean filterNoAudioStream;
    private String filterLowResolution;
    private Boolean mergeSameChannels;
    // 审计字段
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean deleted;
    private DistributionUser distributionUser;

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


    public boolean valid(){
        LocalDateTime now = LocalDateTime.now();
        // 检查开始时间
        if (this.getStartTime().isAfter(now)) {
            return false;
        }

        // 检查结束时间（null 表示永久）
        if (this.getEndTime() != null && this.getEndTime().isBefore(now)) {
            return false;
        }
        return true;
    }
}
