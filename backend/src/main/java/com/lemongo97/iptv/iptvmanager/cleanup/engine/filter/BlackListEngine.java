package com.lemongo97.iptv.iptvmanager.cleanup.engine.filter;

import com.lemongo97.iptv.iptvmanager.cleanup.engine.CleaningEngine;
import com.lemongo97.iptv.iptvmanager.entity.Channel;
import com.lemongo97.iptv.iptvmanager.utils.JSONUtil;
import io.micrometer.common.util.StringUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Strings;

import java.util.List;

@Slf4j
@Data
public class BlackListEngine implements CleaningEngine {

    @Override
    public Channel process(Channel channel, String paramsJson) {
        log.debug("Channel cleanup: Processing BlackListEngine");
        BlackListEngineParam param = JSONUtil.fromJsonString(paramsJson, BlackListEngineParam.class);

        if (StringUtils.isBlank(channel.getName()) || param.containsMatch(channel.getName())) {
            log.debug("Ignoring blacklisted channel {}", channel.getName());
            return null;
        }

        return channel;
    }

    @Data
    public static class BlackListEngineParam {
        private List<String> keyword;

        public boolean containsMatch(String name){
            for (String s : keyword) {
                if (Strings.CI.contains(name, s)){
                    return true;
                }
            }
            return false;
        }
    }
}
