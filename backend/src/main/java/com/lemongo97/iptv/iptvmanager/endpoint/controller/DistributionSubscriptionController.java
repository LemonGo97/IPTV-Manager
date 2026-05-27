package com.lemongo97.iptv.iptvmanager.endpoint.controller;

import com.lemongo97.iptv.iptvmanager.common.ApiResponse;
import com.lemongo97.iptv.iptvmanager.entity.DistributionSubscription;
import com.lemongo97.iptv.iptvmanager.service.DistributionSubscriptionService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 分发订阅控制器
 */
@RestController
@RequestMapping("/distribution/subscriptions")
public class DistributionSubscriptionController {

    private final DistributionSubscriptionService subscriptionService;

    public DistributionSubscriptionController(DistributionSubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    /**
     * 获取所有分发订阅
     */
    @GetMapping
    public ApiResponse<List<DistributionSubscription>> findAll(
            @RequestParam(required = false) String name) {
        if (name != null && !name.isEmpty()) {
            return ApiResponse.ok(subscriptionService.findByCondition(name));
        }
        return ApiResponse.ok(subscriptionService.findAll());
    }

    /**
     * 获取分发订阅总数
     */
    @GetMapping("/count")
    public ApiResponse<Integer> count() {
        return ApiResponse.ok(subscriptionService.count());
    }

    /**
     * 获取单个分发订阅
     */
    @GetMapping("/{id}")
    public ApiResponse<DistributionSubscription> findById(@PathVariable Long id) {
        return ApiResponse.ok(subscriptionService.findById(id));
    }

    /**
     * 创建分发订阅
     */
    @PostMapping
    public ApiResponse<DistributionSubscription> create(
            @RequestBody CreateSubscriptionRequest request) {
        DistributionSubscription subscription = new DistributionSubscription();
        subscription.setName(request.name());
        subscription.setUserId(request.userId());

        var created = subscriptionService.create(
                subscription,
                request.dateType(),
                request.customStartTime(),
                request.customEndTime()
        );
        return ApiResponse.ok(created, "Distribution subscription created successfully");
    }

    /**
     * 更新分发订阅
     */
    @PutMapping("/{id}")
    public ApiResponse<DistributionSubscription> update(
            @PathVariable Long id,
            @RequestBody UpdateSubscriptionRequest request) {
        DistributionSubscription subscription = new DistributionSubscription();
        subscription.setName(request.name());
        subscription.setUserId(request.userId());

        var updated = subscriptionService.update(
                id,
                subscription,
                request.dateType(),
                request.customStartTime(),
                request.customEndTime()
        );
        return ApiResponse.ok(updated, "Distribution subscription updated successfully");
    }

    /**
     * 删除分发订阅
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteById(@PathVariable Long id) {
        subscriptionService.deleteById(id);
        return ApiResponse.ok("Distribution subscription deleted successfully");
    }

    /**
     * 获取订阅链接
     */
    @GetMapping("/{id}/subscription-url")
    public ApiResponse<String> getSubscriptionUrl(@PathVariable Long id) {
        var subscription = subscriptionService.findById(id);
        String url = "/api/distribution/subscriptions/" + id + "/playlist.m3u8";
        return ApiResponse.<String>ok(url, "");
    }

    /**
     * 创建订阅请求
     */
    public record CreateSubscriptionRequest(
            String name,
            Long userId,
            DistributionSubscription.DateType dateType,
            LocalDateTime customStartTime,
            LocalDateTime customEndTime
    ) {}

    /**
     * 更新订阅请求
     */
    public record UpdateSubscriptionRequest(
            String name,
            Long userId,
            DistributionSubscription.DateType dateType,
            LocalDateTime customStartTime,
            LocalDateTime customEndTime
    ) {}
}
