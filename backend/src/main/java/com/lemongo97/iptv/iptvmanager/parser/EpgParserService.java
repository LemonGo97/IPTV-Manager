package com.lemongo97.iptv.iptvmanager.parser;

import com.lemongo97.iptv.iptvmanager.entity.EpgProgram;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * EPG 解析服务 (XMLTV 格式)
 * 使用 jsoup 解析 XMLTV 格式的 EPG 数据
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EpgParserService {

    /**
     * 解析 EPG XML 内容
     *
     * @param xmlContent XMLTV 格式的 XML 内容
     * @param epgSourceId EPG 源 ID
     * @return 解析后的节目列表
     */
    public List<EpgProgram> parse(String xmlContent, Long epgSourceId) {
        log.info("Parsing EPG data for source: id={}, length={} bytes", epgSourceId, xmlContent.length());

        try {
            Document doc = Jsoup.parse(xmlContent, "", org.jsoup.parser.Parser.xmlParser());
            Elements programmeElements = doc.select("programme");

            // 构建频道名称映射
            Map<String, String> channelNameMap = buildChannelNameMap(doc);

            List<EpgProgram> programs = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now();
            int skipped = 0;

            for (Element programme : programmeElements) {
                try {
                    EpgProgram program = parseProgramme(programme, channelNameMap, epgSourceId, now);
                    if (program != null) {
                        programs.add(program);
                    } else {
                        skipped++;
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse programme element: {}", e.getMessage());
                    skipped++;
                }
            }

            log.info("EPG parsing completed: total={}, parsed={}, skipped={}",
                programmeElements.size(), programs.size(), skipped);

            return programs;

        } catch (Exception e) {
            log.error("Failed to parse EPG XML: {}", e.getMessage(), e);
            throw new RuntimeException("EPG parsing failed: " + e.getMessage(), e);
        }
    }

    /**
     * 构建频道 ID 到频道名称的映射
     */
    private Map<String, String> buildChannelNameMap(Document doc) {
        Map<String, String> channelNameMap = new HashMap<>();
        Elements channelElements = doc.select("channel");

        for (Element channel : channelElements) {
            String channelId = channel.attr("id");
            Element displayName = channel.selectFirst("display-name");
            if (displayName != null) {
                channelNameMap.put(channelId, displayName.text());
            }
        }

        log.debug("Built channel name map: {} channels", channelNameMap.size());
        return channelNameMap;
    }

    /**
     * 解析单个 programme 元素
     */
    private EpgProgram parseProgramme(Element programme, Map<String, String> channelNameMap,
                                      Long epgSourceId, LocalDateTime now) {
        String channelId = programme.attr("channel");
        String channelName = channelNameMap.getOrDefault(channelId, channelId);

        String startTime = programme.attr("start");
        String stopTime = programme.attr("stop");

        // 解析标题
        Element titleElement = programme.selectFirst("title");
        String title = titleElement != null ? titleElement.text() : "未知节目";

        // 解析副标题
        Element subTitleElement = programme.selectFirst("sub-title");
        String subTitle = subTitleElement != null ? subTitleElement.text() : null;

        // 解析描述
        Element descElement = programme.selectFirst("desc");
        String description = descElement != null ? descElement.text() : null;

        // 解析分类
        Element categoryElement = programme.selectFirst("category");
        String categories = null;
        if (categoryElement != null) {
            categories = "[\"" + categoryElement.text() + "\"]";
        }

        // 创建 EpgProgram 对象
        return new EpgProgram(
            null,                      // id
            epgSourceId,               // epgSourceId
            channelId,                 // channelId
            channelName,               // channelName
            startTime,                 // startTime (原始 XMLTV 格式)
            stopTime,                  // stopTime
            title,                     // title
            subTitle,                  // subTitle
            description,               // description
            categories,                // categories (JSON array string)
            null,                      // icons
            null,                      // credits
            null,                      // ratings
            now,                       // createdAt
            now,                       // updatedAt
            false                      // deleted
        );
    }
}
