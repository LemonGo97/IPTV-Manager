package com.lemongo97.iptv.iptvmanager.dto;

import com.lemongo97.iptv.iptvmanager.entity.M3U8RefreshTask;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * M3U8 刷新任务 DTO，包含通过关联查询获取的订阅源名称
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class M3U8RefreshTaskDTO {
    private Long id;
    private Long providerId;
    private String providerName;        // 通过关联查询获取
    private String triggerType;
    private String status;
    private Long startTime;
    private Long endTime;
    private Long duration;
    private Integer channelCount;
    private String errorMessage;
    private String rawContent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 从实体转换为 DTO（不包含 providerName）
     */
    public static M3U8RefreshTaskDTO fromEntity(M3U8RefreshTask task) {
        return new M3U8RefreshTaskDTO(
            task.getId(),
            task.getProviderId(),
            null, // providerName 需要通过数据库查询获取
            task.getTriggerType(),
            task.getStatus(),
            task.getStartTime(),
            task.getEndTime(),
            task.getDuration(),
            task.getChannelCount(),
            task.getErrorMessage(),
            task.getRawContent(),
            task.getCreatedAt(),
            task.getUpdatedAt()
        );
    }
}
