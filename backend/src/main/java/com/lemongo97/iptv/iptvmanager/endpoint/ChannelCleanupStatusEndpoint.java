package com.lemongo97.iptv.iptvmanager.endpoint;

import com.lemongo97.iptv.iptvmanager.configuration.websocket.AbstractWebSocketEndpoint;
import com.lemongo97.iptv.iptvmanager.configuration.websocket.WebSocketEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@WebSocketEndpoint("/channel/cleanup/status")
public class ChannelCleanupStatusEndpoint extends AbstractWebSocketEndpoint {
    /**
     * <p>k -> sessionId
     * <p>v -> WebSocketSession
     */
    private final static ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
    }

    @Scheduled(fixedRate = 5000)
    public void publishCleanupStatus() {
        for (WebSocketSession session : sessions.values()) {
            try {
                session.sendMessage(new TextMessage("123123123"));
            } catch (IOException e) {
                log.error("Error while sending message to channel cleanup status", e);
            }
        }
    }

    @Override
    public void onTextMessage(WebSocketSession session, TextMessage message) {
        log.info("onTextMessage: {}", message.getPayload());
    }

    @Override
    public void onBinaryMessage(WebSocketSession session, BinaryMessage message) {
        log.info("onBinaryMessage: {}", message.getPayload());
    }
}
