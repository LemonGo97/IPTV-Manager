package com.lemongo97.iptv.iptvmanager.cleanup.engine.name;

import com.github.houbb.opencc4j.util.ZhConverterUtil;
import com.lemongo97.iptv.iptvmanager.cleanup.engine.CleaningEngine;
import com.lemongo97.iptv.iptvmanager.entity.Channel;
import com.lemongo97.iptv.iptvmanager.utils.JSONUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Data
public class OpenCCEngine implements CleaningEngine {

    private Type input;
    private Type output;

    @Override
    public Channel process(Channel channel, String paramsJson) {
        log.debug("Channel cleanup: Processing OpenCCEngine");
        OpenCCEngineParam param = JSONUtil.fromJsonString(paramsJson, OpenCCEngineParam.class);

        return channel.setName(param.cover(channel.getName()));
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
