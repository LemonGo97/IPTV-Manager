package com.lemongo97.iptv.iptvmanager.cleanup.engine.name;

import com.lemongo97.iptv.iptvmanager.cleanup.engine.CleaningEngine;
import com.lemongo97.iptv.iptvmanager.entity.Channel;
import com.lemongo97.iptv.iptvmanager.utils.JSONUtil;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.util.List;

public class StringRemoveEngine implements CleaningEngine {

    @Override
    public List<Channel> process(List<Channel> channels, String paramsJson) {
        if (StringUtils.isBlank(paramsJson)) return channels;

        StringRemoveEngineParam param = JSONUtil.fromJsonString(paramsJson, StringRemoveEngineParam.class);

        // 根据是否忽略大小写确定使用的字符串引擎
        Strings strings = param.ignoreCase ? Strings.CI : Strings.CS;
        for (Channel channel : channels) {
            String name = channel.getName();

            // 移除配置的字符串
            for (String target : param.getTarget()) {
                name = strings.remove(name, target);
            }

            if (param.removeSpaces) {
                // 移除空格
                name = StringUtils.deleteWhitespace(name);
            }

            channel.setName(name);
        }
        return channels;
    }

    @Data
    public static class StringRemoveEngineParam {
        private boolean removeSpaces = true;
        private boolean ignoreCase = true;
        private List<String> target;
    }
}
