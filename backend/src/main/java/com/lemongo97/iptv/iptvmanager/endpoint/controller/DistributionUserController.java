package com.lemongo97.iptv.iptvmanager.endpoint.controller;

import com.lemongo97.iptv.iptvmanager.common.ApiResponse;
import com.lemongo97.iptv.iptvmanager.entity.DistributionUser;
import com.lemongo97.iptv.iptvmanager.service.DistributionUserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订阅用户控制器
 */
@RestController
@RequestMapping("/distribution/users")
public class DistributionUserController {

    private final DistributionUserService userService;

    public DistributionUserController(DistributionUserService userService) {
        this.userService = userService;
    }

    /**
     * 获取所有订阅用户
     */
    @GetMapping
    public ApiResponse<List<DistributionUser>> findAll(
            @RequestParam(required = false) String username) {
        if (username != null && !username.isEmpty()) {
            return ApiResponse.ok(userService.findByCondition(username));
        }
        return ApiResponse.ok(userService.findAll());
    }

    /**
     * 获取订阅用户总数
     */
    @GetMapping("/count")
    public ApiResponse<Integer> count() {
        return ApiResponse.ok(userService.count());
    }

    /**
     * 获取单个订阅用户
     */
    @GetMapping("/{id}")
    public ApiResponse<DistributionUser> findById(@PathVariable Long id) {
        return ApiResponse.ok(userService.findById(id));
    }

    /**
     * 创建订阅用户
     */
    @PostMapping
    public ApiResponse<DistributionUser> create(@RequestBody DistributionUser user) {
        return ApiResponse.ok(userService.create(user), "Distribution user created successfully");
    }

    /**
     * 更新订阅用户
     */
    @PutMapping("/{id}")
    public ApiResponse<DistributionUser> update(@PathVariable Long id, @RequestBody DistributionUser user) {
        return ApiResponse.ok(userService.update(id, user), "Distribution user updated successfully");
    }

    /**
     * 删除订阅用户
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteById(@PathVariable Long id) {
        userService.deleteById(id);
        return ApiResponse.ok("Distribution user deleted successfully");
    }

    /**
     * 重置访问密钥
     */
    @PostMapping("/{id}/reset-key")
    public ApiResponse<DistributionUser> resetAccessKey(@PathVariable Long id) {
        return ApiResponse.ok(userService.resetAccessKey(id), "Access key reset successfully");
    }

    /**
     * 获取订阅链接
     */
    @GetMapping("/{id}/subscription-url")
    public ApiResponse<String> getSubscriptionUrl(@PathVariable Long id) {
        var user = userService.findById(id);
        String url = "/api/distribution/users/" + user.getUserId() + "/playlist.m3u8?key=" + user.getAccessKey();
        return ApiResponse.<String>ok(url, "");
    }
}
