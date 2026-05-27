package com.lemongo97.iptv.iptvmanager.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 订阅用户
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DistributionUser {
    private Long id;
    private String username;
    private String userId;
    private String accessKey;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean deleted;
}
