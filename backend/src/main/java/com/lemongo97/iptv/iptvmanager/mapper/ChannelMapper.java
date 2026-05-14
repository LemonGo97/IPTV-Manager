package com.lemongo97.iptv.iptvmanager.mapper;

import com.lemongo97.iptv.iptvmanager.controller.request.ChannelQuery;
import com.lemongo97.iptv.iptvmanager.entity.Channel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * 频道 Mapper
 */
@Mapper
public interface ChannelMapper {

    /**
     * 查询所有频道
     */
    List<Channel> findAll();

    /**
     * 根据 ID 查询频道
     */
    Optional<Channel> findById(@Param("id") Long id);

    /**
     * 根据分组查询频道
     */
    List<Channel> findByGroup(@Param("group") String group);

    List<Channel> findByCondition(@Param("query") ChannelQuery query);

    /**
     * 插入频道
     */
    void insert(@Param("channels") List<Channel> channels);

    void truncate();

}
