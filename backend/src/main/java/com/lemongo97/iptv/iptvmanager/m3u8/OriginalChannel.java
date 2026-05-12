package com.lemongo97.iptv.iptvmanager.m3u8;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 频道实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OriginalChannel {
    @ChannelName
    private String channelName;

    @ChannelUrl
    private String url;

    @MetadataAttribute("tvg-id")
    private String tvgId;

    @MetadataAttribute("tvg-name")
    private String tvgName;

    @MetadataAttribute("tvg-logo")
    private String tvgLogo;

    @MetadataAttribute("group-title")
    private String groupTitle;

    @MetadataAttribute("tvg-country")
    private String tvgCountry;

    @MetadataAttribute("tvg-language")
    private String tvgLanguage;
}
