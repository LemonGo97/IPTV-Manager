package com.lemongo97.iptv.iptvmanager.parser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 简单的 M3U8 解析器（兼容 IPTV 播放列表）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SimpleM3U8Parser {

    /**
     * 解析 M3U8 内容并返回频道列表
     */
    public List<ParsedChannel> parseContent(String content) {
        List<ParsedChannel> channels = new ArrayList<>();
        String currentName = "Unknown Channel";
        String currentLogo = null;
        String currentGroup = null;
        String currentTvgId = null;
        String currentTvgName = null;
        String currentTvgCountry = null;
        String currentTvgLanguage = null;

        String[] lines = content.split("\\r?\\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            if (line.startsWith("#EXTINF:")) {
                // 解析 #EXTINF 行
                currentName = extractExtInfName(line);
                currentLogo = extractAttribute(line, "tvg-logo");
                currentGroup = extractAttribute(line, "group-title");
                currentTvgId = extractAttribute(line, "tvg-id");
                currentTvgName = extractAttribute(line, "tvg-name");
                currentTvgCountry = extractAttribute(line, "tvg-country");
                currentTvgLanguage = extractAttribute(line, "tvg-language");
            } else if (!line.startsWith("#")) {
                // 这是一个 URL 行
                String url = line;
                if (!url.isEmpty()) {
                    channels.add(new ParsedChannel(
                        currentName,
                        currentGroup,
                        url,
                        currentLogo,
                        currentTvgId,
                        currentTvgName,
                        currentTvgCountry,
                        currentTvgLanguage
                    ));
                }
            }
        }

        log.info("Parsed {} channels using simple parser", channels.size());
        return channels;
    }

    /**
     * 从 #EXTINF 行中提取频道名称
     */
    private String extractExtInfName(String extInfLine) {
        // 格式: #EXTINF:-1 tvg-name="CCTV 8K" tvg-id="" tvg-country="中國大陸" ...,CCTV 8K
        // 提取最后一个逗号后的名称
        int lastComma = extInfLine.lastIndexOf(',');
        if (lastComma > 0) {
            String namePart = extInfLine.substring(lastComma + 1).trim();
            //TODO 移除可能的前缀
//            if (namePart.contains("[BD]")) {
//                namePart = namePart.substring(namePart.indexOf("[") + 1).trim();
//            }
            return namePart;
        }
        return "Unknown Channel";
    }

    /**
     * 提取属性值
     */
    private String extractAttribute(String line, String attrName) {
        Pattern pattern = Pattern.compile(attrName + "=\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * 解析的频道数据
     */
    public record ParsedChannel(
            String name,
            String group,
            String url,
            String logo,
            String tvgId,
            String tvgName,
            String tvgCountry,
            String tvgLanguage
    ) {}
}
