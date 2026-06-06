package com.lemongo97.iptv.iptvmanager.cleanup.engine.group;

import com.lemongo97.iptv.iptvmanager.cleanup.engine.CleaningEngine;
import com.lemongo97.iptv.iptvmanager.entity.Channel;
import com.lemongo97.iptv.iptvmanager.utils.JSONUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Strings;

import java.util.List;

/**
 * 分组
 */
@Slf4j
@Data
public class GroupingEngine implements CleaningEngine {

    @Override
    public Channel process(Channel channel, String paramsJson) {
        log.debug("Channel cleanup: Processing GroupingEngine");
        GroupingEngineParam param = JSONUtil.fromJsonString(paramsJson, GroupingEngineParam.class);
        if (Strings.CI.contains(channel.getName(), param.getKeyword())){
            channel.setGroupId(param.getGroupId());
        }
        return channel;
    }

    @Data
    public static class GroupingEngineParam {
        private String keyword;
        private Long groupId;
    }
}
