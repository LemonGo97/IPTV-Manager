package com.lemongo97.iptv.iptvmanager.module.migu.exception;

public class MiguHttpRequestException extends RuntimeException {
    public MiguHttpRequestException(String message) {
        super(message);
    }
    public MiguHttpRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public MiguHttpRequestException(Throwable cause) {
        super(cause);
    }
}
