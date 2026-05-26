package com.lemongo97.iptv.iptvmanager.cleanup.engine.filter;

import com.lemongo97.iptv.iptvmanager.cleanup.engine.CleaningEngine;
import com.lemongo97.iptv.iptvmanager.entity.Channel;
import com.lemongo97.iptv.iptvmanager.utils.JSONUtil;
import lombok.Data;
import org.apache.commons.lang3.Strings;

import java.util.List;

@Data
public class BlackListEngine implements CleaningEngine {

    @Override
    public List<Channel> process(List<Channel> channels, String paramsJson) {
        BlackListEngineParam param = JSONUtil.fromJsonString(paramsJson, BlackListEngineParam.class);
        return channels.stream().filter(channel -> !param.containsMatch(channel.getName())).toList();
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
