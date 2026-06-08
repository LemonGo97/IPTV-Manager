package com.lemongo97.iptv.iptvmanager.module.migu.service;

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
import com.lemongo97.iptv.iptvmanager.module.migu.Constants;
import com.lemongo97.iptv.iptvmanager.module.migu.configutation.MiguModuleProperties;
import com.lemongo97.iptv.iptvmanager.module.migu.entity.LiveCategory;
import com.lemongo97.iptv.iptvmanager.module.migu.exception.MiguHttpRequestException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.Strings;
import tools.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Slf4j
@AllArgsConstructor
public class MiguApiService {

    private final MiguModuleProperties properties;
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
}
