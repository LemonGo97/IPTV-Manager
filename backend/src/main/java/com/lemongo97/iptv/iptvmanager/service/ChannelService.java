package com.lemongo97.iptv.iptvmanager.service;

import com.lemongo97.iptv.iptvmanager.common.BusinessException;
import com.lemongo97.iptv.iptvmanager.entity.Channel;
import com.lemongo97.iptv.iptvmanager.mapper.ChannelMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 频道服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChannelService {

    private final ChannelMapper channelMapper;

    /**
     * 获取所有频道
     */
    public List<Channel> findAll() {
        return channelMapper.findAll();
    }

    /**
     * 根据 ID 获取频道
     */
    public Channel findById(Long id) {
        return channelMapper.findById(id)
                .orElseThrow(() -> new BusinessException("Channel not found: id=" + id));
    }

    /**
     * 根据分组获取频道
     */
    public List<Channel> findByGroup(String group) {
        return channelMapper.findByGroup(group);
    }

    /**
     * 创建频道
     */
    public Channel create(Channel channel) {
        log.info("Creating channel: {}", channel.name());
        var now = LocalDateTime.now();
        var newChannel = new Channel(
                null,
                channel.name(),
                channel.logo(),
                channel.url(),
                channel.number(),
                channel.channelId(),
                channel.enabled() != null ? channel.enabled() : true,
                channel.providerId(),
                channel.groupId(),
                channel.epgSourceId(),
                channel.sortOrder(),
                channel.description(),
                now,
                now,
                false
        );
        channelMapper.insert(newChannel);
        return newChannel;
    }

    /**
     * 更新频道
     */
    public Channel update(Long id, Channel channel) {
        var existing = findById(id);
        log.info("Updating channel: id={}", id);
        var updated = new Channel(
                id,
                channel.name() != null ? channel.name() : existing.name(),
                channel.logo(),
                channel.url() != null ? channel.url() : existing.url(),
                channel.number(),
                channel.channelId(),
                channel.enabled() != null ? channel.enabled() : existing.enabled(),
                existing.providerId(),
                channel.groupId() != null ? channel.groupId() : existing.groupId(),
                channel.epgSourceId() != null ? channel.epgSourceId() : existing.epgSourceId(),
                channel.sortOrder() != null ? channel.sortOrder() : existing.sortOrder(),
                channel.description(),
                existing.createdAt(),
                LocalDateTime.now(),
                existing.deleted()
        );
        channelMapper.update(updated);
        return updated;
    }

    /**
     * 删除频道
     */
    public void deleteById(Long id) {
        findById(id);
        log.info("Deleting channel: id={}", id);
        channelMapper.deleteById(id);
    }
}
