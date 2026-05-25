package com.lemongo97.iptv.iptvmanager.parser.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class EPGProgramme implements XmlTvElement{

    /**
     * required
     * 节目开始时间。
     */
    private String start;
    /**
     * 节目结束时间。节目表默认在前一个节目结束时，后一个节目立刻开始，中间没有缝隙。
     */
    private String stop;
    /**
     * required
     * 播出该节目的频道 `id`。
     */
    private String channel;
    /**
     * 默认值为 `0/1` ：用来解决同一个时间段播出多个节目（例如“新闻与天气”）的冲突问题
     */
    private String clumpidx;
    /**
     * required
     * 节目名称，例如《辛普森一家》
     */
    private Title title;

    /**
     * 副标题或者单集名称。
     */
    private SubTitle subTitle;

    /**
     * 节目制作完成的日期或版权日期。
     */
    private String date;

    /**
     * 节目简介。这个元素内部允许使用换行符。
     */
    private List<Desc> desc = new ArrayList<>();

//    /**
//     * 演职人员名单。包含导演（`director`）、演员（`actor`，可标注角色和是否为客串）、编剧（`writer`）和主持人（`presenter`）等。
//     */
//    private Object credits;

    /**
     * 节目类型，例如“喜剧”或“悬疑”。一个节目可以有多个类型
     */
    private List<Category> category = new ArrayList<>();

    /**
     * 节目的关键词。多个单词的词组建议用连字符连接（例如 `super-hero`）
     */
    private List<Keyword> keyword = new ArrayList<>();

    /**
     * 播出的语言以及原始语言（适用于配音节目）
     */
    private String language;

    /**
     * 播出的语言以及原始语言（适用于配音节目）
     */
    private OrigLanguage origLanguage;

    /**
     * （必须指定单位）：节目的实际时长，不包含广告和预告片
     */
    private Length length;

    private Icon icon;

    private List<Url> url = new ArrayList<>();

    /**
     * 制作国家，可以使用国家名称或两位大写国家代码（如 `US`、`GB`）
     */
    private String country;

    private List<EpisodeNum> episodeNum = new ArrayList<>();

    private Video video;

    private Audio audio;

    /**
     * 表示该节目是重播。可以带有之前的播出时间和频道属性
     */
    private PreviouslyShown previouslyShown;

    /**
     * 首播标签。可以在标签内写明具体含义（例如：在该频道第一次播出）
     */
    private String premiere;

    /**
     * 最后一次播放标签。通常用于电视台购买播放权后，最后一次播放该电影。
     */
    private LastChance lastChance;

//    /**
//     * 全新节目标签。指这个节目从来没有在任何地方或者该国家播放过。第二季或者老节目的新一集不能使用这个标签。
//     */
//    private Object _new;

    /**
     * 字幕信息。属性 `type` 可以是 `teletext`（数字文字字幕）、`onscreen`（内嵌硬字幕）或 `deaf-signed`（手语翻译）
     */
    private List<Subtitles> subtitles = new ArrayList<>();

    /**
     * 分级控制。例如使用 `MPAA` 系统，评分为 `NC-17`。
     */
    private List<Rating> rating = new ArrayList<>();

    /**
     * 星级评价。格式必须是 `N / M`（例如 `4/5` 表示五星里给四星）。
     */
    private List<StartRating> startRating = new ArrayList<>();

    /**
     * 节目评论。属性 `type` 必须是 `text`（文本内容）或者 `url`（评论链接）。可以标注评论来源（`source`）和评论人（`reviewer`）。
     */
    private List<Review> review = new ArrayList<>();

    /**
     * 用来为节目或演职人员提供图片。
     * <p>
     * <p> * 节目图片类型：`poster`（海报）、`backdrop`（背景图）或 `still`（节目截图）。
     * <p> * 人员图片类型：`person`（个人艺术照）或 `character`（剧照）。
     * <p> * 属性：`size`（1 表示小于 200px，2 表示 200-400px，3 表示大于 400px），`orient`（P 表示竖屏，L 表示横屏）。
     */
    private List<Image> image = new ArrayList<>();

    @Override
    public XmlTvElementType type() {
        return XmlTvElementType.Programme;
    }

    @Data
    @Accessors(chain = true)
    public static class Title {
        private String lang;
        private String value;
    }

    @Data
    @Accessors(chain = true)
    public static class SubTitle {
        private String lang;
        private String value;
    }

    @Data
    @Accessors(chain = true)
    public static class Desc {
        private String lang;
        private String value;
    }

    @Data
    @Accessors(chain = true)
    public static class Category {
        private String lang;
        private String value;
    }

    @Data
    @Accessors(chain = true)
    public static class Keyword {
        private String lang;
        private String value;
    }

    @Data
    @Accessors(chain = true)
    public static class OrigLanguage {
        private String lang;
        private String value;
    }

    @Data
    @Accessors(chain = true)
    public static class Length {
        private String units;
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

    /**
     * 集数系统
     * <p>
     * 用来记录节目的季数和集数。通过 `system` 属性来区分不同的计数方式：
     * <p>
     * <p>
     * <p> **xmltv_ns 系统**：
     * <p>使用点号分隔的三个数字（`季.集.篇幅`），所有数字都**从 0 开始计算**。
     * <p>* 例如 `1.0.0/1` 表示：第二季的第一集。
     * <p>* 如果是上下集，上集写 `1.0.0/2`，下集写 `1.0.1/2`。
     * <p>* 如果只知道是第一季，可以写成 `0..`。
     * <p>
     * <p>
     * <p> **onscreen 系统**（默认值）：
     * <p>直接抄录电视里播出的单集标签，例如 `Episode #FFEE`。
     * <p> **第三方数据库系统**（2013年引入）：
     * <p>支持直接引用 `themoviedb.org`、`thetvdb.com` 或 `imdb.com` 的数据编号。
     */
    @Data
    @Accessors(chain = true)
    public static class EpisodeNum {
        private String system;
        private String value;
    }

    @Data
    @Accessors(chain = true)
    public static class Video {
        /**
         * 是否有画面（`yes` 或 `no`）。如果是 `no`，说明这是广播电台
         */
        private String present;
        /**
         * 是否为彩色（`yes` 或 `no`）
         */
        private String colour;
        /**
         * 画面比例，例如 `16:9`
         */
        private String aspect;
        /**
         * 画面画质，例如 `HDTV`
         */
        private String quality;
    }

    @Data
    @Accessors(chain = true)
    public static class Audio {
        /**
         * 是否有声音。
         */
        private String present;
        /**
         * 声音声道类型，例如 `mono`（单声道）、`stereo`（双声道）、`surround`（环绕声）或 `bilingual`（双语独立声道）。
         */
        private String stereo;
    }

    @Data
    @Accessors(chain = true)
    public static class PreviouslyShown {
        private String start;
        private String channel;
    }

    @Data
    @Accessors(chain = true)
    public static class LastChance {
        private String lang;
        private String value;
    }

    @Data
    @Accessors(chain = true)
    public static class Subtitles {
        private String type;
        private String language;
    }

    @Data
    @Accessors(chain = true)
    public static class Rating {
        private String system;
        private String value;
        private Icon icon;

        @Data
        @Accessors(chain = true)
        public static class Icon {
            private String src;
        }
    }

    @Data
    @Accessors(chain = true)
    public static class StartRating {
        private String system;
        private String value;
        private Icon icon;

        @Data
        @Accessors(chain = true)
        public static class Icon {
            private String src;
        }
    }

    @Data
    @Accessors(chain = true)
    public static class Review {
        private String type;
        private String source;
        private String reviewer;
        private String lang;
        private String value;
    }

    @Data
    @Accessors(chain = true)
    public static class Image {
        private String type;
        private String size;
        private String orient;
        private String system;
        private String value;
    }
}
