package com.lemongo97.iptv.iptvmanager.service;

import com.lemongo97.iptv.iptvmanager.entity.EpgProgram;
import com.lemongo97.iptv.iptvmanager.mapper.EpgProgramMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * EPG 节目服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EpgProgramService {

    private final EpgProgramMapper epgProgramMapper;

    /**
     * 获取指定源的所有节目
     */
    public List<EpgProgram> findBySourceId(Long sourceId) {
        return epgProgramMapper.findBySourceId(sourceId);
    }

    /**
     * 根据 ID 获取节目
     */
    public EpgProgram findById(Long id) {
        return epgProgramMapper.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("EPG program not found: id=" + id));
    }

    /**
     * 获取指定源的所有频道分组（用于前端树形展示）
     */
    public List<EpgProgramMapper.ChannelGroup> getChannelGroups(Long sourceId) {
        return epgProgramMapper.findChannelGroups(sourceId);
    }

    /**
     * 获取指定频道的节目列表
     */
    public List<EpgProgram> findByChannel(Long sourceId, String channelName) {
        return epgProgramMapper.findByChannelName(sourceId, channelName);
    }

    /**
     * 批量插入节目
     */
    public int insertBatch(List<EpgProgram> programs) {
        if (programs == null || programs.isEmpty()) {
            return 0;
        }
        log.info("Inserting {} EPG programs", programs.size());
        return epgProgramMapper.insertBatch(programs);
    }

    /**
     * 删除指定源的所有节目（逻辑删除）
     */
    public int deleteBySourceId(Long sourceId) {
        log.info("Deleting all EPG programs for source: id={}", sourceId);
        return epgProgramMapper.deleteBySourceId(sourceId);
    }
}
