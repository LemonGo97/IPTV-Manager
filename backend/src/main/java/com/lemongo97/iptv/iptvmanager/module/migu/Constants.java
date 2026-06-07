package com.lemongo97.iptv.iptvmanager.module.migu;

public interface Constants {

    String BASE_URL = "https://program-sc.miguvideo.com/live/v2/tv-data";
    String CATEGORY_URL = BASE_URL + "/1ff892f2b5ab4a79be6e25b69d2f5d05";
    String CHANNELS_URL = BASE_URL + "/%s";

    String LIVE_CHANNELS_URL = "http://pro.fengcaizb.com/channels/pro.gz";
    String LIVE_CHANNELS_REFER = "http://pro.fengcaizb.com";

    String LIVE_CHANNELS_DECRYPT_KEY_HEX = "796F75216A6540313972722432307923";
    String LIVE_CHANNELS_DECRYPT_IV_HEX = "417265796F757C3E7F6E36260D616E3F";

}
