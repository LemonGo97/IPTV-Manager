package com.lemongo97.iptv.iptvmanager.common;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {
    private long total;
    private List<T> data;

    public PageResult(long total, List<T> data) {
        this.total = total;
        this.data = data;
    }

    public static <T> PageResult<T> of(long total, List<T> data) {
        return new PageResult<>(total, data);
    }
}