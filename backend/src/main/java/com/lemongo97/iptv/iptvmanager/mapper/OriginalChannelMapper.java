package com.lemongo97.iptv.iptvmanager.mapper;

import com.lemongo97.iptv.iptvmanager.entity.OriginalChannelMetadata;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * 频道 Mapper
 */
@Mapper
public interface OriginalChannelMapper {

    /**
     * 查询所有频道
     */
    List<OriginalChannelMetadata> findAll();

    /**
     * 根据 ID 查询频道
     */
    Optional<OriginalChannelMetadata> findById(@Param("id") Long id);

    /**
     * 插入频道
     */
    void insert(@Param("channels") List<OriginalChannelMetadata> channels, @Param("taskId") Long taskId);

    /**
     * 删除频道
     */
    int deleteByProviderId(@Param("providerId") Long providerId);

    List<OriginalChannelMetadata> findByTaskId(@Param("taskId") Long taskId);
}
