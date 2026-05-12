package com.lemongo97.iptv.iptvmanager.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * M3U8 源提供者
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class M3U8Provider {
    private Long id;
    private String name;
    private String type;
    private String url;
    private String filePath;
    private Map<String, String> headers;
    private Integer refreshRate;
    private Boolean enabled;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean deleted;
}
