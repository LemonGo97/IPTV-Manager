package com.lemongo97.iptv.iptvmanager.mapper;

import com.lemongo97.iptv.iptvmanager.entity.EpgProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * EPG 源 Mapper
 */
@Mapper
public interface EpgProviderMapper {

    List<EpgProvider> findAll();

    Optional<EpgProvider> findById(@Param("id") Long id);

    List<EpgProvider> findEnabled();

    /**
     * 根据条件搜索 EPG 源
     */
    List<EpgProvider> findByCondition(@Param("name") String name);

    int insert(EpgProvider epgProvider);

    int update(EpgProvider epgProvider);

    int deleteById(@Param("id") Long id);
}
