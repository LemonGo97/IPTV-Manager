package com.lemongo97.iptv.iptvmanager.service;

import com.github.houbb.opencc4j.util.ZhConverterUtil;
import com.lemongo97.iptv.iptvmanager.entity.Channel;
import com.lemongo97.iptv.iptvmanager.entity.OriginalChannelMetadata;

import java.util.Collection;
import java.util.List;

public interface OriginalChannelCover {

    default List<Channel> toChannel(Collection<OriginalChannelMetadata> original) {
        return original.stream()
                .map(o -> {

                    boolean tvgNameZh = ZhConverterUtil.containsChinese(o.getTvGuideName());
                    boolean tvgIdZh = ZhConverterUtil.containsChinese(o.getTvGuideId());
                    boolean cname = ZhConverterUtil.containsChinese(o.getName());

                    String name;
                    if (tvgNameZh){
                        name = o.getTvGuideName();
                    } else {
                        if (tvgIdZh && cname){
                            name = o.getTvGuideId();
                        }else {
                            name = o.getName();
                        }
                    }

                    return new Channel()
                            .setName(name)
                            .setLogo(o.getTvGuideLogo())
                            .setUrl(o.getUrl())
                            .setProviderId(o.getProviderId())
                            .setGroupId(null)
                            .setEpgSourceId(o.getTvGuideId())
                            .setStatus(Channel.Status.unknown)
                            .setCountry(o.getTvGuideCountry())
                            .setLanguage(o.getTvGuideLanguage())
                            .setScore(0d)
                            .setCreatedAt(o.getCreatedAt())
                            .setUpdatedAt(o.getUpdatedAt());
                })
                .toList();
    }
}
