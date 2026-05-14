package com.lemongo97.iptv.iptvmanager.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.lemongo97.iptv.iptvmanager.common.BusinessException;
import com.lemongo97.iptv.iptvmanager.common.PageResult;
import com.lemongo97.iptv.iptvmanager.controller.request.ChannelQuery;
import com.lemongo97.iptv.iptvmanager.entity.Channel;
import com.lemongo97.iptv.iptvmanager.entity.OriginalChannelMetadata;
import com.lemongo97.iptv.iptvmanager.mapper.ChannelMapper;
import com.lemongo97.iptv.iptvmanager.mapper.OriginalChannelMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 频道服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChannelService {

    private final ChannelMapper channelMapper;
    private final OriginalChannelMapper originalChannelMapper;

    /**
     * 获取所有频道
     */
    public List<Channel> findAll() {
        return channelMapper.findAll();
    }

    /**
     * 根据 ID 获取频道
     */
    public Channel findById(Long id) {
        return channelMapper.findById(id)
                .orElseThrow(() -> new BusinessException("Channel not found: id=" + id));
    }

    /**
     * 根据分组获取频道
     */
    public List<Channel> findByGroup(String group) {
        return channelMapper.findByGroup(group);
    }

    /**
     * 获取统计数据
     *
     * @return
     */
    public Map<String, Object> statistic() {
        // TODO 获取分组统计数据
        Map<String, Object> result = new HashMap<>();
        result.put("totalChannels", 1234);
        result.put("validChannels", 1156);
        result.put("invalidChannels", 78);
        result.put("groupCount", 12);
        result.put("status", "进行中");
        return result;
    }

    /**
     * 获取节目EPG时间轴
     *
     * @param id
     * @return
     */
    public Channel.ChannelEPGTimeline getEPGTimeline(Long id) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss Z");

        // TODO 获取真实数据

        Channel.ChannelEPGTimeline result = new Channel.ChannelEPGTimeline()
                .addItem(new Channel.ChannelEPGTimelineItem()
                        .setChannel("CCTV 1")
                        .setType(Channel.ChannelEPGTimelineItem.Type.date)
                        .setTitle("5月14日 星期四")
                        .setLang("zh"))
                .addItem(new Channel.ChannelEPGTimelineItem()
                        .setChannel("CCTV 1")
                        .setType(Channel.ChannelEPGTimelineItem.Type.program)
                        .setStartTime(OffsetDateTime.parse("20260514005200 +0800", formatter))
                        .setStopTime(OffsetDateTime.parse("20260514012200 +0800", formatter))
                        .setTitle("晚间新闻")
                        .setLang("zh")
                ).addItem(new Channel.ChannelEPGTimelineItem()
                        .setChannel("CCTV 1")
                        .setType(Channel.ChannelEPGTimelineItem.Type.program)
                        .setStartTime(OffsetDateTime.parse("20260514012200 +0800", formatter))
                        .setStopTime(OffsetDateTime.parse("20260514020400 +0800", formatter))
                        .setTitle("生活早参考-特别节目（生活圈）2026-128")
                        .setLang("zh")
                ).addItem(new Channel.ChannelEPGTimelineItem()
                        .setChannel("CCTV 1")
                        .setType(Channel.ChannelEPGTimelineItem.Type.program)
                        .setStartTime(OffsetDateTime.parse("20260514020400 +0800", formatter))
                        .setStopTime(OffsetDateTime.parse("20260514023400 +0800", formatter))
                        .setTitle("农耕探文明-2025-20")
                        .setLang("zh")
                ).addItem(new Channel.ChannelEPGTimelineItem()
                        .setChannel("CCTV 1")
                        .setType(Channel.ChannelEPGTimelineItem.Type.program)
                        .setStartTime(OffsetDateTime.parse("20260514023400 +0800", formatter))
                        .setStopTime(OffsetDateTime.parse("20260514023800 +0800", formatter))
                        .setTitle("三餐四季（第二季）-宣传片")
                        .setLang("zh")
                ).addItem(new Channel.ChannelEPGTimelineItem()
                        .setChannel("CCTV 1")
                        .setType(Channel.ChannelEPGTimelineItem.Type.program)
                        .setStartTime(OffsetDateTime.parse("20260514023800 +0800", formatter))
                        .setStopTime(OffsetDateTime.parse("20260514024400 +0800", formatter))
                        .setTitle("非遗里的中国-MV")
                        .setLang("zh")
                ).addItem(new Channel.ChannelEPGTimelineItem()
                        .setChannel("CCTV 1")
                        .setType(Channel.ChannelEPGTimelineItem.Type.program)
                        .setStartTime(OffsetDateTime.parse("20260514024400 +0800", formatter))
                        .setStopTime(OffsetDateTime.parse("20260514042000 +0800", formatter))
                        .setTitle("宗师列传·大宋词人传-黄庭坚")
                        .setLang("zh")
                ).addItem(new Channel.ChannelEPGTimelineItem()
                        .setChannel("CCTV 1")
                        .setType(Channel.ChannelEPGTimelineItem.Type.program)
                        .setStartTime(OffsetDateTime.parse("20260514042000 +0800", formatter))
                        .setStopTime(OffsetDateTime.parse("20260514042200 +0800", formatter))
                        .setTitle("泱泱中华-历史文化街区1")
                        .setLang("zh")
                ).addItem(new Channel.ChannelEPGTimelineItem()
                        .setChannel("CCTV 1")
                        .setType(Channel.ChannelEPGTimelineItem.Type.program)
                        .setStartTime(OffsetDateTime.parse("20260514042200 +0800", formatter))
                        .setStopTime(OffsetDateTime.parse("20260514045300 +0800", formatter))
                        .setTitle("今日说法-2026-82")
                        .setLang("zh")
                ).addItem(new Channel.ChannelEPGTimelineItem()
                        .setChannel("CCTV 1")
                        .setType(Channel.ChannelEPGTimelineItem.Type.program)
                        .setStartTime(OffsetDateTime.parse("20260514045300 +0800", formatter))
                        .setStopTime(OffsetDateTime.parse("20260514052700 +0800", formatter))
                        .setTitle("新闻联播")
                        .setLang("zh")
                ).addItem(new Channel.ChannelEPGTimelineItem()
                        .setChannel("CCTV 1")
                        .setType(Channel.ChannelEPGTimelineItem.Type.program)
                        .setStartTime(OffsetDateTime.parse("20260514052700 +0800", formatter))
                        .setStopTime(OffsetDateTime.parse("20260514060000 +0800", formatter))
                        .setTitle("寻古中国-寻秦记4")
                        .setLang("zh")
                ).addItem(new Channel.ChannelEPGTimelineItem()
                        .setChannel("CCTV 1")
                        .setType(Channel.ChannelEPGTimelineItem.Type.program)
                        .setStartTime(OffsetDateTime.parse("20260514060000 +0800", formatter))
                        .setStopTime(OffsetDateTime.parse("20260514083000 +0800", formatter))
                        .setTitle("朝闻天下")
                        .setLang("zh")
                ).addItem(new Channel.ChannelEPGTimelineItem()
                        .setChannel("CCTV 1")
                        .setType(Channel.ChannelEPGTimelineItem.Type.program)
                        .setStartTime(OffsetDateTime.parse("20260514083000 +0800", formatter))
                        .setStopTime(OffsetDateTime.parse("20260514090000 +0800", formatter))
                        .setTitle("朝闻天下")
                        .setLang("zh")
                ).addItem(new Channel.ChannelEPGTimelineItem()
                        .setChannel("CCTV 1")
                        .setType(Channel.ChannelEPGTimelineItem.Type.program)
                        .setStartTime(OffsetDateTime.parse("20260514090000 +0800", formatter))
                        .setStopTime(OffsetDateTime.parse("20260514093000 +0800", formatter))
                        .setTitle("新闻直播间")
                        .setLang("zh")
                ).addItem(new Channel.ChannelEPGTimelineItem()
                        .setChannel("CCTV 1")
                        .setType(Channel.ChannelEPGTimelineItem.Type.program)
                        .setStartTime(OffsetDateTime.parse("20260514093000 +0800", formatter))
                        .setStopTime(OffsetDateTime.parse("20260514104500 +0800", formatter))
                        .setTitle("新闻直播间")
                        .setLang("zh")
                ).addItem(new Channel.ChannelEPGTimelineItem()
                        .setChannel("CCTV 1")
                        .setType(Channel.ChannelEPGTimelineItem.Type.program)
                        .setStartTime(OffsetDateTime.parse("20260514104500 +0800", formatter))
                        .setStopTime(OffsetDateTime.parse("20260514110000 +0800", formatter))
                        .setTitle("中华古树-黄山迎客松（4K）")
                        .setLang("zh")
                ).addItem(new Channel.ChannelEPGTimelineItem()
                        .setChannel("CCTV 1")
                        .setType(Channel.ChannelEPGTimelineItem.Type.program)
                        .setStartTime(OffsetDateTime.parse("20260514110000 +0800", formatter))
                        .setStopTime(OffsetDateTime.parse("20260514115000 +0800", formatter))
                        .setTitle("爱情没有神话第11集")
                        .setLang("zh")
                ).addItem(new Channel.ChannelEPGTimelineItem()
                        .setChannel("CCTV 1")
                        .setType(Channel.ChannelEPGTimelineItem.Type.program)
                        .setStartTime(OffsetDateTime.parse("20260514115000 +0800", formatter))
                        .setStopTime(OffsetDateTime.parse("20260514115100 +0800", formatter))
                        .setTitle("中华古树-绿色国宝（4K）")
                        .setLang("zh")
                ).addItem(new Channel.ChannelEPGTimelineItem()
                        .setChannel("CCTV 1")
                        .setType(Channel.ChannelEPGTimelineItem.Type.program)
                        .setStartTime(OffsetDateTime.parse("20260514115100 +0800", formatter))
                        .setStopTime(OffsetDateTime.parse("20260514115400 +0800", formatter))
                        .setTitle("非遗里的中国-MV")
                        .setLang("zh")
                ).addItem(new Channel.ChannelEPGTimelineItem()
                        .setChannel("CCTV 1")
                        .setType(Channel.ChannelEPGTimelineItem.Type.program)
                        .setStartTime(OffsetDateTime.parse("20260514115400 +0800", formatter))
                        .setStopTime(OffsetDateTime.parse("20260514120000 +0800", formatter))
                        .setTitle("秘境之眼-2026-113")
                        .setLang("zh")
                ).addItem(new Channel.ChannelEPGTimelineItem()
                        .setChannel("CCTV 1")
                        .setType(Channel.ChannelEPGTimelineItem.Type.program)
                        .setStartTime(OffsetDateTime.parse("20260514120000 +0800", formatter))
                        .setStopTime(OffsetDateTime.parse("20260514123400 +0800", formatter))
                        .setTitle("新闻30分")
                        .setLang("zh")
                ).addItem(new Channel.ChannelEPGTimelineItem()
                        .setChannel("CCTV 1")
                        .setType(Channel.ChannelEPGTimelineItem.Type.program)
                        .setStartTime(OffsetDateTime.parse("20260514123400 +0800", formatter))
                        .setStopTime(OffsetDateTime.parse("20260514130700 +0800", formatter))
                        .setTitle("今日说法-2026-83")
                        .setLang("zh")
                ).addItem(new Channel.ChannelEPGTimelineItem()
                        .setChannel("CCTV 1")
                        .setType(Channel.ChannelEPGTimelineItem.Type.program)
                        .setStartTime(OffsetDateTime.parse("20260514130700 +0800", formatter))
                        .setStopTime(OffsetDateTime.parse("20260514135400 +0800", formatter))
                        .setTitle("生命树第18集")
                        .setLang("zh")
                ).addItem(new Channel.ChannelEPGTimelineItem()
                        .setChannel("CCTV 1")
                        .setType(Channel.ChannelEPGTimelineItem.Type.program)
                        .setStartTime(OffsetDateTime.parse("20260514135400 +0800", formatter))
                        .setStopTime(OffsetDateTime.parse("20260514144200 +0800", formatter))
                        .setTitle("生命树第19集")
                        .setLang("zh")
                ).addItem(new Channel.ChannelEPGTimelineItem()
                        .setChannel("CCTV 1")
                        .setType(Channel.ChannelEPGTimelineItem.Type.program)
                        .setStartTime(OffsetDateTime.parse("20260514144200 +0800", formatter))
                        .setStopTime(OffsetDateTime.parse("20260514153000 +0800", formatter))
                        .setTitle("生命树第20集")
                        .setLang("zh")
                ).addItem(new Channel.ChannelEPGTimelineItem()
                        .setChannel("CCTV 1")
                        .setType(Channel.ChannelEPGTimelineItem.Type.program)
                        .setStartTime(OffsetDateTime.parse("20260514153000 +0800", formatter))
                        .setStopTime(OffsetDateTime.parse("20260514162000 +0800", formatter))
                        .setTitle("生命树第21集")
                        .setLang("zh")
                ).addItem(new Channel.ChannelEPGTimelineItem()
                        .setChannel("CCTV 1")
                        .setType(Channel.ChannelEPGTimelineItem.Type.program)
                        .setStartTime(OffsetDateTime.parse("20260514162000 +0800", formatter))
                        .setStopTime(OffsetDateTime.parse("20260514171200 +0800", formatter))
                        .setTitle("生命树第22集")
                        .setLang("zh")
                ).addItem(new Channel.ChannelEPGTimelineItem()
                        .setChannel("CCTV 1")
                        .setType(Channel.ChannelEPGTimelineItem.Type.program)
                        .setStartTime(OffsetDateTime.parse("20260514171200 +0800", formatter))
                        .setStopTime(OffsetDateTime.parse("20260514173700 +0800", formatter))
                        .setTitle("第一动画乐园-2026-128")
                        .setLang("zh")
                ).addItem(new Channel.ChannelEPGTimelineItem()
                        .setChannel("CCTV 1")
                        .setType(Channel.ChannelEPGTimelineItem.Type.program)
                        .setStartTime(OffsetDateTime.parse("20260514173700 +0800", formatter))
                        .setStopTime(OffsetDateTime.parse("20260514182100 +0800", formatter))
                        .setTitle("生活早参考-特别节目（生活圈）2026-129")
                        .setLang("zh")
                ).addItem(new Channel.ChannelEPGTimelineItem()
                        .setChannel("CCTV 1")
                        .setType(Channel.ChannelEPGTimelineItem.Type.program)
                        .setStartTime(OffsetDateTime.parse("20260514182100 +0800", formatter))
                        .setStopTime(OffsetDateTime.parse("20260514185100 +0800", formatter))
                        .setTitle("农耕探文明-2025-6")
                        .setLang("zh")
                ).addItem(new Channel.ChannelEPGTimelineItem()
                        .setChannel("CCTV 1")
                        .setType(Channel.ChannelEPGTimelineItem.Type.program)
                        .setStartTime(OffsetDateTime.parse("20260514185100 +0800", formatter))
                        .setStopTime(OffsetDateTime.parse("20260514190000 +0800", formatter))
                        .setTitle("秘境之眼-2026-127")
                        .setLang("zh")
                ).addItem(new Channel.ChannelEPGTimelineItem()
                        .setChannel("CCTV 1")
                        .setType(Channel.ChannelEPGTimelineItem.Type.program)
                        .setStartTime(OffsetDateTime.parse("20260514190000 +0800", formatter))
                        .setStopTime(OffsetDateTime.parse("20260514193800 +0800", formatter))
                        .setTitle("新闻联播")
                        .setLang("zh")
                ).addItem(new Channel.ChannelEPGTimelineItem()
                        .setChannel("CCTV 1")
                        .setType(Channel.ChannelEPGTimelineItem.Type.program)
                        .setStartTime(OffsetDateTime.parse("20260514193800 +0800", formatter))
                        .setStopTime(OffsetDateTime.parse("20260514195800 +0800", formatter))
                        .setTitle("焦点访谈")
                        .setLang("zh")
                ).addItem(new Channel.ChannelEPGTimelineItem()
                        .setChannel("CCTV 1")
                        .setType(Channel.ChannelEPGTimelineItem.Type.program)
                        .setStartTime(OffsetDateTime.parse("20260514195800 +0800", formatter))
                        .setStopTime(OffsetDateTime.parse("20260514200200 +0800", formatter))
                        .setTitle("前情提要-主角-9/48")
                        .setLang("zh")
                ).addItem(new Channel.ChannelEPGTimelineItem()
                        .setChannel("CCTV 1")
                        .setType(Channel.ChannelEPGTimelineItem.Type.program)
                        .setStartTime(OffsetDateTime.parse("20260514200200 +0800", formatter))
                        .setStopTime(OffsetDateTime.parse("20260514205300 +0800", formatter))
                        .setTitle("主角9/48")
                        .setLang("zh")
                ).addItem(new Channel.ChannelEPGTimelineItem()
                        .setChannel("CCTV 1")
                        .setType(Channel.ChannelEPGTimelineItem.Type.program)
                        .setStartTime(OffsetDateTime.parse("20260514205300 +0800", formatter))
                        .setStopTime(OffsetDateTime.parse("20260514205600 +0800", formatter))
                        .setTitle("前情提要-主角-10/48")
                        .setLang("zh")
                ).addItem(new Channel.ChannelEPGTimelineItem()
                        .setChannel("CCTV 1")
                        .setType(Channel.ChannelEPGTimelineItem.Type.program)
                        .setStartTime(OffsetDateTime.parse("20260514205600 +0800", formatter))
                        .setStopTime(OffsetDateTime.parse("20260514214600 +0800", formatter))
                        .setTitle("主角10/48")
                        .setLang("zh")
                ).addItem(new Channel.ChannelEPGTimelineItem()
                        .setChannel("CCTV 1")
                        .setType(Channel.ChannelEPGTimelineItem.Type.program)
                        .setStartTime(OffsetDateTime.parse("20260514214600 +0800", formatter))
                        .setStopTime(OffsetDateTime.parse("20260514215200 +0800", formatter))
                        .setTitle("非遗里的中国-MV")
                        .setLang("zh")
                ).addItem(new Channel.ChannelEPGTimelineItem()
                        .setChannel("CCTV 1")
                        .setType(Channel.ChannelEPGTimelineItem.Type.program)
                        .setStartTime(OffsetDateTime.parse("20260514215200 +0800", formatter))
                        .setStopTime(OffsetDateTime.parse("20260514220000 +0800", formatter))
                        .setTitle("三餐四季（第二季）-宣传片")
                        .setLang("zh")
                ).addItem(new Channel.ChannelEPGTimelineItem()
                        .setChannel("CCTV 1")
                        .setType(Channel.ChannelEPGTimelineItem.Type.program)
                        .setStartTime(OffsetDateTime.parse("20260514220000 +0800", formatter))
                        .setStopTime(OffsetDateTime.parse("20260514223500 +0800", formatter))
                        .setTitle("晚间新闻")
                        .setLang("zh")
                ).addItem(new Channel.ChannelEPGTimelineItem()
                        .setChannel("CCTV 1")
                        .setType(Channel.ChannelEPGTimelineItem.Type.program)
                        .setStartTime(OffsetDateTime.parse("20260514223500 +0800", formatter))
                        .setStopTime(OffsetDateTime.parse("20260514231000 +0800", formatter))
                        .setTitle("自然中国-探秘哀牢山-奇幻生灵")
                        .setLang("zh")
                ).addItem(new Channel.ChannelEPGTimelineItem()
                        .setChannel("CCTV 1")
                        .setType(Channel.ChannelEPGTimelineItem.Type.program)
                        .setStartTime(OffsetDateTime.parse("20260514231000 +0800", formatter))
                        .setStopTime(OffsetDateTime.parse("20260514235900 +0800", formatter))
                        .setTitle("宗师列传·大宋词人传-秦观")
                        .setLang("zh"));
        return result;
    }

    @Transactional
    public void dataClean() {
        List<OriginalChannelMetadata> originalChannelList = originalChannelMapper.findAll();
        List<Channel> channels = originalChannelList.stream()
                .map(o -> new Channel()
                        .setName(o.getName())
                        .setLogo(o.getTvGuideLogo())
                        .setUrl(o.getUrl())
                        .setGroupId(0L)
                        .setEpgSourceId(o.getTvGuideId())
                        .setStatus(Channel.Status.valid)
                        .setCountry(o.getTvGuideCountry())
                        .setLanguage(o.getTvGuideLanguage())
                        .setScore(100)
                        .setCreatedAt(o.getCreatedAt())
                        .setUpdatedAt(o.getUpdatedAt()))
                .toList();
        channelMapper.truncate();
        channelMapper.insert(channels);
    }

    public PageResult<Channel> findByQuery(ChannelQuery query) {
        Page<Channel> page = PageHelper.startPage(query.getPageNum(), query.getPageSize())
                .doSelectPage(() ->
                        channelMapper.findByCondition(query));
        return PageResult.of(page.getTotal(), page.getResult());
    }
}
