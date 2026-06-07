package com.lemongo97.iptv.iptvmanager.cleanup.engine.delay;

import com.lemongo97.iptv.iptvmanager.entity.Channel;
import com.lemongo97.iptv.iptvmanager.utils.JSONUtil;
import lombok.AccessLevel;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

@Slf4j(access = AccessLevel.PUBLIC)
public class TestHttpGetCheckEngine {

    HttpGetCheckEngine engine;

    @BeforeEach
    void init() {
        engine = new HttpGetCheckEngine();
    }

    @Test
    void test_checkM3UContentValid(){
        String content = """
                #EXTM3U
                #EXT-X-VERSION:3
                #EXT-X-MEDIA-SEQUENCE:0
                #EXT-X-ALLOW-CACHE:YES
                #EXT-X-TARGETDURATION:4
                #EXTINF:3.600000,
                http://files4.3y1.xyz/media/video/no_signal_epg/no_signal0.ts
                #EXTINF:3.600000,
                https://files4.3y1.xyz/media/video/no_signal_epg/no_signal1.ts
                #EXTINF:3.600000,
                http://files4.3y1.xyz/media/video/no_signal_epg/no_signal2.ts
                #EXTINF:3.600000,
                http://files4.3y1.xyz/media/video/no_signal_epg/no_signal3.ts
                #EXTINF:3.600000,
                http://files4.3y1.xyz/media/video/no_signal_epg/no_signal4.ts
                #EXTINF:3.600000,
                http://files4.3y1.xyz/media/video/no_signal_epg/no_signal5.ts
                #EXTINF:3.600000,
                http://files4.3y1.xyz/media/video/no_signal_epg/no_signal6.ts
                #EXTINF:3.600000,
                http://files4.3y1.xyz/media/video/no_signal_epg/no_signal7.ts
                #EXTINF:3.600000,
                http://files4.3y1.xyz/media/video/no_signal_epg/no_signal8.ts
                #EXTINF:3.600000,
                http://files4.3y1.xyz/media/video/no_signal_epg/no_signal9.ts
                #EXTINF:3.600000,
                http://files4.3y1.xyz/media/video/no_signal_epg/no_signal10.ts
                #EXTINF:3.600000,
                http://files4.3y1.xyz/media/video/no_signal_epg/no_signal11.ts
                #EXTINF:3.600000,
                http://files4.3y1.xyz/media/video/no_signal_epg/no_signal12.ts
                #EXTINF:3.600000,
                http://files4.3y1.xyz/media/video/no_signal_epg/no_signal13.ts
                #EXTINF:3.600000,
                http://files4.3y1.xyz/media/video/no_signal_epg/no_signal14.ts
                #EXTINF:3.600000,
                http://files4.3y1.xyz/media/video/no_signal_epg/no_signal15.ts
                #EXTINF:3.600000,
                http://files4.3y1.xyz/media/video/no_signal_epg/no_signal16.ts
                #EXTINF:3.600000,
                http://files4.3y1.xyz/media/video/no_signal_epg/no_signal17.ts
                #EXTINF:3.600000,
                http://files4.3y1.xyz/media/video/no_signal_epg/no_signal18.ts
                #EXTINF:3.600000,
                http://files4.3y1.xyz/media/video/no_signal_epg/no_signal19.ts
                #EXTINF:3.600000,
                http://files4.3y1.xyz/media/video/no_signal_epg/no_signal20.ts
                #EXTINF:3.600000,
                http://files4.3y1.xyz/media/video/no_signal_epg/no_signal21.ts
                #EXTINF:3.600000,
                http://files4.3y1.xyz/media/video/no_signal_epg/no_signal22.ts
                #EXTINF:3.600000,
                http://files4.3y1.xyz/media/video/no_signal_epg/no_signal23.ts
                #EXTINF:3.600000,
                http://files4.3y1.xyz/media/video/no_signal_epg/no_signal24.ts
                #EXTINF:3.600000,
                http://files4.3y1.xyz/media/video/no_signal_epg/no_signal25.ts
                #EXTINF:3.600000,
                http://files4.3y1.xyz/media/video/no_signal_epg/no_signal26.ts
                #EXTINF:3.600000,
                http://files4.3y1.xyz/media/video/no_signal_epg/no_signal27.ts
                #EXTINF:3.600000,
                http://files4.3y1.xyz/media/video/no_signal_epg/no_signal28.ts
                #EXTINF:3.600000,
                http://files4.3y1.xyz/media/video/no_signal_epg/no_signal29.ts
                #EXTINF:3.600000,
                http://files4.3y1.xyz/media/video/no_signal_epg/no_signal30.ts
                #EXTINF:3.600000,
                http://files4.3y1.xyz/media/video/no_signal_epg/no_signal31.ts
                #EXTINF:3.160000,
                http://files4.3y1.xyz/media/video/no_signal_epg/no_signal32.ts
                """;

        engine.checkM3UContentValid(content);
    }

    @Test
    void test_process(){

        Channel channel = new Channel().setName("test_channel").setUrl("http://hlsztemgsplive.miguvideo.com:8080/migu/kailu/cctv1hd265/51/20191230/01.m3u8?msisdn=4f89b0f1d885527a227fd4a636419cfe&mdspid=&spid=699004&netType=4&sid=2201057821&pid=2028597139&timestamp=20260602114217&Channel_ID=0116_2600031500-99000-200300140100004&ProgramID=608807420&ParentNodeID=-99&assertID=2201057821&client_ip=116.130.185.147&SecurityKey=20260602114217&promotionId=&mvid=2201057821&mcid=500020&playurlVersion=ZQ-A1-9.5.1.2-RELEASE&userid=&jmhm=&videocodec=h265&appCode=miguvideo_android&bean=mgspad&tid=android&conFee=0&mtv_session=757cfcdcabe805c454b60db39878cc32&sv=10004&ct=android&playUrlSsn=4f89b0f1d885527a227fd4a636419cfe1780371733482&prlId=4f89b0f1d885527a227fd4a636419cfe1780371733491&jid=4f89b0f1d885527a227fd4a636419cfe1780371733042V&is_advertise=0&sjid=subsession_1780371764629&HlsSubType=1&HlsProfileId=1&nphaid=0&encrypt=4c148b083bf205e874f06c1bbd9fd553");
        Channel channel1 = new Channel().setName("test_channel1").setUrl("http://221.216.54.228:4022/rtp/239.3.1.129:8008");
        HttpGetCheckEngine.HttpGetCheckEngineParam param = new HttpGetCheckEngine.HttpGetCheckEngineParam();
        param.setParallelNum(10);
        Channel processed = engine.process(channel, JSONUtil.toJsonString(param));
        log.info("pressed channels => {}", processed);
        processed = engine.process(channel1, JSONUtil.toJsonString(param));
        log.info("pressed channels => {}", processed);
    }
}
