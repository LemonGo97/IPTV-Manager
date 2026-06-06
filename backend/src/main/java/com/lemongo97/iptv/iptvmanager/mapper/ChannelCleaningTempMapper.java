package com.lemongo97.iptv.iptvmanager.mapper;

import com.lemongo97.iptv.iptvmanager.entity.Channel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 频道清洗中间表 Mapper
 * 用于数据清洗过程中的临时存储
 */
@Mapper
public interface ChannelCleaningTempMapper {

    /**
     * 物理清空中间表（DELETE，不是逻辑删除）
     */
    void truncate();

    default void insert(List<Channel> channels) {
        for (Channel channel : channels) {
            this.insert(channel);
        }
    }

    void insertBatch(@Param("channels") List<Channel> channels);

    /**
     * 插入单个频道到中间表
     */
    void insert(@Param("channel") Channel channel);

    /**
     * 查询中间表所有频道（用于转移到正式表）
     */
    List<Channel> findAll();

    /**
     * 统计中间表频道数量
     */
    int count();
}
