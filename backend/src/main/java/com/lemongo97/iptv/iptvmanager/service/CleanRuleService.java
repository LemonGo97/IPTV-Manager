package com.lemongo97.iptv.iptvmanager.service;

import com.lemongo97.iptv.iptvmanager.engine.CleanUpRuleParam;
import com.lemongo97.iptv.iptvmanager.entity.CleanupEngine;
import com.lemongo97.iptv.iptvmanager.mapper.CleanupEngineMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
@AllArgsConstructor
public class CleanRuleService {

    private final CleanupEngineMapper cleanupEngineMapper;

    public List<CleanupEngine> getEngineList(){
        return cleanupEngineMapper.findAll();
    }

    public static void main(String[] args) {
        CleanRuleService cleanRuleService = new CleanRuleService(null);
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(cleanRuleService.caseCover());
        System.out.println(json);
    }

    private List<CleanUpRuleParam> blacklist() {
        return List.of(new CleanUpRuleParam.DynamicInputParam("keyword", "关键字"));
    }

    private List<CleanUpRuleParam> ffprobeFilter() {
        return List.of(
                new CleanUpRuleParam.NumberParam("delayMinutes", "最高延迟时间"),
                new CleanUpRuleParam.SwitchParam("discardNoVideo", "丢弃无视频"),
                new CleanUpRuleParam.SwitchParam("discardNoAudio", "丢弃无音频"),
                new CleanUpRuleParam.NumberParam("minVideoFrameWidth", "最小视频帧宽度"),
                new CleanUpRuleParam.NumberParam("minVideoFrameHeight", "最小视频帧高度")
        );
    }

    public List<CleanUpRuleParam> opencc() {

        List<CleanUpRuleParam.SelectParam.SelectParamOption> options = List.of(
                new CleanUpRuleParam.SelectParam.SelectParamOption("simple", "简体"),
                new CleanUpRuleParam.SelectParam.SelectParamOption("traditional", "繁体")
        );

        CleanUpRuleParam.SelectParam input = new CleanUpRuleParam.SelectParam("input", "输入语言");
        input.setOptions(options);

        CleanUpRuleParam.SelectParam output = new CleanUpRuleParam.SelectParam("output", "输出语言");
        output.setOptions(options);
        return List.of(
                input, output
        );
    }

    public List<CleanUpRuleParam> caseCover() {

        List<CleanUpRuleParam.SelectParam.SelectParamOption> options = List.of(
                new CleanUpRuleParam.SelectParam.SelectParamOption("uppercase", "简体"),
                new CleanUpRuleParam.SelectParam.SelectParamOption("lowercase", "繁体")
        );

        CleanUpRuleParam.SelectParam input = new CleanUpRuleParam.SelectParam("input", "输入");
        input.setOptions(options);

        CleanUpRuleParam.SelectParam output = new CleanUpRuleParam.SelectParam("output", "输出");
        output.setOptions(options);
        return List.of(
                input, output
        );
    }

    public List<CleanUpRuleParam> regex() {
        CleanUpRuleParam.DynamicInputPairParam dynamicInputPairParam = new CleanUpRuleParam.DynamicInputPairParam("groups", "分组替换设置");
        dynamicInputPairParam.setKeyField("groupId");
        dynamicInputPairParam.setKeyPlaceholder("分组ID");
        dynamicInputPairParam.setValueField("text");
        dynamicInputPairParam.setValuePlaceholder("替换值");
        return List.of(
                new CleanUpRuleParam.InputParam("regex", "正则表达式"),
                dynamicInputPairParam
        );
    }

    public List<CleanUpRuleParam> string() {
        return List.of(
                new CleanUpRuleParam.InputParam("target", "匹配值"),
                new CleanUpRuleParam.InputParam("text", "替换文字")
        );
    }
    public List<CleanUpRuleParam> http() {

        List<CleanUpRuleParam.SelectParam.SelectParamOption> options = List.of(
                new CleanUpRuleParam.SelectParam.SelectParamOption("GET", "GET"),
                new CleanUpRuleParam.SelectParam.SelectParamOption("HEAD", "HEAD")
        );

        CleanUpRuleParam.SelectParam input = new CleanUpRuleParam.SelectParam("type", "检测方式");
        input.setOptions(options);
        return List.of(
                input,
                new CleanUpRuleParam.NumberParam("delayMinutes", "最大延迟时间")
        );
    }
}
