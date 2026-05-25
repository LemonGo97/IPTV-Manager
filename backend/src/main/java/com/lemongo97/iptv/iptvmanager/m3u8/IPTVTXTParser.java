package com.lemongo97.iptv.iptvmanager.m3u8;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class IPTVTXTParser {
    public <T> List<T> parseContent(String content, Class<T> clazz) {
        List<T> channels = new ArrayList<>();

        String[] lines = content.split("\\r?\\n");
        for (String line : lines) {
            line = line.trim();
            if (StringUtils.isBlank(line)) continue;

            int ch = line.indexOf(',');

            String part2 = line.substring(ch + 1);
            if (!part2.startsWith("http")) continue;

            String part1 = line.substring(0, ch);

            try{
                Constructor<T> constructor = clazz.getDeclaredConstructor();
                constructor.setAccessible(true);
                T parsed = constructor.newInstance();

                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    ChannelName nameAnno = field.getAnnotation(ChannelName.class);
                    ChannelUrl urlAnno = field.getAnnotation(ChannelUrl.class);

                    if (ObjectUtils.allNull(nameAnno, urlAnno)) continue;

                    field.setAccessible(true);

                    if (nameAnno != null) {
                        field.set(parsed, part1);
                        continue;
                    }

                    field.set(parsed, part2);
                }
                channels.add(parsed);
            }catch (Exception e){
                log.error("Error while parsing {} using {}", line, clazz.getName());
            }
        }
        return channels;
    }
}
