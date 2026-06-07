package com.lemongo97.iptv.iptvmanager.module.migu.entity;

import lombok.Data;

import java.util.List;

@Data
public class MiguChannel {

    private String title;
    private String group;
    private List<String> urls;

}
