package com.lemongo97.iptv.iptvmanager.module.fengcaizb;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.symmetric.AES;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.Jackson3JsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.Jackson3MappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import com.lemongo97.iptv.iptvmanager.module.fengcaizb.configutation.FengCaiModuleProperties;
import com.lemongo97.iptv.iptvmanager.module.fengcaizb.exception.FengCaiHttpRequestException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import tools.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

@Slf4j
@AllArgsConstructor
public class FengCaiApiService {

    private final FengCaiModuleProperties properties;
    private final OkHttpClient okHttpClient;

    static {
        Configuration.setDefaults(new Configuration.Defaults() {
            private final Jackson3JsonProvider jsonProvider = new Jackson3JsonProvider();
            private final Jackson3MappingProvider mappingProvider = new Jackson3MappingProvider();

            @Override
            public JsonProvider jsonProvider() { return jsonProvider; }

            @Override
            public MappingProvider mappingProvider() { return mappingProvider; }

            @Override
            public java.util.Set<Option> options() {
                return java.util.EnumSet.noneOf(Option.class);
            }
        });
    }

    public List<FengCaiChannel> fetchChannelList() {
        try (Response response = okHttpClient
                .newCall(new Request.Builder().get()
                        .url(Constants.LIVE_CHANNELS_URL).header("Referer", Constants.LIVE_CHANNELS_REFER).build()).execute()) {
            if (!response.isSuccessful()) throw new FengCaiHttpRequestException("Unexpected code: " + response.code());
            InputStream responseInputStream = response.body().byteStream();
            InputStream inputStream = new GzipCompressorInputStream(responseInputStream);

            DocumentContext jsonpath = JsonPath.parse(inputStream);
            int status = jsonpath.read("$.status", int.class);
            if (status != 0) throw new FengCaiHttpRequestException("Unexpected status with response json content: " + status);

            ArrayNode channelNodes = jsonpath.read("$.data", ArrayNode.class);

            List<FengCaiChannel> channels = channelNodes.valueStream().map(node -> {

                // 过滤广告频道
                if (node.has("ct")) {
                    return null;
                }

                FengCaiChannel fengCaiChannel = new FengCaiChannel();

                String title = node.get("title").asString();
                fengCaiChannel.setTitle(title);

                ArrayNode urlNodes = node.get("urls").asArray();
                List<String> urls = urlNodes.valueStream().map(urlNode -> {
                    String url = this.decryptChannelUrl(urlNode.asString());
                    if (url.startsWith("sys_http")) {
                        url = url.replace("sys_", "");
                    }
                    if (!url.startsWith("http")) {
                        return null;
                    }
                    if (url.contains("$")) {
                        url = url.split("\\$")[0];
                    }
                    return url;
                }).filter(Objects::nonNull).toList();
                fengCaiChannel.setUrls(urls);

                if (node.has("province")){
                    String province = node.get("province").asString();
                    fengCaiChannel.setGroup(province);
                }

                return fengCaiChannel;
            }).filter(Objects::nonNull).toList();

            return channels;
        } catch (IOException e) {
            throw new FengCaiHttpRequestException(e);
        }
    }

    String decryptChannelUrl(String channelUrl) {
        AES aes = new AES(
                Mode.CBC,
                Padding.PKCS5Padding,
                HexUtil.decodeHex(properties.getDecryptKeyHex()),
                HexUtil.decodeHex(properties.getDecryptIvHex()));
        return aes.decryptStr(Base64.decode(channelUrl));
    }

}
