package com.lemongo97.iptv.iptvmanager.mapper;

import com.lemongo97.iptv.iptvmanager.entity.EpgSource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * EPG 源 Mapper
 */
@Mapper
public interface EpgSourceMapper {

    List<EpgSource> findAll();

    Optional<EpgSource> findById(@Param("id") Long id);

    List<EpgSource> findEnabled();

    /**
     * 根据条件搜索 EPG 源
     */
    List<EpgSource> findByCondition(@Param("name") String name);

    int insert(EpgSource epgSource);

    int update(EpgSource epgSource);

    int deleteById(@Param("id") Long id);
}
