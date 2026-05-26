package com.lemongo97.iptv.iptvmanager.mapper;

import com.lemongo97.iptv.iptvmanager.entity.IPTVProviderRawData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * IPTV 原始数据 Mapper
 */
@Mapper
public interface IPTVProviderRawDataMapper {

    /**
     * 保存原始数据
     */
    void insert(IPTVProviderRawData rawData);

    /**
     * 获取指定提供者的原始数据历史（按时间倒序）
     */
    List<IPTVProviderRawData> findByProviderIdOrderByFetchedAtDesc(@Param("providerId") Long providerId);

    /**
     * 获取指定提供者在指定时间之后的数据数量
     */
    long countByProviderIdAfterFetchedAt(@Param("providerId") Long providerId, @Param("fetchedAt") LocalDateTime fetchedAt);

    /**
     * 删除指定时间之前的数据
     */
    int deleteByFetchedAtBefore(@Param("fetchedAt") LocalDateTime fetchedAt);

    /**
     * 删除指定提供者的所有数据
     */
    int deleteByProviderId(@Param("providerId") Long providerId);
}
