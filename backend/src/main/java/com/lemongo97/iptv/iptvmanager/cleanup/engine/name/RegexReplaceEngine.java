package com.lemongo97.iptv.iptvmanager.cleanup.engine.name;

import com.lemongo97.iptv.iptvmanager.cleanup.engine.CleaningEngine;
import com.lemongo97.iptv.iptvmanager.entity.Channel;
import com.lemongo97.iptv.iptvmanager.utils.JSONUtil;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class RegexReplaceEngine implements CleaningEngine {

    @Override
    public List<Channel> process(List<Channel> channels, String paramsJson) {
        RegexReplaceEngineParam param = JSONUtil.fromJsonString(paramsJson, RegexReplaceEngineParam.class);
        Pattern pattern = Pattern.compile(param.getRegex());
        for (Channel channel : channels) {
            String name = channel.getName();
            Matcher matcher = pattern.matcher(name);
            if (!matcher.find()) continue;

            // 用一个列表存所有的替换操作
            List<ReplaceOp> ops = new ArrayList<>();
            for (RegexReplaceGroupMatcher entry : param.getGroups()) {
                int gid = entry.getKey();
                // 获取该分组在字符串中的绝对位置
                int start = matcher.start(gid);
                int end = matcher.end(gid);
                if (start != -1) {
                    ops.add(new ReplaceOp(start, end, entry.getValue()));
                }
            }

            // 核心：必须从后往前替换，这样前面的索引才不会因为替换而变动
            ops.sort((a, b) -> Integer.compare(b.start, a.start));

            StringBuilder sb = new StringBuilder(name);
            for (ReplaceOp op : ops) {
                sb.replace(op.start, op.end, op.newText);
            }

            channel.setName(sb.toString());
        }
        return channels;
    }

    // 辅助类：记录替换信息
    private static class ReplaceOp {
        int start, end;
        String newText;

        ReplaceOp(int start, int end, String newText) {
            this.start = start;
            this.end = end;
            this.newText = newText;
        }
    }

    @Data
    public static class RegexReplaceEngineParam {
        private String regex;
        private List<RegexReplaceGroupMatcher> groups;
    }

    @Data
    public static class RegexReplaceGroupMatcher {
        private Integer key;
        private String value;
    }
}
