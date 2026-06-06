package com.lemongo97.iptv.iptvmanager.cleanup.engine.name;

import com.lemongo97.iptv.iptvmanager.cleanup.engine.CleaningEngine;
import com.lemongo97.iptv.iptvmanager.entity.Channel;
import com.lemongo97.iptv.iptvmanager.utils.JSONUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Slf4j
public class StringReplaceEngine implements CleaningEngine {

    @Override
    public Channel process(Channel channel, String paramsJson) {
        log.debug("Channel cleanup: Processing StringReplaceEngine");
        StringReplaceEngineParam param = JSONUtil.fromJsonString(paramsJson, StringReplaceEngineParam.class);

        return channel.setName(
                StringUtils.replaceChars(channel.getName(), param.getTarget(), param.getText()));
    }

    @Data
    public static class StringReplaceEngineParam {
        private String target;
        private String text;
    }
}
