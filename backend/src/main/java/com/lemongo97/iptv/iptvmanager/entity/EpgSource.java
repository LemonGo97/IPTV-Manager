package com.lemongo97.iptv.iptvmanager.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * EPG 源
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EpgSource {
    private Long id;
    private String name;
    private String url;
    private String type;
    private Boolean enabled;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean deleted;
}
