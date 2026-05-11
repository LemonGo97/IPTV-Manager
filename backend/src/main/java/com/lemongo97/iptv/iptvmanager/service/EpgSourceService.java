package com.lemongo97.iptv.iptvmanager.service;

import com.lemongo97.iptv.iptvmanager.common.BusinessException;
import com.lemongo97.iptv.iptvmanager.entity.EpgSource;
import com.lemongo97.iptv.iptvmanager.mapper.EpgSourceMapper;
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

    @Transactional
    public EpgSource create(EpgSource epgSource) {
        log.info("Creating EPG source: {}", epgSource.name());

        var now = LocalDateTime.now();
        var newEpgSource = new EpgSource(
                null,
                epgSource.name(),
                epgSource.url(),
                epgSource.type() != null ? epgSource.type() : "xml",
                epgSource.enabled() != null ? epgSource.enabled() : true,
                epgSource.description(),
                now,
                now,
                false
        );

        epgSourceMapper.insert(newEpgSource);
        log.info("EPG source created: id={}", newEpgSource.id());
        return newEpgSource;
    }

    @Transactional
    public EpgSource update(Long id, EpgSource epgSource) {
        var existing = findById(id);
        log.info("Updating EPG source: id={}", id);

        var updated = new EpgSource(
                id,
                epgSource.name() != null ? epgSource.name() : existing.name(),
                epgSource.url() != null ? epgSource.url() : existing.url(),
                epgSource.type() != null ? epgSource.type() : existing.type(),
                epgSource.enabled() != null ? epgSource.enabled() : existing.enabled(),
                epgSource.description(),
                existing.createdAt(),
                LocalDateTime.now(),
                existing.deleted()
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
        log.info("Refreshing EPG source: id={}, url={}", id, epgSource.url());

        if (!epgSource.enabled()) {
            throw new BusinessException("Cannot refresh disabled EPG source: id=" + id);
        }

        // TODO: 实现 EPG 解析和入库逻辑

        log.info("EPG source refreshed successfully: id={}", id);
    }
}
