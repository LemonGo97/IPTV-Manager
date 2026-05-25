package com.lemongo97.iptv.iptvmanager.mapper;

import com.lemongo97.iptv.iptvmanager.entity.EpgChannel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * EPG 节目 Mapper
 */
@Mapper
public interface EpgChannelMapper {

    /**
     * 根据源 ID 查询所有节目
     */
    List<EpgChannel> findBySourceId(@Param("sourceId") Long sourceId);

    /**
     * 根据 ID 查询节目
     */
    Optional<EpgChannel> findById(@Param("id") Long id);

    /**
     * 根据频道名查询节目列表
     */
    List<EpgChannel> findByChannelName(@Param("channelName") String channelName);

    /**
     * 插入单个节目
     */
    int insert(EpgChannel channel);

    /**
     * 批量插入节目
     */
    int insertBatch(@Param("channels") List<EpgChannel> channels);

    /**
     * 删除指定源的所有节目（逻辑删除）
     */
    int deleteBySourceId(@Param("sourceId") Long sourceId);

}
