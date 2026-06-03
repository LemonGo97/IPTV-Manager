package com.lemongo97.iptv.iptvmanager.utils;

import com.lemongo97.iptv.iptvmanager.okhttp.exception.OkHttpRequestException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OkHttpUtil {

    private static final MediaType JSON =
            MediaType.get("application/json; charset=utf-8");

    private static final OkHttpClient CLIENT =
            new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .build();

    public interface HttpCallback {

        void onSuccess(Response response);

        void onFailure(Throwable throwable);
    }

    public static class UploadFile {

        private final String fieldName;
        private final File file;
        private final String contentType;

        public UploadFile(
                String fieldName,
                File file,
                String contentType) {

            this.fieldName = fieldName;
            this.file = file;
            this.contentType = contentType;
        }

        public String getFieldName() {
            return fieldName;
        }

        public File getFile() {
            return file;
        }

        public String getContentType() {
            return contentType;
        }
    }

    // =========================================================
    // GET
    // =========================================================

    public static Response get(
            String url,
            Map<String, ?> queryParams,
            Map<String, String> headers)
            throws IOException {

        return execute(
                "GET",
                buildUrl(url, queryParams),
                null,
                headers);
    }

    public static void getAsync(
            String url,
            Map<String, ?> queryParams,
            Map<String, String> headers,
            HttpCallback callback) {

        executeAsync(
                "GET",
                buildUrl(url, queryParams),
                null,
                headers,
                callback);
    }

    // =========================================================
    // DELETE
    // =========================================================

    public static Response delete(
            String url,
            Map<String, String> headers)
            throws IOException {

        return execute(
                "DELETE",
                url,
                null,
                headers);
    }

    public static void deleteAsync(
            String url,
            Map<String, String> headers,
            HttpCallback callback) {

        executeAsync(
                "DELETE",
                url,
                null,
                headers,
                callback);
    }

    // =========================================================
    // HEAD
    // =========================================================

    public static Response head(
            String url,
            Map<String, String> headers)
            throws IOException {

        return execute(
                "HEAD",
                url,
                null,
                headers);
    }

    public static void headAsync(
            String url,
            Map<String, String> headers,
            HttpCallback callback) {

        executeAsync(
                "HEAD",
                url,
                null,
                headers,
                callback);
    }

    // =========================================================
    // POST JSON
    // =========================================================

    public static Response postJson(
            String url,
            String json,
            Map<String, String> headers)
            throws IOException {

        RequestBody body =
                RequestBody.create(json, JSON);

        return execute(
                "POST",
                url,
                body,
                headers);
    }

    public static void postJsonAsync(
            String url,
            String json,
            Map<String, String> headers,
            HttpCallback callback) {

        RequestBody body =
                RequestBody.create(json, JSON);

        executeAsync(
                "POST",
                url,
                body,
                headers,
                callback);
    }

    // =========================================================
    // POST FORM
    // =========================================================

    public static Response postForm(
            String url,
            Map<String, ?> formParams,
            Map<String, String> headers)
            throws IOException {

        return execute(
                "POST",
                url,
                buildFormBody(formParams),
                headers);
    }

    public static void postFormAsync(
            String url,
            Map<String, ?> formParams,
            Map<String, String> headers,
            HttpCallback callback) {

        executeAsync(
                "POST",
                url,
                buildFormBody(formParams),
                headers,
                callback);
    }

    // =========================================================
    // POST MULTIPART
    // =========================================================

    public static Response postMultipart(
            String url,
            Map<String, ?> formParams,
            List<UploadFile> files,
            Map<String, String> headers)
            throws IOException {

        return execute(
                "POST",
                url,
                buildMultipartBody(formParams, files),
                headers);
    }

    public static void postMultipartAsync(
            String url,
            Map<String, ?> formParams,
            List<UploadFile> files,
            Map<String, String> headers,
            HttpCallback callback) {

        executeAsync(
                "POST",
                url,
                buildMultipartBody(formParams, files),
                headers,
                callback);
    }

    // =========================================================
    // PUT JSON
    // =========================================================

    public static Response putJson(
            String url,
            String json,
            Map<String, String> headers)
            throws IOException {

        RequestBody body =
                RequestBody.create(json, JSON);

        return execute(
                "PUT",
                url,
                body,
                headers);
    }

    public static void putJsonAsync(
            String url,
            String json,
            Map<String, String> headers,
            HttpCallback callback) {

        RequestBody body =
                RequestBody.create(json, JSON);

        executeAsync(
                "PUT",
                url,
                body,
                headers,
                callback);
    }

    // =========================================================
    // PUT FORM
    // =========================================================

    public static Response putForm(
            String url,
            Map<String, ?> formParams,
            Map<String, String> headers)
            throws IOException {

        return execute(
                "PUT",
                url,
                buildFormBody(formParams),
                headers);
    }

    public static void putFormAsync(
            String url,
            Map<String, ?> formParams,
            Map<String, String> headers,
            HttpCallback callback) {

        executeAsync(
                "PUT",
                url,
                buildFormBody(formParams),
                headers,
                callback);
    }

    // =========================================================
    // PUT MULTIPART
    // =========================================================

    public static Response putMultipart(
            String url,
            Map<String, ?> formParams,
            List<UploadFile> files,
            Map<String, String> headers)
            throws IOException {

        return execute(
                "PUT",
                url,
                buildMultipartBody(formParams, files),
                headers);
    }

    public static void putMultipartAsync(
            String url,
            Map<String, ?> formParams,
            List<UploadFile> files,
            Map<String, String> headers,
            HttpCallback callback) {

        executeAsync(
                "PUT",
                url,
                buildMultipartBody(formParams, files),
                headers,
                callback);
    }

    // =========================================================
    // 核心同步请求
    // =========================================================

    private static Response execute(
            String method,
            String url,
            RequestBody body,
            Map<String, String> headers)
            throws IOException {

        Request.Builder builder =
                new Request.Builder()
                        .url(url);

        addHeaders(builder, headers);

        builder.method(method, body);

        return CLIENT.newCall(builder.build())
                .execute();
    }

    // =========================================================
    // 核心异步请求
    // =========================================================

    private static void executeAsync(
            String method,
            String url,
            RequestBody body,
            Map<String, String> headers,
            HttpCallback callback) {

        Request.Builder builder =
                new Request.Builder()
                        .url(url);

        addHeaders(builder, headers);

        builder.method(method, body);

        CLIENT.newCall(builder.build())
                .enqueue(new Callback() {

                    @Override
                    public void onFailure(
                            Call call,
                            IOException e) {

                        if (callback != null) {
                            callback.onFailure(e);
                        }
                    }

                    @Override
                    public void onResponse(
                            Call call,
                            Response response) {

                        if (callback != null) {
                            callback.onSuccess(response);
                        }
                    }
                });
    }

    // =========================================================
    // Header
    // =========================================================

    private static void addHeaders(
            Request.Builder builder,
            Map<String, String> headers) {

        if (headers == null || headers.isEmpty()) {
            return;
        }

        headers.forEach(builder::addHeader);
    }

    // =========================================================
    // QueryString
    // =========================================================

    private static String buildUrl(
            String url,
            Map<String, ?> params) {

        if (params == null || params.isEmpty()) {
            return url;
        }

        HttpUrl.Builder builder =
                HttpUrl.parse(url).newBuilder();

        params.forEach((k, v) ->
                builder.addQueryParameter(
                        k,
                        v == null ? "" : String.valueOf(v)));

        return builder.build().toString();
    }

    // =========================================================
    // FormBody
    // =========================================================

    private static FormBody buildFormBody(
            Map<String, ?> params) {

        FormBody.Builder builder =
                new FormBody.Builder();

        if (params != null) {

            params.forEach((k, v) ->
                    builder.add(
                            k,
                            v == null ? "" : String.valueOf(v)));
        }

        return builder.build();
    }

    // =========================================================
    // MultipartBody
    // =========================================================

    private static MultipartBody buildMultipartBody(
            Map<String, ?> formParams,
            List<UploadFile> files) {

        MultipartBody.Builder builder =
                new MultipartBody.Builder()
                        .setType(MultipartBody.FORM);

        if (formParams != null) {

            formParams.forEach((k, v) ->
                    builder.addFormDataPart(
                            k,
                            v == null ? "" : String.valueOf(v)));
        }

        if (files != null) {

            for (UploadFile uploadFile : files) {

                builder.addFormDataPart(
                        uploadFile.getFieldName(),
                        uploadFile.getFile().getName(),
                        RequestBody.create(
                                uploadFile.getFile(),
                                MediaType.get(
                                        uploadFile.getContentType())));
            }
        }

        return builder.build();
    }

    public static OkHttpClient getClient() {
        return CLIENT;
    }
}