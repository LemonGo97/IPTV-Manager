package com.lemongo97.iptv.iptvmanager.module.fengcaizb;

import lombok.Data;

import java.util.List;

@Data
public class FengCaiChannel {

    private String title;
    private String group;
    private List<String> urls;

}
