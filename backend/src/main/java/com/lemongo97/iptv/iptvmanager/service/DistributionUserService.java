package com.lemongo97.iptv.iptvmanager.service;

import com.lemongo97.iptv.iptvmanager.common.BusinessException;
import com.lemongo97.iptv.iptvmanager.entity.DistributionUser;
import com.lemongo97.iptv.iptvmanager.mapper.DistributionUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 订阅用户服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DistributionUserService {

    private final DistributionUserMapper userMapper;

    public List<DistributionUser> findAll() {
        return userMapper.findAll();
    }

    public List<DistributionUser> findByCondition(String username) {
        return userMapper.findByCondition(username);
    }

    public int count() {
        return userMapper.count();
    }

    public DistributionUser findById(Long id) {
        return userMapper.findById(id)
                .orElseThrow(() -> new BusinessException("Distribution user not found: id=" + id));
    }

    public DistributionUser findByUserId(String userId) {
        return userMapper.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("Distribution user not found: userId=" + userId));
    }

    /**
     * 生成随机访问密钥
     */
    private String generateAccessKey() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    @Transactional
    public DistributionUser create(DistributionUser user) {
        log.info("Creating distribution user: {}", user.getUsername());

        // 检查用户名是否已存在
        userMapper.findByCondition(user.getUsername()).forEach(existingUser -> {
            if (existingUser.getUsername().equals(user.getUsername())) {
                throw new BusinessException("Username already exists: " + user.getUsername());
            }
        });

        var now = LocalDateTime.now();
        var newUser = new DistributionUser(
                null,
                user.getUsername(),
                UUID.randomUUID().toString(),
                user.getAccessKey() != null && !user.getAccessKey().isBlank()
                        ? user.getAccessKey()
                        : generateAccessKey(),
                now,
                now,
                false
        );

        userMapper.insert(newUser);
        log.info("Distribution user created: id={}, userId={}", newUser.getId(), newUser.getUserId());
        return newUser;
    }

    @Transactional
    public DistributionUser update(Long id, DistributionUser user) {
        var existing = findById(id);
        log.info("Updating distribution user: id={}", id);

        // 如果修改了用户名，检查新用户名是否已存在
        if (user.getUsername() != null && !user.getUsername().equals(existing.getUsername())) {
            userMapper.findByCondition(user.getUsername()).forEach(existingUser -> {
                if (existingUser.getUsername().equals(user.getUsername()) && !existingUser.getId().equals(id)) {
                    throw new BusinessException("Username already exists: " + user.getUsername());
                }
            });
        }

        var updated = new DistributionUser(
                id,
                user.getUsername() != null ? user.getUsername() : existing.getUsername(),
                existing.getUserId(),
                user.getAccessKey() != null && !user.getAccessKey().isBlank()
                        ? user.getAccessKey()
                        : existing.getAccessKey(),
                existing.getCreatedAt(),
                LocalDateTime.now(),
                existing.getDeleted()
        );

        userMapper.update(updated);
        log.info("Distribution user updated: id={}", id);
        return updated;
    }

    @Transactional
    public void deleteById(Long id) {
        findById(id);
        log.info("Deleting distribution user: id={}", id);
        userMapper.deleteById(id);
    }

    /**
     * 重置访问密钥
     */
    @Transactional
    public DistributionUser resetAccessKey(Long id) {
        var existing = findById(id);
        log.info("Resetting access key for distribution user: id={}", id);

        var updated = new DistributionUser(
                id,
                existing.getUsername(),
                existing.getUserId(),
                generateAccessKey(),
                existing.getCreatedAt(),
                LocalDateTime.now(),
                existing.getDeleted()
        );

        userMapper.update(updated);
        log.info("Access key reset for distribution user: id={}", id);
        return updated;
    }
}
