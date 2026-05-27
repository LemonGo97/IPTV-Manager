package com.lemongo97.iptv.iptvmanager.mapper;

import com.lemongo97.iptv.iptvmanager.endpoint.controller.request.DistributionSubscriptionQuery;
import com.lemongo97.iptv.iptvmanager.entity.DistributionSubscription;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * 分发订阅 Mapper
 */
@Mapper
public interface DistributionSubscriptionMapper {

    List<DistributionSubscription> findByCondition(@Param("query") DistributionSubscriptionQuery query);

    Optional<DistributionSubscription> findById(@Param("id") Long id);

    List<DistributionSubscription> findByUserId(@Param("userId") Long userId);

    int insert(DistributionSubscription subscription);

    int update(DistributionSubscription subscription);

    int deleteById(@Param("id") Long id);

    int count();
}