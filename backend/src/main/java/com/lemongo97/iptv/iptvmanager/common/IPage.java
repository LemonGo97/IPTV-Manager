package com.lemongo97.iptv.iptvmanager.common;

import java.util.Map;

public interface IPage {

    int getPageNum();
    int getPageSize();
    String getOrderBy();
    Boolean getAsc();

    default int getOffset(){
        return (getPageNum() - 1) * getPageSize();
    }

    static IPage DEFAULT() {
        return of(1, 10); // 修改默认页码从1开始
    }

    static IPage of(int pageNum, int pageSize) {
        return new IPage() {
            @Override
            public int getPageNum() {
                return pageNum;
            }

            @Override
            public int getPageSize() {
                return pageSize;
            }

            @Override
            public String getOrderBy() {
                return null;
            }

            @Override
            public Boolean getAsc() {
                return null;
            }

        };
    }
}