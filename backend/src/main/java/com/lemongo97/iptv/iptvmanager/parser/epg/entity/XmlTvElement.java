package com.lemongo97.iptv.iptvmanager.parser.epg.entity;

public interface XmlTvElement {

    XmlTvElementType type();

    enum XmlTvElementType {
        Tv,
        Channel,
        Programme
    }

}
