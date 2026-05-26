package com.lemongo97.iptv.iptvmanager.mapper;

import com.lemongo97.iptv.iptvmanager.entity.IPTVProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * IPTV 源 Mapper
 */
@Mapper
public interface IPTVProviderMapper {

    /**
     * 查询所有 IPTV 源
     */
    List<IPTVProvider> findAll();

    /**
     * 根据 ID 查询 IPTV 源
     */
    Optional<IPTVProvider> findById(@Param("id") Long id);

    /**
     * 根据类型查询 IPTV 源
     */
    List<IPTVProvider> findByType(@Param("type") String type);

    /**
     * 查询启用的 IPTV 源
     */
    List<IPTVProvider> findEnabled();

    /**
     * 插入 IPTV 源
     */
    int insert(IPTVProvider provider);

    /**
     * 更新 IPTV 源
     */
    int update(IPTVProvider provider);

    /**
     * 删除 IPTV 源
     */
    int deleteById(@Param("id") Long id);

    List<IPTVProvider> getProviderNames();
}
