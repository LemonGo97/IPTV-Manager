package com.lemongo97.iptv.iptvmanager.module.migu.entity;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class LiveChannel {
    private String name;
    private String pID;
    private String categoryName;
    private String vomsID;
}
