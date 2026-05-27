package com.lemongo97.iptv.iptvmanager.endpoint.controller.request;

import com.lemongo97.iptv.iptvmanager.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class DistributionSubscriptionQuery extends PageQuery {
    private String name;
    private Long userId;
}
