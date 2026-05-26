package com.lemongo97.iptv.iptvmanager.cleanup.engine.name;

import com.github.houbb.opencc4j.util.ZhConverterUtil;
import com.lemongo97.iptv.iptvmanager.cleanup.engine.CleaningEngine;
import com.lemongo97.iptv.iptvmanager.entity.Channel;
import com.lemongo97.iptv.iptvmanager.utils.JSONUtil;
import lombok.Data;

import java.util.List;

@Data
public class OpenCCEngine implements CleaningEngine {

    private Type input;
    private Type output;

    @Override
    public List<Channel> process(List<Channel> channels, String paramsJson) {
        OpenCCEngineParam param = JSONUtil.fromJsonString(paramsJson, OpenCCEngineParam.class);

        return channels.stream().peek(channel -> {
            String name = param.cover(channel.getName());
            channel.setName(name);
        }).toList();
    }

    @Data
    public static class OpenCCEngineParam {
        private Type input;
        private Type output;

        public String cover(String name){
            if (input == output) {
                return name;
            }
            if (output == Type.simple){
                return ZhConverterUtil.toSimple(name);
            }
            if (output == Type.traditional){
                return ZhConverterUtil.toTraditional(name);
            }
            return name;
        }
    }

    public enum Type {
        simple,
        traditional
    }
}
