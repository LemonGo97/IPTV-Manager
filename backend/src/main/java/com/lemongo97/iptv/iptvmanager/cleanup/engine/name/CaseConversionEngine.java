package com.lemongo97.iptv.iptvmanager.cleanup.engine.name;

import com.lemongo97.iptv.iptvmanager.cleanup.engine.CleaningEngine;
import com.lemongo97.iptv.iptvmanager.entity.Channel;
import com.lemongo97.iptv.iptvmanager.utils.JSONUtil;
import lombok.Data;

import java.util.List;

/**
 * 大小写转换
 */
@Data
public class CaseConversionEngine implements CleaningEngine {

    private Type input;
    private Type output;

    @Override
    public List<Channel> process(List<Channel> channels, String paramsJson) {

        CaseConversionEngineParam param = JSONUtil.fromJsonString(paramsJson, CaseConversionEngineParam.class);

        return channels.stream().peek(channel -> {
            String name = param.cover(channel.getName());
            channel.setName(name);
        }).toList();
    }

    @Data
    public static class CaseConversionEngineParam {
        private Type input;
        private Type output;

        public String cover(String name){
            if (input == output) {
                return name;
            }
            if (output == Type.lowercase){
                return name.toLowerCase();
            }
            if (output == Type.uppercase){
                return name.toUpperCase();
            }
            return name;
        }
    }

    public enum Type{
        uppercase,
        lowercase
    }
}
