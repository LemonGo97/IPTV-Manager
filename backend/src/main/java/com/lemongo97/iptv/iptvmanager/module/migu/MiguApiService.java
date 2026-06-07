package com.lemongo97.iptv.iptvmanager.module.migu;

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
import com.lemongo97.iptv.iptvmanager.module.migu.entity.LiveCategory;
import com.lemongo97.iptv.iptvmanager.module.migu.entity.MiguChannel;
import com.lemongo97.iptv.iptvmanager.module.migu.exception.MiguHttpRequestException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.lang3.Strings;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

@Slf4j
@AllArgsConstructor
public class MiguApiService {

    private final OkHttpClient okHttpClient;
//    private final ObjectMapper objectMapper;

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

    public List<LiveCategory> fetchCateList() {
        try (Response response = okHttpClient.newCall(new Request.Builder().get().url(Constants.CATEGORY_URL).build()).execute()) {
            if (!response.isSuccessful()) throw new MiguHttpRequestException("Unexpected code: " + response.code());
            String jsonString = response.body().string();

            DocumentContext jsonpath = JsonPath.parse(jsonString);
            int code = jsonpath.read("$.code", int.class);
            if (code != 200) throw new MiguHttpRequestException("Unexpected code with response json content: " + code);

            ArrayNode liveList = jsonpath.read("$.body.liveList", ArrayNode.class);
            if (liveList == null || liveList.isEmpty()) {
                return List.of();
            }

            List<LiveCategory> liveCategories = liveList.valueStream().map(live -> {
                String name = live.get("name").asString();
                if (Strings.CI.equals("热门", name)) return null;

                String vomsID = live.get("vomsID").asString();
                LiveCategory liveCategory = new LiveCategory();
                liveCategory.setName(name);
                liveCategory.setVomsID(vomsID);
                return liveCategory;
            }).filter(Objects::nonNull).toList();
            return liveCategories;
        } catch (IOException e) {
            throw new MiguHttpRequestException(e);
        }
    }

    public List<MiguChannel> fetchChannelList() {
        try (Response response = okHttpClient
                .newCall(new Request.Builder().get()
                        .url(Constants.LIVE_CHANNELS_URL).header("Referer", Constants.LIVE_CHANNELS_REFER).build()).execute()) {
            if (!response.isSuccessful()) throw new MiguHttpRequestException("Unexpected code: " + response.code());
            InputStream responseInputStream = response.body().byteStream();
            InputStream inputStream = new GzipCompressorInputStream(responseInputStream);

            DocumentContext jsonpath = JsonPath.parse(inputStream);
            int status = jsonpath.read("$.status", int.class);
            if (status != 0) throw new MiguHttpRequestException("Unexpected status with response json content: " + status);

            ArrayNode channelNodes = jsonpath.read("$.data", ArrayNode.class);

            List<MiguChannel> channels = channelNodes.valueStream().map(node -> {

                // 过滤广告频道
                if (node.has("ct")) {
                    return null;
                }

                MiguChannel miguChannel = new MiguChannel();

                String title = node.get("title").asString();
                miguChannel.setTitle(title);

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
                miguChannel.setUrls(urls);

                if (node.has("province")){
                    String province = node.get("province").asString();
                    miguChannel.setGroup(province);
                }

                return miguChannel;
            }).filter(Objects::nonNull).toList();

            return channels;
        } catch (IOException e) {
            throw new MiguHttpRequestException(e);
        }
    }

    String decryptChannelUrl(String channelUrl) {
        AES aes = new AES(
                Mode.CBC,
                Padding.PKCS5Padding,
                HexUtil.decodeHex(Constants.LIVE_CHANNELS_DECRYPT_KEY_HEX),
                HexUtil.decodeHex(Constants.LIVE_CHANNELS_DECRYPT_IV_HEX));
        return aes.decryptStr(Base64.decode(channelUrl));
    }

}
