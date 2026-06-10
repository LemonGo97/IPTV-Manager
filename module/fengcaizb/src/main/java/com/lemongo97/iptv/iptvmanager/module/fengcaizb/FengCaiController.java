package com.lemongo97.iptv.iptvmanager.module.fengcaizb;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@AllArgsConstructor
@RequestMapping("/module/fengcai")
public class FengCaiController {
    private final FengCaiLiveService liveService;

    @GetMapping("/m3u8")
    public ResponseEntity<String> m3u8() throws IOException {
        String playList = liveService.getPlayList(PlaylistType.m3u8);

        // 设置响应标头
        HttpHeaders headers = new HttpHeaders();

        // 设置内容类型为纯文本，并指定字符集为 UTF-8，防止中文乱码
        headers.setContentType(new MediaType("application", "x-mpegurl", StandardCharsets.UTF_8));
        // 允许跨域，方便网页播放器调用
        headers.add("Access-Control-Allow-Origin", "*");
        // 设置缓存时间为 1 小时
        headers.add("Cache-Control", "max-age=21600");

        return new ResponseEntity<>(playList, headers, HttpStatus.OK);
    }

    @GetMapping("/m3u8-multi-line")
    public ResponseEntity<String> m3u8MultiLine() throws IOException {
        String playList = liveService.getPlayList(PlaylistType.m3u8_multi_line);

        // 设置响应标头
        HttpHeaders headers = new HttpHeaders();

        // 设置内容类型为纯文本，并指定字符集为 UTF-8，防止中文乱码
        headers.setContentType(new MediaType("application", "x-mpegurl", StandardCharsets.UTF_8));
        // 允许跨域，方便网页播放器调用
        headers.add("Access-Control-Allow-Origin", "*");
        // 设置缓存时间为 1 小时
        headers.add("Cache-Control", "max-age=21600");

        return new ResponseEntity<>(playList, headers, HttpStatus.OK);
    }

    @GetMapping("/text")
    public ResponseEntity<String> text() throws IOException {
        String playList = liveService.getPlayList(PlaylistType.txt);

        // 设置响应标头
        HttpHeaders headers = new HttpHeaders();

        // 设置内容类型为纯文本，并指定字符集为 UTF-8，防止中文乱码
        headers.setContentType(new MediaType("text", "plain", StandardCharsets.UTF_8));
        // 允许跨域，方便网页播放器调用
        headers.add("Access-Control-Allow-Origin", "*");
        // 设置缓存时间为 1 小时
        headers.add("Cache-Control", "max-age=21600");


        return new ResponseEntity<>(playList, headers, HttpStatus.OK);
    }
}
