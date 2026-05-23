package com.lemongo97.iptv.iptvmanager.engine.group;

import com.lemongo97.iptv.iptvmanager.engine.CleaningEngine;
import com.lemongo97.iptv.iptvmanager.entity.Channel;
import com.lemongo97.iptv.iptvmanager.utils.JSONUtil;
import lombok.Data;
import org.apache.commons.lang3.Strings;

import java.util.List;

/**
 * 分组
 */
@Data
public class GroupingEngine implements CleaningEngine {

    @Override
    public List<Channel> process(List<Channel> channels, String paramsJson) {
        GroupingEngineParam param = JSONUtil.fromJsonString(paramsJson, GroupingEngineParam.class);
        for (Channel channel : channels) {
            if (Strings.CI.contains(channel.getName(), param.getKeyword())){
                channel.setGroupId(param.getGroupId());
            }
        }
        return channels;
    }

    @Data
    public static class GroupingEngineParam {
        private String keyword;
        private Long groupId;
    }
}
