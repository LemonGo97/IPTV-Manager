package com.lemongo97.iptv.iptvmanager.mapper;

import com.lemongo97.iptv.iptvmanager.entity.EpgProgram;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * EPG 节目 Mapper
 */
@Mapper
public interface EpgProgramMapper {

    /**
     * 根据源 ID 查询所有节目
     */
    List<EpgProgram> findBySourceId(@Param("sourceId") Long sourceId);

    /**
     * 根据 ID 查询节目
     */
    Optional<EpgProgram> findById(@Param("id") Long id);

    /**
     * 根据频道名查询节目列表
     */
    List<EpgProgram> findByChannelName(@Param("sourceId") Long sourceId, @Param("channelName") String channelName);

    /**
     * 获取指定源的所有频道（按频道名分组）
     * 返回频道名称和节目数量的列表
     */
    List<ChannelGroup> findChannelGroups(@Param("sourceId") Long sourceId);

    /**
     * 插入单个节目
     */
    int insert(EpgProgram program);

    /**
     * 批量插入节目
     */
    int insertBatch(@Param("programs") List<EpgProgram> programs);

    /**
     * 删除指定源的所有节目（逻辑删除）
     */
    int deleteBySourceId(@Param("sourceId") Long sourceId);

    /**
     * 更新节目
     */
    int update(EpgProgram program);

    /**
     * 频道分组记录
     */
    record ChannelGroup(
        String channelName,
        Integer programCount
    ) {}
}
