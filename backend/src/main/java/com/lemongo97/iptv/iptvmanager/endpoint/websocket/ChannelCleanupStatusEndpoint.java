package com.lemongo97.iptv.iptvmanager.endpoint.websocket;

import com.lemongo97.iptv.iptvmanager.configuration.websocket.AbstractWebSocketEndpoint;
import com.lemongo97.iptv.iptvmanager.configuration.websocket.WebSocketEndpoint;
import com.lemongo97.iptv.iptvmanager.entity.TaskProgress;
import com.lemongo97.iptv.iptvmanager.service.TaskProgressService;
import com.lemongo97.iptv.iptvmanager.utils.JSONUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@AllArgsConstructor
@WebSocketEndpoint("/channel/cleanup/status")
public class ChannelCleanupStatusEndpoint extends AbstractWebSocketEndpoint {
    /**
     * <p>k -> sessionId
     * <p>v -> WebSocketSession
     */
    private final static ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    private final TaskProgressService taskProgressService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        log.debug("Session ID: {}", sessionId);
        sessions.put(sessionId, session);
    }

    @Scheduled(fixedRate = 5000)
    public void publishCleanupStatus() {
        if (sessions.isEmpty()) return;

        Optional<TaskProgress> latestTask = taskProgressService.getLatestTask("DATA_CLEANUP");
        if (latestTask.isEmpty()) return;

        for (WebSocketSession session : sessions.values()) {
            try {
                session.sendMessage(new TextMessage(JSONUtil.toJsonString(latestTask.get())));
            } catch (IOException e) {
                log.error("Error while sending message to channel cleanup status", e);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = session.getId();
        try{
            session.close(Optional.of(status).orElse(CloseStatus.NOT_ACCEPTABLE));
        }catch (Exception e){
            log.error("Error closing websocket session", e);
        }finally {
            sessions.remove(sessionId);
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
