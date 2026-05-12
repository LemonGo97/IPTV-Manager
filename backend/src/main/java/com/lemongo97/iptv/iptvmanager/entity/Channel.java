package com.lemongo97.iptv.iptvmanager.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 频道实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Channel {
    private Long id;
    private String name;
    private String logo;
    private String url;
    private String number;
    private String channelId;
    private Boolean enabled;
    private Long providerId;
    private Long groupId;
    private Long epgSourceId;
    private Integer sortOrder;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean deleted;
}
