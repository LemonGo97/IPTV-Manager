package com.lemongo97.iptv.iptvmanager.cleanup.engine.name;

import com.lemongo97.iptv.iptvmanager.cleanup.engine.CleaningEngine;
import com.lemongo97.iptv.iptvmanager.entity.Channel;
import com.lemongo97.iptv.iptvmanager.utils.JSONUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 大小写转换
 */
@Slf4j
@Data
public class CaseConversionEngine implements CleaningEngine {

    private Type input;
    private Type output;

    @Override
    public Channel process(Channel channel, String paramsJson) {
        log.debug("Channel cleanup: Processing CaseConversionEngine");
        CaseConversionEngineParam param = JSONUtil.fromJsonString(paramsJson, CaseConversionEngineParam.class);

        return channel.setName(param.cover(channel.getName()));
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
