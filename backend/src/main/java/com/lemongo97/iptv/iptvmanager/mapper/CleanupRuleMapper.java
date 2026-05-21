package com.lemongo97.iptv.iptvmanager.mapper;

import com.lemongo97.iptv.iptvmanager.entity.CleanupRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * 数据清洗规则 Mapper
 */
@Mapper
public interface CleanupRuleMapper {

    List<CleanupRule> findAll();

    List<CleanupRule> findByRuleType(@Param("ruleType") String ruleType);

    Optional<CleanupRule> findById(@Param("id") Long id);

    int insert(CleanupRule rule);

    int update(CleanupRule rule);

    int deleteById(@Param("id") Long id);

    void batchUpdateSortOrder(@Param("rules") List<CleanupRule> rules);
}
