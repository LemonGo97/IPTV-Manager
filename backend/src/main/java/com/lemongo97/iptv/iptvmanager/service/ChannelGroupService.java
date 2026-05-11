package com.lemongo97.iptv.iptvmanager.service;

import com.lemongo97.iptv.iptvmanager.common.BusinessException;
import com.lemongo97.iptv.iptvmanager.entity.ChannelGroup;
import com.lemongo97.iptv.iptvmanager.mapper.ChannelGroupMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 频道组服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChannelGroupService {

    private final ChannelGroupMapper groupMapper;

    public List<ChannelGroup> findAll() {
        return groupMapper.findAll();
    }

    public ChannelGroup findById(Long id) {
        return groupMapper.findById(id)
                .orElseThrow(() -> new BusinessException("Channel group not found: id=" + id));
    }

    @Transactional
    public ChannelGroup create(ChannelGroup group) {
        log.info("Creating channel group: {}", group.name());

        var now = LocalDateTime.now();
        var newGroup = new ChannelGroup(
                null,
                group.name(),
                group.sortOrder() != null ? group.sortOrder() : 0,
                group.description(),
                now,
                now,
                false
        );

        groupMapper.insert(newGroup);
        log.info("Channel group created: id={}", newGroup.id());
        return newGroup;
    }

    @Transactional
    public ChannelGroup update(Long id, ChannelGroup group) {
        var existing = findById(id);
        log.info("Updating channel group: id={}", id);

        var updated = new ChannelGroup(
                id,
                group.name() != null ? group.name() : existing.name(),
                group.sortOrder() != null ? group.sortOrder() : existing.sortOrder(),
                group.description(),
                existing.createdAt(),
                LocalDateTime.now(),
                existing.deleted()
        );

        groupMapper.update(updated);
        log.info("Channel group updated: id={}", id);
        return updated;
    }

    @Transactional
    public void deleteById(Long id) {
        findById(id);
        log.info("Deleting channel group: id={}", id);
        groupMapper.deleteById(id);
    }
}
