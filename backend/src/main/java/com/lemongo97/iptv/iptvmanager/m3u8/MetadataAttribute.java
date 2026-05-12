package com.lemongo97.iptv.iptvmanager.m3u8;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface MetadataAttribute {
    String value();
}
