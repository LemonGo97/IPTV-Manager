package com.lemongo97.iptv.iptvmanager.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 通用任务进度实体
 * 用于跟踪长时间运行的任务进度
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskProgress {
    private Long id;
    private String taskId;              // 任务唯一标识
    private String taskType;            // DATA_CLEANUP, M3U8_PARSE, HTTP_DETECT
    private Double progress;           // 0-100
    private Status status;              // RUNNING, SUCCESS, ERROR, NOT_RUNNING
    private String message;             // 状态消息
    private Integer totalItems;         // 总项目数
    private Integer processedItems;     // 已处理数
    private String startedAt;           // ISO 8601 timestamp
    private String completedAt;         // ISO 8601 timestamp
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 任务状态枚举
     */
    public enum Status {
        NOT_RUNNING,
        RUNNING,
        SUCCESS,
        ERROR;
    }
}
