package com.lemongo97.iptv.iptvmanager.engine.delay;

import com.lemongo97.iptv.iptvmanager.engine.CleaningEngine;

import java.util.List;

public class FFProbeCheckEngine implements CleaningEngine {

    /**
     * 延迟时间，高于此延迟时间的将被过滤
     */
    private Integer delayMinutes;

    /**
     * 丢弃无视频
     */
    private Boolean discardNoVideo;

    /**
     * 丢弃无音频
     */
    private Boolean discardNoAudio;

    /**
     * 最小视频帧宽
     */
    private Integer minVideoFrameWidth;

    /**
     * 最小视频帧高
     */
    private Integer minVideoFrameHeight;

    @Override
    public List<Object> process(List<Object> channels, String paramsJson) {
        return List.of();
    }
}
