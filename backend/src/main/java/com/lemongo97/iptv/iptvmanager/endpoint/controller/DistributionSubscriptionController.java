package com.lemongo97.iptv.iptvmanager.endpoint.controller;

import com.lemongo97.iptv.iptvmanager.common.ApiResponse;
import com.lemongo97.iptv.iptvmanager.common.PageResult;
import com.lemongo97.iptv.iptvmanager.endpoint.controller.request.DistributionSubscriptionQuery;
import com.lemongo97.iptv.iptvmanager.entity.DistributionSubscription;
import com.lemongo97.iptv.iptvmanager.service.DistributionSubscriptionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpRequest;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 分发订阅控制器
 */
@RestController
@RequestMapping("/distribution")
public class DistributionSubscriptionController {

    private final DistributionSubscriptionService subscriptionService;

    public DistributionSubscriptionController(DistributionSubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    /**
     * 获取所有分发订阅
     */
    @GetMapping("/subscriptions")
    public ApiResponse<PageResult<DistributionSubscription>> findAll(DistributionSubscriptionQuery query) {
        return ApiResponse.ok(subscriptionService.findByCondition(query));
    }

    /**
     * 获取分发订阅总数
     */
    @GetMapping("/subscriptions/count")
    public ApiResponse<Integer> count() {
        return ApiResponse.ok(subscriptionService.count());
    }

    /**
     * 获取单个分发订阅
     */
    @GetMapping("/subscriptions/{id}")
    public ApiResponse<DistributionSubscription> findById(@PathVariable Long id) {
        return ApiResponse.ok(subscriptionService.findById(id));
    }

    /**
     * 创建分发订阅
     */
    @PostMapping("/subscriptions")
    public ApiResponse<DistributionSubscription> create(
            @RequestBody DistributionSubscription subscription) {

        var created = subscriptionService.create(
                subscription
        );
        return ApiResponse.ok(created, "Distribution subscription created successfully");
    }

    /**
     * 更新分发订阅
     */
    @PutMapping("/subscriptions/{id}")
    public ApiResponse<DistributionSubscription> update(
            @PathVariable Long id,
            @RequestBody DistributionSubscription subscription) {
        var updated = subscriptionService.update(
                id,
                subscription
        );
        return ApiResponse.ok(updated, "Distribution subscription updated successfully");
    }

    /**
     * 删除分发订阅
     */
    @DeleteMapping("/subscriptions/{id}")
    public ApiResponse<Void> deleteById(@PathVariable Long id) {
        subscriptionService.deleteById(id);
        return ApiResponse.ok("Distribution subscription deleted successfully");
    }

    /**
     * 获取订阅链接
     */
    @GetMapping("/subscriptions/{id}/subscription-url")
    public ApiResponse<String> getSubscriptionUrl(@PathVariable Long id, HttpServletRequest request) {
        String url = subscriptionService.getSubscriptionUrl(id, request);
        if (url == null) {
            return ApiResponse.error("No subscription found");
        }
        return ApiResponse.<String>ok(url, "");
    }

    @GetMapping("/subscription/{id}")
    public void getPlaylist(@PathVariable Long id, String uid, String accessKey, HttpServletResponse response) throws IOException {
        subscriptionService.getPlaylist(id, uid, accessKey, response);
    }
}
