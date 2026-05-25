package com.lemongo97.iptv.iptvmanager.parser;

import com.lemongo97.iptv.iptvmanager.parser.entity.EPGChannel;
import com.lemongo97.iptv.iptvmanager.parser.entity.EPGProgramme;
import com.lemongo97.iptv.iptvmanager.parser.entity.EPGTv;
import com.lemongo97.iptv.iptvmanager.parser.handler.EPGChannelHandler;
import com.lemongo97.iptv.iptvmanager.parser.handler.EPGProgrammeHandler;
import com.lemongo97.iptv.iptvmanager.parser.handler.EPGTvHandler;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EPGParser {

    public static List<EPGTv> parse(String xmlContent, EPGChannelHandler channelHandler, EPGProgrammeHandler programmeHandler, EPGTvHandler tvHandler) throws IOException {
        Document document = Jsoup.parse(xmlContent, Parser.xmlParser());
        return parse(document, channelHandler, programmeHandler, tvHandler);
    }

    public static List<EPGTv> parse(InputStream xmlInputStream, Charset charset, EPGChannelHandler channelHandler, EPGProgrammeHandler programmeHandler, EPGTvHandler tvHandler) throws IOException {
        Document document = Jsoup.parse(xmlInputStream, charset.name(), "", Parser.xmlParser());
        return parse(document, channelHandler, programmeHandler, tvHandler);
    }

    public static List<EPGTv> parse(Document document, EPGChannelHandler channelHandler, EPGProgrammeHandler programmeHandler, EPGTvHandler tvHandler) throws IOException {
        Elements tvElements = document.children();
        List<EPGTv> epgTvs = new ArrayList<>();
        for (Element tvElement : tvElements) {
            EPGTv tv = parseTVElement(tvElement, channelHandler, programmeHandler);
            epgTvs.add(tv);

            if (tvHandler != null) {
                tvHandler.handle(tv);
            }
        }
        return epgTvs;
    }

    private static EPGTv parseTVElement(Element tvElement, EPGChannelHandler channelHandler, EPGProgrammeHandler programmeHandler){
        EPGTv tv = new EPGTv();

        String generatorInfoName = tvElement.attr("generator-info-name");
        tv.setGeneratorInfoName(generatorInfoName);
        String generatorInfoUrl = tvElement.attr("generator-info-url");
        tv.setGeneratorInfoUrl(generatorInfoUrl);
        String sourceInfoName = tvElement.attr("source-info-name");
        tv.setSourceInfoName(sourceInfoName);
        String sourceInfoUrl = tvElement.attr("source-info-url");
        tv.setSourceInfoUrl(sourceInfoUrl);
        String date = tvElement.attr("date");
        tv.setDate(date);


        for (Element element : tvElement.children()) {
            String tagName = element.tag().getName();
            if ("channel".equals(tagName)) {
                EPGChannel channel = parseChannelElement(element);
                tv.getChannel().add(channel);

                if (channelHandler != null) {
                    channelHandler.handle(channel);
                }
            } else if ("programme".equals(tagName)) {
                EPGProgramme programme = parseProgrammeElement(element);
                tv.getProgramme().add(programme);

                if (programmeHandler != null) {
                    programmeHandler.handle(programme);
                }
            } else {
                continue;
            }
        }
        return tv;
    }

    private static EPGChannel parseChannelElement(Element element) {
        EPGChannel channel = new EPGChannel();

        String id = element.attr("id");
        channel.setId(id);

        for (Element child : element.children()) {
            String tagName = child.tag().getName();
            if ("display-name".equals(tagName)) {
                EPGChannel.DisplayName displayName = new EPGChannel.DisplayName();

                String lang = child.attr("lang");
                displayName.setLang(lang);

                String value = child.text();
                displayName.setValue(value);

                channel.getDisplayName().add(displayName);
            } else if ("icon".equals(tagName)) {
                EPGChannel.Icon icon = new EPGChannel.Icon();

                String src = child.attr("src");
                icon.setSrc(src);

                String width = child.attr("width");
                icon.setWidth(width);

                String height = child.attr("height");
                icon.setHeight(height);

                channel.setIcon(icon);
            } else if ("url".equals(tagName)) {
                EPGChannel.Url url = new EPGChannel.Url();

                String system = child.attr("system");
                url.setSystem(system);

                String value = child.text();
                url.setValue(value);

                channel.getUrl().add(url);
            } else {
                continue;
            }
        }

        return channel;
    }

    private static EPGProgramme parseProgrammeElement(Element element) {
        EPGProgramme programme = new EPGProgramme();

        String start = element.attr("start");
        programme.setStart(start);

        String stop = element.attr("stop");
        programme.setStop(stop);

        String channel = element.attr("channel");
        programme.setChannel(channel);

        for (Element child : element.children()) {
            if ("title".equals(child.tag().getName())) {
                EPGProgramme.Title title = new EPGProgramme.Title();

                String lang = child.attr("lang");
                title.setLang(lang);
                String value = child.text();
                title.setValue(value);

                programme.setTitle(title);
            } else if ("desc".equals(child.tag().getName())) {
                EPGProgramme.Desc desc = new EPGProgramme.Desc();

                String lang = child.attr("lang");
                desc.setLang(lang);
                String value = child.text();
                desc.setValue(value);

                programme.getDesc().add(desc);
            } else {
                continue;
            }
        }

        return programme;
    }


}
