package com.lemongo97.iptv.iptvmanager.common;

import lombok.Data;

@Data
public class PageQuery implements IPage{
    protected int pageNum = 1;
    protected int pageSize = 10;
    protected String orderBy;
    protected Boolean asc;
}
