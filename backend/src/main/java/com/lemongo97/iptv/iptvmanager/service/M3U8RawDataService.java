package com.lemongo97.iptv.iptvmanager.service;

import com.lemongo97.iptv.iptvmanager.entity.M3U8RawData;
import com.lemongo97.iptv.iptvmanager.mapper.M3U8RawDataMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * M3U8 原始数据服务
 * 负责管理 M3U8 文件的原始内容历史，保留最近3次数据
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class M3U8RawDataService {

    private final M3U8RawDataMapper rawDataMapper;
    private static final int MAX_HISTORY_COUNT = 3;

    /**
     * 保存原始数据
     * 保存后会自动清理旧数据，只保留最近的3次
     */
    @Transactional
    public void saveRawData(Long providerId, String content) {
        var now = LocalDateTime.now();
        var rawData = new M3U8RawData(null, providerId, content, now, false);
        rawDataMapper.insert(rawData);
        log.debug("Saved raw data for provider: {}, size: {} bytes", providerId, content.length());

        // 清理旧数据，只保留最近3次
        cleanupOldData(providerId);
    }

    /**
     * 获取指定提供者的原始数据历史（最近的记录在前）
     */
    public List<M3U8RawData> getHistory(Long providerId) {
        return rawDataMapper.findByProviderIdOrderByFetchedAtDesc(providerId);
    }

    /**
     * 获取最近的原始数据
     */
    public M3U8RawData getLatest(Long providerId) {
        var history = getHistory(providerId);
        return history.isEmpty() ? null : history.get(0);
    }

    /**
     * 清理旧数据，只保留最近的3次记录
     */
    private void cleanupOldData(Long providerId) {
        // 获取所有历史记录
        var history = rawDataMapper.findByProviderIdOrderByFetchedAtDesc(providerId);

        // 如果超过3条，删除多余的
        if (history.size() > MAX_HISTORY_COUNT) {
            // 保留第3条记录的时间戳，删除此时间之前的所有数据
            LocalDateTime cutoffTime = history.get(MAX_HISTORY_COUNT - 1).getFetchedAt();
            int deletedCount = rawDataMapper.deleteByFetchedAtBefore(cutoffTime);
            log.debug("Cleaned up {} old raw data records for provider: {}", deletedCount, providerId);
        }
    }

    /**
     * 删除指定提供者的所有原始数据
     */
    @Transactional
    public void deleteByProviderId(Long providerId) {
        int deletedCount = rawDataMapper.deleteByProviderId(providerId);
        log.info("Deleted {} raw data records for provider: {}", deletedCount, providerId);
    }

    /**
     * 检查指定提供者在指定时间之后的数据数量
     */
    public long countAfter(Long providerId, LocalDateTime fetchedAt) {
        return rawDataMapper.countByProviderIdAfterFetchedAt(providerId, fetchedAt);
    }
}
