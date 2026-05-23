package com.lemongo97.iptv.iptvmanager.service;

import com.lemongo97.iptv.iptvmanager.common.BusinessException;
import com.lemongo97.iptv.iptvmanager.entity.EpgProgram;
import com.lemongo97.iptv.iptvmanager.entity.EpgSource;
import com.lemongo97.iptv.iptvmanager.mapper.EpgSourceMapper;
import com.lemongo97.iptv.iptvmanager.parser.EpgParserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * EPG 源服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EpgSourceService {

    private final EpgSourceMapper epgSourceMapper;
    private final EpgProgramService epgProgramService;
    private final EpgParserService epgParserService;

    public List<EpgSource> findAll() {
        return epgSourceMapper.findAll();
    }

    public EpgSource findById(Long id) {
        return epgSourceMapper.findById(id)
                .orElseThrow(() -> new BusinessException("EPG source not found: id=" + id));
    }

    public List<EpgSource> findEnabled() {
        return epgSourceMapper.findEnabled();
    }

    /**
     * 根据条件搜索 EPG 源
     */
    public List<EpgSource> findByCondition(String name) {
        return epgSourceMapper.findByCondition(name);
    }

    @Transactional
    public EpgSource create(EpgSource epgSource) {
        log.info("Creating EPG source: {}", epgSource.getName());

        var now = LocalDateTime.now();
        var newEpgSource = new EpgSource(
                null,
                epgSource.getName(),
                epgSource.getUrl(),
                epgSource.getType() != null ? epgSource.getType() : "xml",
                epgSource.getEnabled() != null ? epgSource.getEnabled() : true,
                epgSource.getDescription(),
                now,
                now,
                false
        );

        epgSourceMapper.insert(newEpgSource);
        log.info("EPG source created: id={}", newEpgSource.getId());
        return newEpgSource;
    }

    @Transactional
    public EpgSource update(Long id, EpgSource epgSource) {
        var existing = findById(id);
        log.info("Updating EPG source: id={}", id);

        var updated = new EpgSource(
                id,
                epgSource.getName() != null ? epgSource.getName() : existing.getName(),
                epgSource.getUrl() != null ? epgSource.getUrl() : existing.getUrl(),
                epgSource.getType() != null ? epgSource.getType() : existing.getType(),
                epgSource.getEnabled() != null ? epgSource.getEnabled() : existing.getEnabled(),
                epgSource.getDescription(),
                existing.getCreatedAt(),
                LocalDateTime.now(),
                existing.getDeleted()
        );

        epgSourceMapper.update(updated);
        log.info("EPG source updated: id={}", id);
        return updated;
    }

    @Transactional
    public void deleteById(Long id) {
        findById(id);
        log.info("Deleting EPG source: id={}", id);
        epgSourceMapper.deleteById(id);
    }

    @Transactional
    public void refresh(Long id) {
        var epgSource = findById(id);
        log.info("Refreshing EPG source: id={}, url={}", id, epgSource.getUrl());

        if (!epgSource.getEnabled()) {
            throw new BusinessException("Cannot refresh disabled EPG source: id=" + id);
        }

        try {
            // 1. 获取 EPG 数据
            String xmlContent = fetchEpgData(epgSource.getUrl());

            // 2. 解析 EPG 数据
            List<EpgProgram> programs = epgParserService.parse(xmlContent, id);

            // 3. 删除旧的节目数据
            epgProgramService.deleteBySourceId(id);

            // 4. 批量插入新的节目数据
            epgProgramService.insertBatch(programs);

            log.info("EPG source refreshed successfully: id={}, programs={}", id, programs.size());

        } catch (Exception e) {
            log.error("Failed to refresh EPG source: id={}, error={}", id, e.getMessage(), e);
            throw new BusinessException("Failed to refresh EPG source: " + e.getMessage(), e);
        }
    }

    /**
     * 从 URL 获取 EPG 数据
     */
    private String fetchEpgData(String url) {
        try {
            log.info("Fetching EPG data from: {}", url);

            // 使用 HttpClient 获取数据
            var request = new org.apache.hc.client5.http.classic.methods.HttpGet(url);
            try (var httpClient = org.apache.hc.client5.http.impl.classic.HttpClients.createDefault()) {
                var response = httpClient.execute(request);
                var entity = response.getEntity();
                if (entity == null) {
                    throw new BusinessException("No content returned from EPG source");
                }

                return org.apache.hc.core5.http.io.entity.EntityUtils.toString(entity, "UTF-8");
            }
        } catch (Exception e) {
            log.error("Failed to fetch EPG data from: {}", url, e);
            throw new BusinessException("Failed to fetch EPG data: " + e.getMessage(), e);
        }
    }
}
