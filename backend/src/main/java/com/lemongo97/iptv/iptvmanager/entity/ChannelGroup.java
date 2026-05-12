package com.lemongo97.iptv.iptvmanager.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 频道组
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChannelGroup {
    private Long id;
    private String name;
    private Integer sortOrder;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean deleted;
}
