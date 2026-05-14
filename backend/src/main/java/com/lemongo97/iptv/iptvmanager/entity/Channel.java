package com.lemongo97.iptv.iptvmanager.entity;

import com.lemongo97.iptv.iptvmanager.m3u8.OriginalChannel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;

/**
 * 频道实体
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class Channel {
    /**
     * 自增ID
     */
    private Long id;
    /**
     * 频道名称
     */
    private String name;
    /**
     * 频道LOGO URL
     */
    private String logo;
    /**
     * 频道URL
     */
    private String url;
    /**
     * 频道组ID
     */
    private Long groupId;
    /**
     * 用于匹配EPG的ID字符串
     */
    private String epgSourceId;
    /**
     * 状态（有效/无效）
     */
    private Status status;
    /**
     * 国家/地区
     */
    private String country;
    /**
     * 语言
     */
    private String language;
    /**
     * 评分。满分100，分数越高质量越好
     */
    private Integer score;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private ChannelGroup channelGroup;

    public enum Status {
        valid,
        invalid
    }

    public static class ChannelEPGTimeline extends ArrayList<ChannelEPGTimelineItem> {

        public ChannelEPGTimeline addItem(ChannelEPGTimelineItem item){
            this.add(item);
            return this;
        }

    }

    @Data
    @Accessors(chain = true)
    public static class ChannelEPGTimelineItem {
        /**
         * 频道名称
         */
        private String channel;
        /**
         * 时间轴对象类型
         */
        private Type type;
        /**
         * 节目开始时间
         */
        private OffsetDateTime startTime;
        /**
         * 节目结束时间
         */
        private OffsetDateTime stopTime;
        /**
         * 节目名称
         */
        private String title;
        /**
         * 语言
         */
        private String lang;

        public enum Type {
            program,
            date,
        }
    }
}
