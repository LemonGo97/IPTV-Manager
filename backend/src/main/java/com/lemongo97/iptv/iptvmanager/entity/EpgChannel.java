package com.lemongo97.iptv.iptvmanager.entity;

import com.lemongo97.iptv.iptvmanager.mybatis.JsonArrayTypeHandler;
import com.lemongo97.iptv.iptvmanager.mybatis.JsonObjectTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * EPG 节目
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class EpgChannel {
    private Long id;
    private Long epgSourceId;
    private String channelId;
    private List<DisplayName> displayName;
    private Icon icon;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean deleted;

    @Data
    public static class DisplayName {
        private String lang;
        private String value;
    }

    @Data
    public static class Icon {
        private String src;
        private String width;
        private String height;
    }

    public static class DisplayNameTypeHandler extends JsonArrayTypeHandler<DisplayName> {
        public DisplayNameTypeHandler() {
            super(DisplayName.class);
        }
    }

    public static class IconTypeHandler extends JsonObjectTypeHandler<Icon> {
        public IconTypeHandler() {
            super(Icon.class);
        }
    }
}
