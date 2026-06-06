package com.lemongo97.iptv.iptvmanager.service;

import com.lemongo97.iptv.iptvmanager.cleanup.CleanEngineManager;
import com.lemongo97.iptv.iptvmanager.cleanup.config.CleanupEngineConfig;
import com.lemongo97.iptv.iptvmanager.cleanup.rule.RuleType;
import com.lemongo97.iptv.iptvmanager.entity.Channel;
import com.lemongo97.iptv.iptvmanager.entity.CleanupRule;
import com.lemongo97.iptv.iptvmanager.entity.OriginalChannelMetadata;
import com.lemongo97.iptv.iptvmanager.entity.TaskProgress;
import com.lemongo97.iptv.iptvmanager.mapper.ChannelCleaningTempMapper;
import com.lemongo97.iptv.iptvmanager.mapper.ChannelMapper;
import com.lemongo97.iptv.iptvmanager.mapper.CleanupRuleMapper;
import com.lemongo97.iptv.iptvmanager.mapper.OriginalChannelMapper;
import com.lemongo97.iptv.iptvmanager.quartz.job.params.ChannelCleanupJobParams;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@AllArgsConstructor
public class ChannelCleanupJobService implements OriginalChannelCover {

    private static final int batchSize = 50;

    private final CleanupRuleMapper cleanupRuleMapper;
    private final ChannelMapper channelMapper;
    private final OriginalChannelMapper originalChannelMapper;
    private final CleanEngineManager cleanEngineManager;
    private final ChannelCleaningTempMapper channelCleaningTempMapper;
    private final TaskProgressService taskProgressService;

    public int execute(ChannelCleanupJobParams jobParams) {
        log.info("Starting data cleanup process");

        // 1. 创建任务跟踪器
        TaskProgress task = taskProgressService.createTask("DATA_CLEANUP", null, "初始化数据清洗任务");
        String taskId = task.getTaskId();
        taskProgressService.startTask(taskId);

        try {
            // 2. 清空中间表（物理删除）
            channelCleaningTempMapper.truncate();
            log.info("Channel cleanup: truncated temp table: channel_cleaning_temp");
            taskProgressService.updateProgress(taskId, 0, 5d, "已清空临时表");

            // 3. 获取清洗规则
            List<CleanupEngineConfig> ruleList = this.prepareCleanupRuleList(jobParams);
            log.debug("Channel cleanup: Found {} enabled cleanup rules", ruleList.size());
            if (CollectionUtils.isEmpty(ruleList)) {
                log.info("Channel cleanup: no cleanup rules found");
                taskProgressService.completeTask(taskId, "数据清洗完成，加载了 0 条规则，未改动任何数据");
                return 0;
            }
            taskProgressService.updateProgress(taskId, 0, 10d, "加载清洗规则完成");

            // 4. 获取待处理的 Channel
            List<Channel> channels = this.prepareChannelList(jobParams);
            if (CollectionUtils.isEmpty(channels)) {
                log.info("Channel cleanup: no channel ready found to data cleanup");
                taskProgressService.completeTask(taskId, "数据清洗完成，成功处理 0 个频道，未改动任何数据");
                return 0;
            }

            int totalChannels = channels.size();
            log.info("Channel cleanup: found {} enabled cleanup rules, found {} channel ready to cleanup..", ruleList.size(), totalChannels);
            taskProgressService.updateProgress(taskId, 0, 15d, "找到 " + totalChannels + " 个原始频道");

            // 信号量，通知执行数据清洗的线程数
            Semaphore cleanupSemaphore = new Semaphore(batchSize);
            // 共享阻塞队列，数据清洗和数据插入线程共享
            BlockingQueue<Channel> cleanedChannels = new LinkedBlockingQueue<>(batchSize * 10);
            // 数据清洗总批次数
//            int totalBatches = (int) Math.ceil((double) totalChannels / batchSize);
            CountDownLatch latch = new CountDownLatch(totalChannels);
            AtomicInteger pilledCount = new AtomicInteger(totalChannels);
            // 使用虚拟线程完成数据清洗
            try (ExecutorService vExecutor = Executors.newVirtualThreadPerTaskExecutor()) {

                    // 数据清洗守护线程
                    vExecutor.submit(() -> {
                        for (Channel channel : channels) {
                            log.debug("Channel cleanup: trying to clean up channel {}", channel);
                            try{
                                cleanupSemaphore.acquire();
                                // 获取信号量后，提交任务到虚拟线程
                                vExecutor.submit(() -> {
                                    try{
                                        Channel cleanupd = cleanEngineManager.process(channel, ruleList);
                                        log.debug("Channel cleanup: 已处理：{}", channel);
                                        if (cleanupd != null) cleanedChannels.put(cleanupd);
                                        pilledCount.decrementAndGet();
                                    }catch (Exception e){
                                        log.error("Channel cleanup: failed to process channel {}", channel, e);
                                    }finally {
                                        latch.countDown();
                                        cleanupSemaphore.release();
                                    }
                                });
                            }catch (Exception e){
                                log.error("Channel cleanup: failed to process channel {}", channel, e);
                                Thread.currentThread().interrupt();
                            }
                        }
                    });

                    // 数据插入线程，当前暂时插入临时表
                    vExecutor.submit(() -> {
                        Channel firstData;
                        while (pilledCount.get() > 0 || !cleanedChannels.isEmpty()) {
                            List<Channel> batchList = new ArrayList<>(batchSize);

                            try{
                                firstData = cleanedChannels.poll(2, TimeUnit.SECONDS);
                                if (firstData == null) continue;

                                batchList.add(firstData);
                                cleanedChannels.drainTo(batchList, batchSize - 1);
                                log.debug("Channel cleanup: ready for insert {} cleaned channel data to temp table", batchList.size());
                                channelCleaningTempMapper.insertBatch(batchList);
                            } catch (Exception e){
                                log.error("Channel cleanup: failed to insert cleaned channel data to temp table", e);
                                Thread.currentThread().interrupt();
                            }
                        }

                        log.debug("Channel cleanup: exit this virtual thread, because latch.getCount() is {}", latch.getCount());
                    });

                    // 任务进度监视器
                    vExecutor.submit(() -> {
                        double lastProgressUpdate = 0;
                        while (latch.getCount() > 0) {
                            int processedCount = Math.toIntExact(totalChannels - latch.getCount());
                            double currentProgress = 15 + ((totalChannels - latch.getCount()) * 70.0 / totalChannels);

                            if (currentProgress > lastProgressUpdate) {
                                log.info("Processed {}/{} channels, {} valid so far",processedCount, totalChannels, processedCount);
                                taskProgressService.updateProgress(
                                        taskId,
                                        processedCount,
                                        currentProgress,
                                        "已处理 " + processedCount + "/" + totalChannels + " 个频道"
                                );
                                lastProgressUpdate = currentProgress;
                            } else {
                                try { Thread.sleep(10); } catch (InterruptedException ignore) {}
                            }
                        }
                    });

                // 等待所有任务结束
                latch.await();
            }catch (Exception e) {
                log.error("Channel cleanup: failed to start cleanup", e);
            }

            taskProgressService.updateProgress(taskId, Math.toIntExact(totalChannels - latch.getCount()), 90d, "正在保存到正式表");

            // 从临时表获取所有数据
            List<Channel> tempChannels = channelCleaningTempMapper.findAll();

            // 检查是否有从channel表中拿到的数据进行数据清洗，若有，则先删除对应数据再进行插入
            List<Long> channelIds = channels.stream().map(Channel::getId).filter(Objects::nonNull).toList();
            if (CollectionUtils.isNotEmpty(tempChannels)) {
                channelMapper.deleteByIds(channelIds);
            }

            // 批量插入新数据
            channelMapper.insert(tempChannels, batchSize);
            taskProgressService.completeTask(taskId, "数据清洗完成，成功处理 " + tempChannels.size() + " 个频道");

            return tempChannels.size();
        } catch (Exception e) {
            log.error("Data cleanup failed", e);
            taskProgressService.failTask(taskId, "数据清洗失败: " + e.getMessage());
            throw e;
        }
    }

    private List<CleanupEngineConfig> prepareCleanupRuleList(ChannelCleanupJobParams jobParams) {
        return this.cleanupRuleMapper.prepareCleanupRuleList(jobParams).stream().map(this::toEngineConfig).toList();
    }

    private List<Channel> prepareChannelList(ChannelCleanupJobParams jobParams) {
        List<Channel> channelList = this.channelMapper.prepareChannelCleanupList(jobParams);
        if (CollectionUtils.isEmpty(channelList)) {
            // 从元数据中获取频道, 此时 channelId 参数已失效
            log.debug("Channel cleanup: from channel table prepared empty channel list, prepare to get channel list from original channel metadata list");
            List<OriginalChannelMetadata> originalChannelMetadataList = this.originalChannelMapper.prepareChannelCleanupList(jobParams);
            channelList = this.toChannel(originalChannelMetadataList);
            log.debug("Channel cleanup: from original channel metadata list, get {} channels", channelList.size());
        }
        log.info("Channel cleanup: preparing {} channels", channelList.size());
        return channelList;
    }

    /**
     * 将 CleanupRule 转换为 EngineConfig
     */
    private CleanupEngineConfig toEngineConfig(CleanupRule rule) {
        RuleType ruleType;
        try {
            ruleType = RuleType.valueOf(rule.getRuleType().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid rule type: {}, defaulting to FILTER", rule.getRuleType());
            ruleType = RuleType.FILTER;
        }

        CleanupEngineConfig config = new CleanupEngineConfig();
        config.setId(rule.getId());
        config.setRuleType(ruleType);
        config.setEngine(rule.getEngine());
        config.setConfigParams(rule.getParams());
        config.setSortOrder(rule.getSortOrder());
        return config;
    }

}
