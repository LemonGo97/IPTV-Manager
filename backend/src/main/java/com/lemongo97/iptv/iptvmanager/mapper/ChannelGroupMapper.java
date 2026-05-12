package com.lemongo97.iptv.iptvmanager.mapper;

import com.lemongo97.iptv.iptvmanager.entity.ChannelGroup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * 频道组 Mapper
 */
@Mapper
public interface ChannelGroupMapper {

    List<ChannelGroup> findAll();

    List<ChannelGroup> findByCondition(@Param("name") String name);

    Optional<ChannelGroup> findById(@Param("id") Long id);

    int insert(ChannelGroup group);

    int update(ChannelGroup group);

    int deleteById(@Param("id") Long id);
}
