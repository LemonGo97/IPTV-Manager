package com.lemongo97.iptv.iptvmanager.m3u8;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 简单的 M3U8 解析器（兼容 IPTV 播放列表）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IPTVM3U8Parser {

    /**
     * 解析 M3U8 内容并返回频道列表
     */
    public <T> List<T> parseContent(String content, Class<T> clazz) {
        List<T> channels = new ArrayList<>();

        Map<String, String> currentAttributes = new HashMap<>();
        String currentName = "Unknown Channel";


        List<String> attrNames = Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> {
                    MetadataAttribute anno = field.getAnnotation(MetadataAttribute.class);
                    return anno != null && StringUtils.isNotBlank(anno.value()) && String.class.equals(field.getType());
                })
                .map(field -> {
                    MetadataAttribute anno = field.getAnnotation(MetadataAttribute.class);
                    return anno.value();
                }).toList();
        String[] lines = content.split("\\r?\\n");
        for (String line : lines) {
            line = line.trim();
            if (StringUtils.isBlank(line)) continue;

            if (line.startsWith("#EXTINF:")) {

                currentName = extractExtInfName(line);

                for (String attr : attrNames) {
                    currentAttributes.put(attr, extractAttribute(line, attr));
                }
            } else if (!line.startsWith("#")) {
                try{
                    Constructor<T> constructor = clazz.getDeclaredConstructor();
                    constructor.setAccessible(true);
                    T parsed = constructor.newInstance();

                    Field[] fields = clazz.getDeclaredFields();
                    for (Field field : fields) {
                        MetadataAttribute attrAnno = field.getAnnotation(MetadataAttribute.class);
                        ChannelName nameAnno = field.getAnnotation(ChannelName.class);
                        ChannelUrl urlAnno = field.getAnnotation(ChannelUrl.class);

                        if (ObjectUtils.allNull(attrAnno, nameAnno, urlAnno)) continue;

                        field.setAccessible(true);

                        if (nameAnno != null) {
                            field.set(parsed, currentName);
                            continue;
                        }

                        if (urlAnno != null) {
                            field.set(parsed, line);
                            continue;
                        }

                        if (StringUtils.isBlank(attrAnno.value())) continue;

                        field.set(parsed, currentAttributes.get(attrAnno.value()));
                    }
                    channels.add(parsed);
                }catch (Exception e){
                    log.error("Error while parsing {} using {} using {}", line, clazz.getName(), currentAttributes);
                } finally {
                    currentName = "Unknown Channel";
                    currentAttributes.clear();
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
            return extInfLine.substring(lastComma + 1).trim();
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

}
