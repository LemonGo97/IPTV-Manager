package com.lemongo97.iptv.iptvmanager.mapper;

import com.lemongo97.iptv.iptvmanager.entity.DistributionUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * 订阅用户 Mapper
 */
@Mapper
public interface DistributionUserMapper {

    List<DistributionUser> findAll();

    List<DistributionUser> findByCondition(@Param("username") String username);

    Optional<DistributionUser> findById(@Param("id") Long id);

    Optional<DistributionUser> findByUserId(@Param("userId") String userId);

    int insert(DistributionUser user);

    int update(DistributionUser user);

    int deleteById(@Param("id") Long id);

    int count();
}
