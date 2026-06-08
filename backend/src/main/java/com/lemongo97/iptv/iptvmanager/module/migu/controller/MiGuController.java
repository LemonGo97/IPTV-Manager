package com.lemongo97.iptv.iptvmanager.module.migu.controller;

import com.lemongo97.iptv.iptvmanager.module.migu.PlaylistType;
import com.lemongo97.iptv.iptvmanager.module.migu.service.MiguApiService;
import com.lemongo97.iptv.iptvmanager.module.migu.service.MiguPlaylistService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequestMapping("/module/migu")
@AllArgsConstructor
public class MiGuController {

    private final MiguApiService miguApiService;
    private final MiguPlaylistService playlistService;

    @GetMapping("/m3u8")
    public ResponseEntity<String> m3u8() throws IOException {
        String playList = playlistService.getPlayList(PlaylistType.m3u8);

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
        String playList = playlistService.getPlayList(PlaylistType.txt);

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

    @GetMapping("/play/{programId}")
    public ResponseEntity<Void> play(@PathVariable String programId){
        String targetUrl;

        try{
            targetUrl = miguApiService.fetchRealChannelUrl(programId);
        }catch (Exception e){
            log.error("Error fetching channel url, params: pid => {}", programId, e);
            return ResponseEntity.notFound().build();
        }

        if (StringUtils.isBlank(targetUrl)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity
                .status(HttpStatus.FOUND) // 设置 302 状态码
                .location(URI.create(targetUrl)) // 设置 Location 响应标头
                .build();
    }

}
