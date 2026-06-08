package com.lemongo97.iptv.iptvmanager.module.fengcaizb;

import com.lemongo97.iptv.iptvmanager.module.fengcaizb.configutation.FengCaiModuleProperties;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
public class FengCaiLiveService {

    private final FengCaiApiService apiService;
    private final FengCaiModuleProperties properties;

    @SuppressWarnings("DuplicateBranchesInSwitch")
    public String getPlayList(PlaylistType type) {
        List<FengCaiChannel> channels = apiService.fetchChannelList();
        switch (type) {
            case m3u8 -> {
                return toM3U8String(channels);
            }
            case m3u8_multi_line -> {
                return toM3U8MultiLineString(channels);
            }
            case txt -> {
                return toTXTString(channels);
            }
            default -> {
                return toM3U8String(channels);
            }
        }
    }

    /**
     * 多线路支持
     */
    String toM3U8MultiLineString(Collection<FengCaiChannel> channels) {
        StringBuilder sb = new StringBuilder();
        this.appendM3U8Header(sb);

        for (FengCaiChannel channel : channels) {
            if (CollectionUtils.isEmpty(channel.getUrls())) continue;
            // output metadata
            this.appendChannelMetadata(channel, sb);
            // output url
            channel.getUrls().forEach(url -> {
                sb.append(url).append("\n");
            });
        }

        return sb.toString();
    }

    String toM3U8String(Collection<FengCaiChannel> channels) {
        StringBuilder sb = new StringBuilder();
        this.appendM3U8Header(sb);

        for (FengCaiChannel channel : channels) {
            if (CollectionUtils.isEmpty(channel.getUrls())) continue;

            channel.getUrls().forEach(url -> {
                // output metadata
                this.appendChannelMetadata(channel, sb);

                // output url
                sb.append(url).append("\n");
            });
        }

        return sb.toString();
    }

    private void appendChannelMetadata(FengCaiChannel channel, StringBuilder sb) {
        sb.append("#EXTINF:-1")
                .append(" tvg-name=")
                .append('"')
                .append(channel.getTitle())
                .append('"')
                .append(" tvg-logo=")
                .append('"')
                .append('"')
                .append(" group-title=")
                .append('"')
                .append(StringUtils.isNotBlank(channel.getGroup()) ? channel.getGroup() : "其他")
                .append('"')
                .append(',')
                .append(channel.getTitle())
                .append('\n');
    }


    String toTXTString(Collection<FengCaiChannel> channels) {
        StringBuilder sb = new StringBuilder();
        Map<String, List<FengCaiChannel>> groupBy = channels.stream()
                .collect(Collectors.groupingBy(user -> {
                    // 如果分组是 null，就分到 "其他" 这一组
                    return StringUtils.isBlank(user.getGroup()) ? "其他" : user.getGroup();
                }));
        groupBy.forEach((group, channelList) -> {
            if (CollectionUtils.isEmpty(channelList)) return;

            sb.append(group).append(",#genre#").append('\n');
            channelList.forEach(channel -> {
                if (CollectionUtils.isEmpty(channel.getUrls())) return;

                channel.getUrls().forEach(url -> {
                    sb.append(channel.getTitle()).append(',').append(url).append("\n");
                });
            });
            sb.append('\n');
        });

        return sb.toString();
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
