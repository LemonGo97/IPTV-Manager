package com.lemongo97.iptv.iptvmanager.module.migu.service;

import com.lemongo97.iptv.iptvmanager.module.migu.PlaylistType;
import com.lemongo97.iptv.iptvmanager.module.migu.configutation.MiguModuleProperties;
import com.lemongo97.iptv.iptvmanager.module.migu.entity.LiveChannel;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
public class MiguPlaylistService {

    private final MiguApiService apiService;
    private final MiguModuleProperties properties;

    @SuppressWarnings("DuplicateBranchesInSwitch")
    public String getPlayList(PlaylistType type) {
        List<LiveChannel> channels = apiService.fetchChannelList();
        switch (type) {
            case m3u8 -> {
                return toM3U8String(channels);
            }
            case txt -> {
                return toTXTString(channels);
            }
            default -> {
                return toM3U8String(channels);
            }
        }
    }

    String toM3U8String(Collection<LiveChannel> channels) {
        StringBuilder sb = new StringBuilder();
        this.appendM3U8Header(sb);

        for (LiveChannel channel : channels) {
            if (StringUtils.isBlank(channel.getPID())) continue;
            // output metadata
            this.appendChannelMetadata(channel, sb);
            // output url
            sb.append("/module/migu/play/").append(channel.getPID()).append("\n");
        }

        return sb.toString();
    }

    String toTXTString(Collection<LiveChannel> channels) {
        StringBuilder sb = new StringBuilder();
        Map<String, List<LiveChannel>> groupBy = channels.stream()
                .collect(Collectors.groupingBy(user -> {
                    // 如果分组是 null，就分到 "其他" 这一组
                    return StringUtils.isBlank(user.getCategoryName()) ? "其他" : user.getCategoryName();
                }));
        groupBy.forEach((group, channelList) -> {
            if (CollectionUtils.isEmpty(channelList)) return;

            sb.append(group).append(",#genre#").append('\n');
            channelList.forEach(channel -> {
                if (StringUtils.isBlank(channel.getPID())) return;
                sb.append(channel.getName()).append(',').append("/module/migu/play/").append("\n");
            });
            sb.append('\n');
        });

        return sb.toString();
    }

    private void appendChannelMetadata(LiveChannel channel, StringBuilder sb) {
        sb.append("#EXTINF:-1")
                .append(" tvg-name=")
                .append('"')
                .append(channel.getName())
                .append('"')
                .append(" tvg-logo=")
                .append('"')
                .append('"')
                .append(" group-title=")
                .append('"')
                .append(StringUtils.isNotBlank(channel.getCategoryName()) ? channel.getCategoryName() : "其他")
                .append('"')
                .append(',')
                .append(channel.getName())
                .append('\n');
    }

    private void appendM3U8Header(StringBuilder sb) {
        sb.append("#EXTM3U");

        List<String> epgProviders = properties.getEpgProvider();
        if (CollectionUtils.isNotEmpty(epgProviders)) {
            sb.append(" url-tvg=").append('"');
            for (int i = 0; i < epgProviders.size(); i++) {
                sb.append(epgProviders.get(i));
                if (i != epgProviders.size() - 1) {
                    sb.append(',');
                }
            }
            sb.append('"');
        }
        sb.append("\n\n");
    }

}
