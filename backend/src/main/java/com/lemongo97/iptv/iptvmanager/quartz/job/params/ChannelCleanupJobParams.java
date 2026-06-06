package com.lemongo97.iptv.iptvmanager.quartz.job.params;

import com.lemongo97.iptv.iptvmanager.cleanup.rule.RuleType;
import com.lemongo97.iptv.iptvmanager.entity.Channel;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

/**
 * 频道数据清洗参数
 */
@Data
public class ChannelCleanupJobParams {

    public static final ChannelCleanupJobParams ALL = new ChannelCleanupJobParams();

    /**
     * 规则类型，若为空，则执行全部步骤，否则执行相应步骤
     */
    private Set<RuleType> ruleType = new HashSet<>();
    /**
     * 规则ID，若为空，则说明为全部匹配的ID
     */
    private Set<Long> ruleId = new HashSet<>();
    /**
     * 频道提供者ID，若为空，则为全部 Provider
     */
    private Set<Long> channelProviderId = new HashSet<>();
    /**
     * 频道ID，若为空，则为全部Channel
     */
    private Set<Long> channelId = new HashSet<>();
    /**
     * 频道状态，若为空，则为全部Channel
     */
    private Set<Channel.Status> status = new HashSet<>();

    public ChannelCleanupJobParams addRuleType(RuleType ruleType) {
        this.ruleType.add(ruleType);
        return this;
    }

    public ChannelCleanupJobParams addRuleId(Long ruleId) {
        this.ruleId.add(ruleId);
        return this;
    }

    public ChannelCleanupJobParams addChannelProviderId(Long channelProviderId) {
        this.channelProviderId.add(channelProviderId);
        return this;
    }

    public ChannelCleanupJobParams addChannelId(Long channelId) {
        this.channelId.add(channelId);
        return this;
    }
}
