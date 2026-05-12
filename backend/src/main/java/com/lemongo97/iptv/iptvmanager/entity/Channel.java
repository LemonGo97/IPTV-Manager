package com.lemongo97.iptv.iptvmanager.entity;

import com.lemongo97.iptv.iptvmanager.m3u8.OriginalChannel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 频道实体
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class Channel {

    public Channel(OriginalChannel originalChannel, Long providerId, Integer sortOrder) {
        this.setName(originalChannel.getChannelName())
                .setLogo(originalChannel.getTvgLogo())
                .setUrl(originalChannel.getUrl())
                .setNumber(null)
                .setChannelId(originalChannel.getTvgId())
                .setEnabled(true)
                .setProviderId(providerId)
                .setGroupId(null)
                .setEpgSourceId(null)
                .setSortOrder(sortOrder)
                .setDescription(originalChannel.getGroupTitle())
                .setCreatedAt(LocalDateTime.now())
                .setUpdatedAt(LocalDateTime.now())
                .setDeleted(false);
    }

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
