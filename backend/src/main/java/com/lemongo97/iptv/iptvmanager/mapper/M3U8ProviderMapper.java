package com.lemongo97.iptv.iptvmanager.mapper;

import com.lemongo97.iptv.iptvmanager.entity.M3U8Provider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * M3U8 源 Mapper
 */
@Mapper
public interface M3U8ProviderMapper {

    /**
     * 查询所有 M3U8 源
     */
    List<M3U8Provider> findAll();

    /**
     * 根据 ID 查询 M3U8 源
     */
    Optional<M3U8Provider> findById(@Param("id") Long id);

    /**
     * 根据类型查询 M3U8 源
     */
    List<M3U8Provider> findByType(@Param("type") String type);

    /**
     * 查询启用的 M3U8 源
     */
    List<M3U8Provider> findEnabled();

    /**
     * 插入 M3U8 源
     */
    int insert(M3U8Provider provider);

    /**
     * 更新 M3U8 源
     */
    int update(M3U8Provider provider);

    /**
     * 删除 M3U8 源
     */
    int deleteById(@Param("id") Long id);
}
