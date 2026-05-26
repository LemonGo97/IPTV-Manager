package com.lemongo97.iptv.iptvmanager.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.lemongo97.iptv.iptvmanager.common.BusinessException;
import com.lemongo97.iptv.iptvmanager.common.PageResult;
import com.lemongo97.iptv.iptvmanager.controller.request.ChannelQuery;
import com.lemongo97.iptv.iptvmanager.engine.CleanEngineManager;
import com.lemongo97.iptv.iptvmanager.engine.RuleType;
import com.lemongo97.iptv.iptvmanager.entity.*;
import com.lemongo97.iptv.iptvmanager.mapper.*;
import com.lemongo97.iptv.iptvmanager.quartz.ScheduledTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 频道服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChannelService {

    private final IPTVProviderMapper IPTVProviderMapper;
    private final ChannelMapper channelMapper;
    private final OriginalChannelMapper originalChannelMapper;
    private final ChannelGroupMapper channelGroupMapper;
    private final CleanEngineManager cleanEngineManager;
    private final ScheduledTaskService scheduledTaskService;
    private final TaskProgressService taskProgressService;
    private final EpgChannelMapper epgChannelMapper;
    private final EpgProviderMapper epgProviderMapper;
    private final EpgProgramMapper epgProgramMapper;

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
        Map<String, Object> result = new HashMap<>();

        Map<String, Integer> statistics = channelMapper.statistics();
        result.putAll(statistics);

        int groupCount = channelGroupMapper.count();
        result.put("groupCount", groupCount);

        Optional<TaskProgress> latestTask = taskProgressService.getLatestTask("DATA_CLEANUP");
        if (latestTask.isPresent()) {
            result.put("status", latestTask.get().getStatus());
        } else {
            result.put("status", TaskProgress.Status.NOT_RUNNING);
        }

        return result;
    }

    /**
     * 获取节目EPG时间轴
     *
     * @param id
     * @return
     */
    public Channel.ChannelEPGTimeline getEPGTimeline(Long id) {

        Channel channel = channelMapper.findById(id)
                .orElseThrow(() -> new BusinessException("Channel not found: id=" + id));
        String channelName = channel.getName();

        List<EpgChannel> epgChannels = epgChannelMapper.findByChannelName(channelName);
        if (epgChannels.isEmpty()) {
            return new Channel.ChannelEPGTimeline();
        }

        EpgChannel epgChannel = epgChannels.getFirst();
        List<EpgProgram> epgPrograms = epgProgramMapper.findByChannelId(epgChannel.getEpgSourceId(), epgChannel.getChannelId());
        if (epgPrograms.isEmpty()) {
            return new Channel.ChannelEPGTimeline();
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss Z");
        DateTimeFormatter chineseDateFormatter = DateTimeFormatter.ofPattern("M'月'd'日' E", Locale.CHINESE);

        Channel.ChannelEPGTimeline timeline = new Channel.ChannelEPGTimeline();


        OffsetDateTime preStartTime = null;
        for (EpgProgram program : epgPrograms) {

            String channelLang = program.getTitle() == null ? null : program.getTitle().getLang();

            // 若上一个节目的开始时间与本节目开始时间不在同一天则添加一个时间节点对象
            OffsetDateTime startTime = OffsetDateTime.parse(program.getStartTime(), formatter);
            if (preStartTime == null || !DateUtils.isSameDay(Date.from(startTime.toInstant()), Date.from(preStartTime.toInstant()))) {
                timeline.addItem(new Channel.ChannelEPGTimelineItem()
                        .setChannel(channelName)
                        .setType(Channel.ChannelEPGTimelineItem.Type.date)
                        .setTitle(startTime.format(chineseDateFormatter))
                        .setLang(channelLang));
            }
            preStartTime = startTime;


            Channel.ChannelEPGTimelineItem item = new Channel.ChannelEPGTimelineItem()
                    .setChannel(channelName)
                    .setTitle(program.getTitle() == null ? null : program.getTitle().getValue())
                    .setType(Channel.ChannelEPGTimelineItem.Type.program)
                    .setStartTime(startTime)
                    .setStopTime(OffsetDateTime.parse(program.getStopTime(), formatter))
                    .setDescription(program.getDescription() == null || program.getDescription().isEmpty() ?
                            null : program.getDescription().getFirst().getValue())
                    .setLang(channelLang);
            timeline.addItem(item);
        }

        return timeline;
    }

    /**
     * 触发数据清洗任务
     * 通过 Quartz Job 异步执行数据清洗
     */
    public void dataClean(RuleType step) {
        log.info("Cleaning data... ==> {}", step);
        scheduledTaskService.triggerManualDataCleanupJob(step);
    }

    public PageResult<Channel> findByQuery(ChannelQuery query) {
        Page<Channel> page = PageHelper.startPage(query.getPageNum(), query.getPageSize())
                .doSelectPage(() ->
                        channelMapper.findByCondition(query));
        return PageResult.of(page.getTotal(), page.getResult());
    }

    public Map<String, ?> getOptions() {
        Map<String, Object> options = new HashMap<>();
        options.put("provider", IPTVProviderMapper.getProviderNames());
        options.put("group", channelGroupMapper.getGroupNames());
        return options;
    }
}
