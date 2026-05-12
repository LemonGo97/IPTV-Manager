package com.lemongo97.iptv.iptvmanager.entity;

import com.lemongo97.iptv.iptvmanager.m3u8.ChannelName;
import com.lemongo97.iptv.iptvmanager.m3u8.ChannelUrl;
import com.lemongo97.iptv.iptvmanager.m3u8.MetadataAttribute;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 原始的频道元数据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OriginalChannelMetadata {

    private Long id;

    private Long providerId;

    @ChannelName
    private String name;

    @ChannelUrl
    private String url;

    @MetadataAttribute("tvg-id")
    private String tvGuideId;

    @MetadataAttribute("tvg-name")
    private String tvGuideName;

    @MetadataAttribute("tvg-logo")
    private String tvGuideLogo;

    @MetadataAttribute("group-title")
    private String groupTitle;

    @MetadataAttribute("tvg-country")
    private String tvGuideCountry;

    @MetadataAttribute("tvg-language")
    private String tvGuideLanguage;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Boolean deleted;
}
