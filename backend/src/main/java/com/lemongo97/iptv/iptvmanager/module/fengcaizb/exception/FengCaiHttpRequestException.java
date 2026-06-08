package com.lemongo97.iptv.iptvmanager.module.fengcaizb.exception;

public class FengCaiHttpRequestException extends RuntimeException {
    public FengCaiHttpRequestException(String message) {
        super(message);
    }
    public FengCaiHttpRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public FengCaiHttpRequestException(Throwable cause) {
        super(cause);
    }
}
