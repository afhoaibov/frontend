package com.social.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class SimpleWebSocketHandler extends TextWebSocketHandler {
    
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, Long> userSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket连接建立: {}", session.getId());
        sessions.put(session.getId(), session);
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            String payload = message.getPayload();
            Map<String, Object> data = objectMapper.readValue(payload, Map.class);
            
            String type = (String) data.get("type");
            
            switch (type) {
                case "AUTH":
                    handleAuth(session, data);
                    break;
                case "PING":
                    handlePing(session);
                    break;
                default:
                    log.warn("未知的消息类型: {}", type);
            }
        } catch (Exception e) {
            log.error("处理WebSocket消息失败", e);
        }
    }
    
    private void handleAuth(WebSocketSession session, Map<String, Object> data) {
        Long userId = Long.valueOf(data.get("userId").toString());
        userSessions.put(session.getId(), userId);
        log.info("用户 {} 认证成功", userId);
        
        // 发送认证成功消息
        Map<String, Object> response = new HashMap<>();
        response.put("type", "AUTH_SUCCESS");
        sendMessage(session, response);
    }
    
    private void handlePing(WebSocketSession session) {
        // 发送pong响应
        Map<String, Object> response = new HashMap<>();
        response.put("type", "PONG");
        sendMessage(session, response);
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("WebSocket连接关闭: {}", session.getId());
        sessions.remove(session.getId());
        userSessions.remove(session.getId());
    }
    
    public void sendNotificationToUser(Long userId, Map<String, Object> notification) {
        log.info("尝试发送WebSocket通知给用户: {}, 当前在线用户数量: {}", userId, userSessions.size());
        
        // 找到用户的WebSocket会话
        userSessions.entrySet().stream()
                .filter(entry -> entry.getValue().equals(userId))
                .forEach(entry -> {
                    WebSocketSession session = sessions.get(entry.getKey());
                    if (session != null && session.isOpen()) {
                        log.info("找到用户 {} 的在线会话，准备发送通知", userId);
                        Map<String, Object> message = new HashMap<>();
                        message.put("type", "NOTIFICATION");
                        message.put("payload", notification);
                        sendMessage(session, message);
                        log.info("WebSocket通知已发送给用户: {}", userId);
                    } else {
                        log.warn("用户 {} 的WebSocket会话不存在或已关闭", userId);
                    }
                });
        
        // 如果没有找到用户的会话，记录日志
        boolean userFound = userSessions.values().stream().anyMatch(id -> id.equals(userId));
        if (!userFound) {
            log.info("用户 {} 当前不在线，无法发送WebSocket通知", userId);
        }
    }
    
    private void sendMessage(WebSocketSession session, Map<String, Object> message) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(jsonMessage));
        } catch (IOException e) {
            log.error("发送WebSocket消息失败", e);
        }
    }
    
    /**
     * 获取在线用户数量
     */
    public int getOnlineUserCount() {
        return userSessions.size();
    }
    
    /**
     * 获取活跃会话数量
     */
    public int getActiveSessionCount() {
        return sessions.size();
    }
    
    /**
     * 获取在线用户ID列表
     */
    public java.util.List<Long> getOnlineUserIds() {
        return new java.util.ArrayList<>(userSessions.values());
    }
} 