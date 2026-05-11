package com.lemongo97.iptv.iptvmanager.service;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import com.lemongo97.iptv.iptvmanager.common.BusinessException;
import com.lemongo97.iptv.iptvmanager.entity.M3U8Provider;
import com.lemongo97.iptv.iptvmanager.mapper.M3U8ProviderMapper;
import com.lemongo97.iptv.iptvmanager.parser.M3U8ParserService;
import com.lemongo97.iptv.iptvmanager.service.M3U8RawDataService;
import com.lemongo97.iptv.iptvmanager.quartz.ScheduledTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * M3U8 源服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class M3U8ProviderService {

    private final M3U8ProviderMapper providerMapper;
    private final M3U8ParserService parserService;
    private final M3U8RawDataService rawDataService;
    private final ScheduledTaskService scheduledTaskService;
    private final ObjectMapper objectMapper;

    /**
     * 获取所有 M3U8 源
     */
    public List<M3U8Provider> findAll() {
        return providerMapper.findAll();
    }

    /**
     * 根据 ID 获取 M3U8 源
     */
    public M3U8Provider findById(Long id) {
        return providerMapper.findById(id)
                .orElseThrow(() -> new BusinessException("M3U8 provider not found: id=" + id));
    }

    /**
     * 根据类型获取 M3U8 源
     */
    public List<M3U8Provider> findByType(String type) {
        return providerMapper.findByType(type);
    }

    /**
     * 获取启用的 M3U8 源
     */
    public List<M3U8Provider> findEnabled() {
        return providerMapper.findEnabled();
    }

    /**
     * 创建 M3U8 源
     */
    @Transactional
    public M3U8Provider create(M3U8Provider provider) {
        log.info("Creating M3U8 provider: {}", provider.name());

        // 验证类型和字段
        validateProvider(provider);

        var now = LocalDateTime.now();
        var newProvider = new M3U8Provider(
                null,
                provider.name(),
                provider.type(),
                provider.url(),
                provider.filePath(),
                provider.headers(),
                provider.refreshRate() != null ? provider.refreshRate() : 3600,
                provider.enabled() != null ? provider.enabled() : true,
                provider.description(),
                now,
                now,
                false
        );

        providerMapper.insert(newProvider);
        scheduledTaskService.scheduleOrUpdateJob(newProvider);
        log.info("M3U8 provider created: id={}", newProvider.id());
        return newProvider;
    }

    /**
     * 更新 M3U8 源
     */
    @Transactional
    public M3U8Provider update(Long id, M3U8Provider provider) {
        var existing = findById(id);
        log.info("Updating M3U8 provider: id={}", id);

        // 验证类型和字段
        validateProvider(provider);

        var updated = new M3U8Provider(
                id,
                provider.name() != null ? provider.name() : existing.name(),
                provider.type() != null ? provider.type() : existing.type(),
                provider.url(),
                provider.filePath(),
                provider.headers(),
                provider.refreshRate() != null ? provider.refreshRate() : existing.refreshRate(),
                provider.enabled() != null ? provider.enabled() : existing.enabled(),
                provider.description(),
                existing.createdAt(),
                LocalDateTime.now(),
                existing.deleted()
        );

        providerMapper.update(updated);
        scheduledTaskService.scheduleOrUpdateJob(updated);
        log.info("M3U8 provider updated: id={}", id);
        return updated;
    }

    /**
     * 删除 M3U8 源
     */
    @Transactional
    public void deleteById(Long id) {
        findById(id);
        log.info("Deleting M3U8 provider: id={}", id);
        // 先删除关联的定时任务
        scheduledTaskService.deleteJob(id);
        // 先删除关联的原始数据
        rawDataService.deleteByProviderId(id);
        providerMapper.deleteById(id);
    }

    /**
     * 刷新 M3U8 源
     */
    @Transactional
    public void refresh(Long id) {
        var provider = findById(id);
        log.info("Refreshing M3U8 provider: id={}, type={}", id, provider.type());

        if (!provider.enabled()) {
            throw new BusinessException("Cannot refresh disabled provider: id=" + id);
        }

        try {
            if ("online".equals(provider.type())) {
                parserService.parseFromUrl(provider);
            } else if ("local".equals(provider.type())) {
                parserService.parseFromFile(provider);
            } else {
                throw new BusinessException("Unknown provider type: " + provider.type());
            }

            log.info("M3U8 provider refreshed successfully: id={}", id);
        } catch (Exception e) {
            log.error("Failed to refresh M3U8 provider: id={}", id, e);
            throw new BusinessException("Failed to refresh M3U8 provider: " + e.getMessage(), e);
        }
    }

    /**
     * 验证 M3U8 源
     */
    private void validateProvider(M3U8Provider provider) {
        String type = provider.type();
        if (type == null) {
            throw new BusinessException("Provider type is required");
        }

        if (!"online".equals(type) && !"local".equals(type)) {
            throw new BusinessException("Invalid provider type: " + type + ", must be 'online' or 'local'");
        }

        if ("online".equals(type) && provider.url() == null) {
            throw new BusinessException("URL is required for online provider");
        }

        if ("local".equals(type) && provider.filePath() == null) {
            throw new BusinessException("File path is required for local provider");
        }

        // 验证 headers 是否为有效 JSON
        if (provider.headers() != null) {
            try {
                objectMapper.writeValueAsString(provider.headers());
            } catch (JacksonException e) {
                throw new BusinessException("Invalid headers format", e);
            }
        }
    }
}
