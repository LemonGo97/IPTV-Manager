package com.lemongo97.iptv.iptvmanager.parser.epg.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class EPGChannel implements XmlTvElement {
    private String id;
    private List<DisplayName> displayName = new ArrayList<>();
    private Icon icon;
    private List<Url> url = new ArrayList<>();

    @Override
    public XmlTvElementType type() {
        return XmlTvElementType.Channel;
    }

    @Data
    @Accessors(chain = true)
    public static class DisplayName {
        private String lang;
        private String value;
    }

    @Data
    @Accessors(chain = true)
    public static class Icon {
        private String src;
        private String width;
        private String height;
    }

    @Data
    @Accessors(chain = true)
    public static class Url {
        private String system;
        private String value;
    }
}
