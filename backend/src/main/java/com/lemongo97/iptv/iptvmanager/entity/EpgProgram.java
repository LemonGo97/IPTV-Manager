package com.lemongo97.iptv.iptvmanager.entity;

import com.lemongo97.iptv.iptvmanager.config.mybatis.JsonArrayTypeHandler;
import com.lemongo97.iptv.iptvmanager.config.mybatis.JsonObjectTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

/**
 * EPG 节目
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class EpgProgram {
    private Long id;
    private Long epgSourceId;
    private String channelId;
    private String startTime;
    private String stopTime;
    private Title title;
    private String subTitle;
    private List<Desc> description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean deleted;



    @Data
    @Accessors(chain = true)
    public static class Title {
        private String lang;
        private String value;
    }

    @Data
    @Accessors(chain = true)
    public static class Desc {
        private String lang;
        private String value;
    }


    public static class DescriptionTypeHandler extends JsonArrayTypeHandler<Desc> {
        public DescriptionTypeHandler() {
            super(Desc.class);
        }
    }

    public static class TitleTypeHandler extends JsonObjectTypeHandler<Title> {
        public TitleTypeHandler() {
            super(Title.class);
        }
    }
}
