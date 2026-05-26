package com.lemongo97.iptv.iptvmanager.cleanup.engine.name;

import com.lemongo97.iptv.iptvmanager.cleanup.engine.CleaningEngine;
import com.lemongo97.iptv.iptvmanager.entity.Channel;
import com.lemongo97.iptv.iptvmanager.utils.JSONUtil;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class StringReplaceEngine implements CleaningEngine {

    @Override
    public List<Channel> process(List<Channel> channels, String paramsJson) {
        StringReplaceEngineParam param = JSONUtil.fromJsonString(paramsJson, StringReplaceEngineParam.class);
        for (Channel channel : channels) {
            String name = channel.getName();
            String replaced = StringUtils.replaceChars(name, param.getTarget(), param.getText());
            channel.setName(replaced);
        }
        return channels;
    }

    @Data
    public static class StringReplaceEngineParam {
        private String target;
        private String text;
    }
}
