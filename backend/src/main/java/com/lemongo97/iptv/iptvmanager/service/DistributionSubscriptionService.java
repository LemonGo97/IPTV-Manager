package com.lemongo97.iptv.iptvmanager.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.lemongo97.iptv.iptvmanager.common.BusinessException;
import com.lemongo97.iptv.iptvmanager.common.PageResult;
import com.lemongo97.iptv.iptvmanager.endpoint.controller.request.DistributionSubscriptionQuery;
import com.lemongo97.iptv.iptvmanager.entity.Channel;
import com.lemongo97.iptv.iptvmanager.entity.DistributionSubscription;
import com.lemongo97.iptv.iptvmanager.entity.DistributionUser;
import com.lemongo97.iptv.iptvmanager.mapper.DistributionSubscriptionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 分发订阅服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DistributionSubscriptionService {

    private final DistributionSubscriptionMapper subscriptionMapper;
    private final DistributionUserService userService;

    public PageResult<DistributionSubscription> findByCondition(DistributionSubscriptionQuery query) {
        Page<DistributionSubscription> page = PageHelper.startPage(query.getPageNum(), query.getPageSize())
                .doSelectPage(() ->
                        subscriptionMapper.findByCondition(query));
        return PageResult.of(page.getTotal(), page.getResult());
    }

    public int count() {
        return subscriptionMapper.count();
    }

    public DistributionSubscription findById(Long id) {
        return subscriptionMapper.findById(id)
                .orElseThrow(() -> new BusinessException("Distribution subscription not found: id=" + id));
    }

    /**
     * 计算订阅结束时间
     */
    private LocalDateTime calculateEndTime(DistributionSubscription.DateType dateType, LocalDateTime customEndTime) {
        return switch (dateType) {
            case MONTH -> LocalDateTime.now().plus(1, ChronoUnit.MONTHS);
            case QUARTER -> LocalDateTime.now().plus(3, ChronoUnit.MONTHS);
            case HALF_YEAR -> LocalDateTime.now().plus(6, ChronoUnit.MONTHS);
            case YEAR -> LocalDateTime.now().plus(1, ChronoUnit.YEARS);
            case FOREVER -> null;
            case CUSTOM -> customEndTime;
        };
    }

    /**
     * 计算订阅开始时间
     */
    private LocalDateTime calculateStartTime(DistributionSubscription.DateType dateType, LocalDateTime customStartTime) {
        if (dateType == DistributionSubscription.DateType.CUSTOM) {
            return customStartTime != null ? customStartTime : LocalDateTime.now();
        }
        return LocalDateTime.now();
    }

    @Transactional
    public DistributionSubscription create(DistributionSubscription subscription) {
        log.info("Creating distribution subscription: {}", subscription.getName());

        // 验证用户是否存在
        DistributionUser user = userService.findById(subscription.getUserId());

        var startTime = calculateStartTime(subscription.getDateType(), subscription.getStartTime());
        var endTime = calculateEndTime(subscription.getDateType(), subscription.getEndTime());

        var now = LocalDateTime.now();
        var newSubscription = new DistributionSubscription(
                null,
                subscription.getName(),
                user.getId(),
                subscription.getDateType(),
                startTime,
                endTime,
                // 高级设置 - 使用前端传来的值或默认值
                subscription.getFilterInvalidChannels() != null ? subscription.getFilterInvalidChannels() : true,
                subscription.getFilterHttpHighDelay() != null ? subscription.getFilterHttpHighDelay() : -1,
                subscription.getFilterFfmpegHighDelay() != null ? subscription.getFilterFfmpegHighDelay() : -1,
                subscription.getFilterNoVideoStream() != null ? subscription.getFilterNoVideoStream() : true,
                subscription.getFilterNoAudioStream() != null ? subscription.getFilterNoAudioStream() : true,
                subscription.getFilterLowResolution() != null ? subscription.getFilterLowResolution() : "1080p",
                subscription.getMergeSameChannels() != null ? subscription.getMergeSameChannels() : true,
                now,
                now,
                false,
                null
        );

        subscriptionMapper.insert(newSubscription);
        log.info("Distribution subscription created: id={}, userId={}", newSubscription.getId(), user.getId());
        return newSubscription;
    }

    @Transactional
    public DistributionSubscription update(Long id, DistributionSubscription subscription) {
        var existing = findById(id);
        log.info("Updating distribution subscription: id={}", id);

        // 如果修改了用户，验证新用户是否存在
        Long userId = subscription.getUserId() != null ? subscription.getUserId() : existing.getUserId();
        userService.findById(userId);

        var startTime = calculateStartTime(subscription.getDateType(), subscription.getStartTime());
        var endTime = calculateEndTime(subscription.getDateType(), subscription.getEndTime());

        var updated = new DistributionSubscription(
                id,
                subscription.getName() != null ? subscription.getName() : existing.getName(),
                userId,
                subscription.getDateType(),
                startTime,
                endTime,
                // 高级设置 - 使用前端传来的值或保留现有值
                subscription.getFilterInvalidChannels() != null ? subscription.getFilterInvalidChannels() : existing.getFilterInvalidChannels(),
                subscription.getFilterHttpHighDelay() != null ? subscription.getFilterHttpHighDelay() : existing.getFilterHttpHighDelay(),
                subscription.getFilterFfmpegHighDelay() != null ? subscription.getFilterFfmpegHighDelay() : existing.getFilterFfmpegHighDelay(),
                subscription.getFilterNoVideoStream() != null ? subscription.getFilterNoVideoStream() : existing.getFilterNoVideoStream(),
                subscription.getFilterNoAudioStream() != null ? subscription.getFilterNoAudioStream() : existing.getFilterNoAudioStream(),
                subscription.getFilterLowResolution() != null ? subscription.getFilterLowResolution() : existing.getFilterLowResolution(),
                subscription.getMergeSameChannels() != null ? subscription.getMergeSameChannels() : existing.getMergeSameChannels(),
                existing.getCreatedAt(),
                LocalDateTime.now(),
                existing.getDeleted(),
                null
        );

        subscriptionMapper.update(updated);
        log.info("Distribution subscription updated: id={}", id);
        return updated;
    }

    @Transactional
    public void deleteById(Long id) {
        findById(id);
        log.info("Deleting distribution subscription: id={}", id);
        subscriptionMapper.deleteById(id);
    }

    /**
     * 获取用户的订阅列表
     */
    public List<DistributionSubscription> findByUserId(Long userId) {
        return subscriptionMapper.findByUserId(userId);
    }

    /**
     * 验证订阅是否有效
     */
    public boolean isValidSubscription(Long subscriptionId) {
        var subscription = findById(subscriptionId);
        var now = LocalDateTime.now();

        // 检查开始时间
        if (subscription.getStartTime().isAfter(now)) {
            return false;
        }

        // 检查结束时间（null 表示永久）
        if (subscription.getEndTime() != null && subscription.getEndTime().isBefore(now)) {
            return false;
        }

        return true;
    }
}
