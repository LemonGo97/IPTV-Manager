package com.lemongo97.iptv.iptvmanager.mapper;

import com.lemongo97.iptv.iptvmanager.entity.CleanupEngine;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 频道组 Mapper
 */
@Mapper
public interface CleanupEngineMapper {

    List<CleanupEngine> findAll();

}
