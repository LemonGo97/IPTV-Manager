package com.lemongo97.iptv.iptvmanager.module.migu.service;

import cn.hutool.crypto.digest.DigestUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
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
import com.lemongo97.iptv.iptvmanager.module.migu.entity.LiveChannel;
import com.lemongo97.iptv.iptvmanager.module.migu.exception.MiguHttpRequestException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * <pre>
 *
 ┌─────────────────────────────────────────────────────────────┐
 │  1. 服务启动 → 定时更新频道列表                              │
 │     update() → updateTV() → 写入 interface.txt               │
 │     文件内容: ${replace}/10001, ${replace}/10002, ...       │
 └─────────────────────────────────────────────────────────────┘
 ↓
 ┌─────────────────────────────────────────────────────────────┐
 │  2. 播放器请求播放列表                                        │
 │     GET http://192.168.1.100:8080/interface.txt              │
 │     → 读取文件 → 替换 ${replace}                            │
 │     → 返回: http://192.168.1.100:8080/10001, ...            │
 └─────────────────────────────────────────────────────────────┘
 ↓
 ┌─────────────────────────────────────────────────────────────┐
 │  3. 播放器请求具体频道                                        │
 │     GET http://192.168.1.100:8080/10001                     │
 │     → 解析 URL，提取 pid = 10001                            │
 └─────────────────────────────────────────────────────────────┘
 ↓
 ┌─────────────────────────────────────────────────────────────┐
 │  4. 请求咪咕 API 获取真实播放地址                              │
 │     channel(pid) → getAndroidURL()                           │
 │     → 获取到: https://实际流地址.m3u8?token=xxx              │
 └─────────────────────────────────────────────────────────────┘
 ↓
 ┌─────────────────────────────────────────────────────────────┐
 │  5. 返回 302 重定向                                          │
 │     Location: https://实际流地址.m3u8?token=xxx               │
 │     → 播放器自动跳转到真实地址开始播放                         │
 └─────────────────────────────────────────────────────────────┘
 * </pre>
 */
@Slf4j
@AllArgsConstructor
public class MiguApiService {

    private static final String clientId = getClientId();

    private static final Cache<String, String> cache = Caffeine.newBuilder()
            .initialCapacity(100)
            .maximumSize(500)
            .expireAfterWrite(3, TimeUnit.HOURS)
            .build();

    private static final Cache<String, String> error_cache = Caffeine.newBuilder()
            .initialCapacity(20)
            .maximumSize(500)
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build();

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
            public Set<Option> options() {
                return EnumSet.noneOf(Option.class);
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

    public List<LiveChannel> fetchChannelList() {
        return this.fetchChannelList(this.fetchCateList());
    }

    public List<LiveChannel> fetchChannelList(List<LiveCategory> categories) {
        List<LiveChannel> channels = new ArrayList<>();
        Set<String> pids = new HashSet<>();
        for (LiveCategory category : categories) {
            try (Response response = okHttpClient.newCall(new Request.Builder().get().url(String.format(Constants.CHANNELS_URL, category.getVomsID())).build()).execute()) {
                if (!response.isSuccessful()) throw new MiguHttpRequestException("Unexpected code: " + response.code());
                String jsonString = response.body().string();

                DocumentContext jsonpath = JsonPath.parse(jsonString);
                int code = jsonpath.read("$.code", int.class);
                if (code != 200) throw new MiguHttpRequestException("Unexpected code with response json content: " + code);

                ArrayNode liveList = jsonpath.read("$.body.dataList", ArrayNode.class);
                if (liveList == null || liveList.isEmpty()) {
                    continue;
                }

                for (JsonNode node : liveList.values()) {
                    String name = node.get("name").asString();
                    String pID = node.get("pID").asString();
                    if (pids.contains(pID)) continue;

                    LiveChannel liveChannel = new LiveChannel();
                    liveChannel.setName(name);
                    liveChannel.setPID(pID);
                    liveChannel.setCategoryName(category.getName());
                    liveChannel.setVomsID(category.getVomsID());
                    channels.add(liveChannel);
                    pids.add(pID);
                }

            } catch (IOException e) {
                throw new MiguHttpRequestException(e);
            }
        }
        return channels;
    }

    public String fetchRealChannelUrl(String pid) {
        String cachedErrUrl = error_cache.getIfPresent(pid);
        if (Strings.CI.equals("noop", cachedErrUrl)) {
            throw new MiguHttpRequestException("Missing url info, try 1 minute later...");
        }

        String cachedUrl = cache.getIfPresent(pid);
        if (StringUtils.isNotBlank(cachedUrl)) {
            log.info("Cached channel url for pid: {}, use cached url: {}", pid, cachedUrl);
            return cachedUrl;
        }

        Headers headers = buildHeaders(pid);

        long now = System.currentTimeMillis();
        String md5 = DigestUtils.md5Hex(now + pid + Constants.APP_VERSION.substring(0, 8));

        String salt = String.format("%06d", ((int) (Math.random() * 1000000))) + "25";
        String suffix = "2cac4f2c6c3346a5b34e085725ef7e33migu" + salt.substring(0, 4);
        String sign = DigestUtil.md5Hex(md5 + suffix);

        @SuppressWarnings("DataFlowIssue")
        HttpUrl.Builder builder = HttpUrl.parse(Constants.CHANNEL_PLAY_URL).newBuilder();

        builder.addQueryParameter("sign", sign)
                .addQueryParameter("rateType", String.valueOf(3))
                .addQueryParameter("contId", pid)
                .addQueryParameter("timestamp", String.valueOf(now))
                .addQueryParameter("salt", salt)
                .addQueryParameter("flvEnable", String.valueOf(true))
                .addQueryParameter("super4k", String.valueOf(true));

        if (properties.isEnableH265()){
            builder
                    .addQueryParameter("h265N", String.valueOf(true));
        }

        if (properties.isEnableHDR()){
            builder
                    .addQueryParameter("4kvivid", String.valueOf(true))
                    .addQueryParameter("2Kvivid", String.valueOf(true))
                    .addQueryParameter("vivid", String.valueOf(2));
        }

        Request request = new Request.Builder().get()
                .headers(headers)
                .url(builder.build())
                .build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new MiguHttpRequestException("Unexpected code: " + response.code());
            String jsonString = response.body().string();
            DocumentContext jsonpath = JsonPath.parse(jsonString);
            int code = jsonpath.read("$.code", int.class);
            if (code != 200) throw new MiguHttpRequestException("Unexpected code with response json content: " + code);

            String url = jsonpath.read("$.body.urlInfo.url", String.class);
            String rateType = jsonpath.read("$.body.urlInfo.rateType", String.class);
            String contId = jsonpath.read("$.body.content.contId", String.class);

            log.debug("response received, rateType: {}, contId: {}, url: {}", rateType, contId, url);

            if (StringUtils.isBlank(url)) {
                error_cache.put(pid, "noop");
                throw new MiguHttpRequestException("Missing url info, try 1 minute later...");
            }

            String puData = url.split("&puData=")[1];
            String ddCalcu = getddCalcu720p(puData, pid);

            String result = url + "&ddCalcu=" + ddCalcu+"&sv=10004&ct=android";
            // 添加到缓存
            cache.put(pid, result);
            return result;
        } catch (IOException e) {
            throw new MiguHttpRequestException(e);
        }
    }

    static String getddCalcu720p(String puData, String programId){
        if(StringUtils.isAnyBlank(puData, programId)){
            return StringUtils.EMPTY;
        }
        String keys = "cdabyzwxkl";

        StringBuilder ddCalcu = new StringBuilder();
        int length = puData.length();

        for (int i = 0; i < length / 2; i++) {
            // 添加倒序字符
            ddCalcu.append(puData.charAt(length - i - 1));

            // 添加正序字符
            ddCalcu.append(puData.charAt(i));

            // 在特定位置插入字符
            switch (i) {
                case 1:
                    ddCalcu.append('v');
                    break;
                case 2:
                    // 获取日期的第2个字符
                    String dateStr = getDateString();
                    int dateIndex = Integer.parseInt(dateStr.substring(2, 3));
                    ddCalcu.append(keys.charAt(dateIndex));
                    break;
                case 3:
                    // 获取 programId 的第6位字符对应的 keys
                    char pidChar = programId.charAt(6);
                    int keyIndex = Character.getNumericValue(pidChar);
                    ddCalcu.append(keys.charAt(keyIndex));
                    break;
                case 4:
                    ddCalcu.append('a');
                    break;
            }
        }

        return ddCalcu.toString();
    }

    static Headers buildHeaders(String pid) {
        Headers.Builder builder = new Headers.Builder();
        builder.add("AppVersion", Constants.APP_VERSION);
        builder.add("TerminalId", "android");
        builder.add("X-UP-CLIENT-CHANNEL-ID", Constants.APP_VERSION_ID);
        builder.add("ClientId", clientId);

        // cctv5和5+开启flv后不能回放
        if (!Strings.CI.equalsAny(pid, "641886683", "641886773")){
            builder.add("appCode", "miguvideo_default_android");
        }

        return builder.build();
    }

    static String getClientId(){
        long now = System.currentTimeMillis();
        return DigestUtil.md5Hex(String.valueOf(now));
    }

    static String getDateString() {
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return now.format(formatter);
    }
}
