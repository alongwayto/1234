package com.campus.equipment.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 事件监听器
 * 监听用户连接和断开事件
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final WebSocketPushService pushService;

    // 用户会话信息缓存
    private final Map<String, SessionInfo> sessionInfoMap = new ConcurrentHashMap<>();

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String userId = headerAccessor.getFirstNativeHeader("userId");
        
        if (userId != null && sessionId != null) {
            SessionInfo info = new SessionInfo();
            info.setSessionId(sessionId);
            info.setUserId(userId);
            info.setConnected(true);
            sessionInfoMap.put(sessionId, info);
            
            pushService.onUserOnline(sessionId, userId);
            log.info("WebSocket 连接建立: sessionId={}, userId={}", sessionId, userId);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        SessionInfo info = sessionInfoMap.remove(sessionId);
        if (info != null) {
            pushService.onUserOffline(sessionId);
            log.info("WebSocket 连接断开: sessionId={}, userId={}", sessionId, info.getUserId());
        }
    }

    /**
     * 获取所有在线会话
     */
    public Map<String, SessionInfo> getOnlineSessions() {
        return sessionInfoMap;
    }

    /**
     * 获取在线用户数
     */
    public int getOnlineUserCount() {
        return (int) sessionInfoMap.values().stream()
                .filter(SessionInfo::isConnected)
                .count();
    }

    /**
     * 会话信息内部类
     */
    @lombok.Data
    public static class SessionInfo {
        private String sessionId;
        private String userId;
        private boolean connected;
        private long connectTime;
        
        public SessionInfo() {
            this.connectTime = System.currentTimeMillis();
        }
    }
}
