package com.lemongo97.iptv.iptvmanager.controller.request;

import com.lemongo97.iptv.iptvmanager.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class ChannelQuery extends PageQuery {
    private String name;
    private List<Long> providerId;
    private List<Long> groupId;
}
