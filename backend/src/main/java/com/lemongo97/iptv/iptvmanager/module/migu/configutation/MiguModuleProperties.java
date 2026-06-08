package com.lemongo97.iptv.iptvmanager.module.migu.configutation;

import com.lemongo97.iptv.iptvmanager.module.migu.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Data
@ConfigurationProperties(prefix = "module.migu")
public class MiguModuleProperties {

    private OkHttpClientConfiguration okhttp = new OkHttpClientConfiguration();
    private boolean enableHDR = true;
    private boolean enableH265 = true;

    private List<String> epgProvider = new ArrayList<>(List.of(
        "https://tv.mxdyeah.top/epgphp/t.xml.gz",
        "http://epg.51zmt.top:8000/e1.xml.gz",
        "http://epg.51zmt.top:8000/e2.xml.gz",
        "https://epg.v1.mk/fy.xml",
        "https://epg.freejptv.com/jp.xml",
        "https://animenosekai.github.io/japanterebi-xmltv/guide.xml",
        "https://epg.pw/xmltv/epg_HK.xml.gz",
        "https://epg.pw/xmltv/epg_TW.xml.gz"
    ));

    @Data
    public static class OkHttpClientConfiguration {

        private Timeout connectTimeout = new Timeout(10L, TimeUnit.SECONDS);
        private Timeout readTimeout = new Timeout(10L, TimeUnit.SECONDS);
        private Timeout writeTimeout = new Timeout(10L, TimeUnit.SECONDS);
        private Timeout callTimeout = new Timeout(0, TimeUnit.SECONDS);
        private boolean retryOnConnectionFailure = true;

        @Data
        @AllArgsConstructor
        public static class Timeout {
            private final long timeout;
            private final TimeUnit unit;
        }
    }

}
