package com.lemongo97.iptv.iptvmanager.parser.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain=true)
public class EPGTv implements XmlTvElement{
    /**
     * 这份节目表最初生成的日期。
     */
    private String date;
    /**
     * 数据提供方的名字。
     */
    private String sourceInfoName;
    /**
     * 数据提供方的网站链接。
     */
    private String sourceInfoUrl;
    /**
     * 获取原始数据文件的链接。
     */
    private String sourceDataUrl;
    /**
     * 生成这份文件的软件名字和版本号。
     */
    private String generatorInfoName;
    /**
     * 该软件的官方网址。
     */
    private String generatorInfoUrl;
    /**
     * 频道列表
     */
    private List<EPGChannel> channel = new ArrayList<>();
    /**
     * 节目列表
     */
    private List<EPGProgramme> programme = new ArrayList<>();

    @Override
    public XmlTvElementType type() {
        return XmlTvElementType.Tv;
    }
}
