package com.lemongo97.iptv.iptvmanager.common;

import java.time.LocalDateTime;

/**
 * 统一 API 响应格式
 */
public record ApiResponse<T>(
        int code,
        boolean success,
        T data,
        String message,
        LocalDateTime timestamp
) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(0, true, data, null, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>(0, true, data, message, LocalDateTime.now());
    }

    public static ApiResponse<Void> ok(String message) {
        return new ApiResponse<>(0, true, null, message, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(0, false, null, message, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(0, false, data, message, LocalDateTime.now());
    }
}
