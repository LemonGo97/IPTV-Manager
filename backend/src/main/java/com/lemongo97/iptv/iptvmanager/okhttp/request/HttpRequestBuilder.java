package com.lemongo97.iptv.iptvmanager.okhttp.request;

import com.lemongo97.iptv.iptvmanager.okhttp.HttpRequestMethod;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import okhttp3.Request;

import java.util.LinkedHashMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HttpRequestBuilder {

    private String url;
    private HttpRequestMethod method;
    private final LinkedHashMap<String, String> headers = new LinkedHashMap<>();
    private final LinkedHashMap<String, Object> params = new LinkedHashMap<>();

    public static HttpRequestBuilder create(){
        return new HttpRequestBuilder();
    }

    public HttpRequestBuilder url(String url){
        this.url = url;
        return this;
    }

    public HttpRequestBuilder method(HttpRequestMethod method){
        this.method = method;
        return this;
    }

    public HttpRequestBuilder header(String key, String value){
        this.headers.put(key, value);
        return this;
    }

    public HttpRequestBuilder headers(Map<String, String> headers){
        this.headers.putAll(headers);
        return this;
    }

    public HttpRequestBuilder param(String key, Object value){
        this.params.put(key, value);
        return this;
    }

    public HttpRequestBuilder params(Map<String, Object> params) {
        this.params.putAll(params);
        return this;
    }

    public HttpRequestBuilder body(String body){
        return this;
    }

    public HttpRequestBuilder body(Object body) {
        return this;
    }

    public HttpRequestBuilder body(Map<String, Object> body) {
        return this;
    }

    public Request build(){
        return new Request.Builder().url(url).build();
    }

}
