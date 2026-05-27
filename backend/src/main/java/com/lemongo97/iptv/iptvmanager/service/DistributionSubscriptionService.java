package com.lemongo97.iptv.iptvmanager.service;

import com.lemongo97.iptv.iptvmanager.common.BusinessException;
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

    public List<DistributionSubscription> findAll() {
        return subscriptionMapper.findAll();
    }

    public List<DistributionSubscription> findByCondition(String name) {
        return subscriptionMapper.findByCondition(name);
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
     * @param validityType 有效期类型：1month, 3month, 6month, 1year, custom, forever
     * @param customStartTime 自定义开始时间（仅当 validityType=custom 时使用）
     * @param customEndTime 自定义结束时间（仅当 validityType=custom 时使用）
     * @return 结束时间，null 表示永久
     */
    private LocalDateTime calculateEndTime(String validityType, LocalDateTime customStartTime, LocalDateTime customEndTime) {
        return switch (validityType) {
            case "1month" -> LocalDateTime.now().plus(1, ChronoUnit.MONTHS);
            case "3month" -> LocalDateTime.now().plus(3, ChronoUnit.MONTHS);
            case "6month" -> LocalDateTime.now().plus(6, ChronoUnit.MONTHS);
            case "1year" -> LocalDateTime.now().plus(1, ChronoUnit.YEARS);
            case "forever" -> null;
            case "custom" -> customEndTime;
            default -> throw new BusinessException("Invalid validity type: " + validityType);
        };
    }

    /**
     * 计算订阅开始时间
     * @param validityType 有效期类型
     * @param customStartTime 自定义开始时间
     * @return 开始时间
     */
    private LocalDateTime calculateStartTime(String validityType, LocalDateTime customStartTime) {
        if ("custom".equals(validityType)) {
            return customStartTime != null ? customStartTime : LocalDateTime.now();
        }
        return LocalDateTime.now();
    }

    @Transactional
    public DistributionSubscription create(DistributionSubscription subscription, String validityType,
                                          LocalDateTime customStartTime, LocalDateTime customEndTime) {
        log.info("Creating distribution subscription: {}", subscription.getName());

        // 验证用户是否存在
        DistributionUser user = userService.findById(subscription.getUserId());

        var startTime = calculateStartTime(validityType, customStartTime);
        var endTime = calculateEndTime(validityType, customStartTime, customEndTime);

        var now = LocalDateTime.now();
        var newSubscription = new DistributionSubscription(
                null,
                subscription.getName(),
                user.getId(),
                startTime,
                endTime,
                now,
                now,
                false
        );

        subscriptionMapper.insert(newSubscription);
        log.info("Distribution subscription created: id={}, userId={}", newSubscription.getId(), user.getId());
        return newSubscription;
    }

    @Transactional
    public DistributionSubscription update(Long id, DistributionSubscription subscription, String validityType,
                                          LocalDateTime customStartTime, LocalDateTime customEndTime) {
        var existing = findById(id);
        log.info("Updating distribution subscription: id={}", id);

        // 如果修改了用户，验证新用户是否存在
        Long userId = subscription.getUserId() != null ? subscription.getUserId() : existing.getUserId();
        userService.findById(userId);

        var startTime = calculateStartTime(validityType, customStartTime);
        var endTime = calculateEndTime(validityType, customStartTime, customEndTime);

        var updated = new DistributionSubscription(
                id,
                subscription.getName() != null ? subscription.getName() : existing.getName(),
                userId,
                startTime,
                endTime,
                existing.getCreatedAt(),
                LocalDateTime.now(),
                existing.getDeleted()
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
