package com.social.service;

import com.social.config.SimpleWebSocketHandler;
import com.social.entity.Message;
import com.social.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketNotificationService {
    
    private final SimpleWebSocketHandler webSocketHandler;
    
    /**
     * 发送通知给指定用户
     */
    public void sendNotificationToUser(Long userId, Message message, User fromUser) {
        try {
            Map<String, Object> notification = createNotificationPayload(message, fromUser);
            webSocketHandler.sendNotificationToUser(userId, notification);
            log.info("WebSocket通知已发送给用户: {}", userId);
        } catch (Exception e) {
            log.error("发送WebSocket通知失败，用户ID: {}", userId, e);
        }
    }
    
    /**
     * 批量发送通知给多个用户
     */
    public void sendNotificationToUsers(List<Long> userIds, Message message, User fromUser) {
        Map<String, Object> notification = createNotificationPayload(message, fromUser);
        
        for (Long userId : userIds) {
            try {
                webSocketHandler.sendNotificationToUser(userId, notification);
                log.debug("WebSocket通知已发送给用户: {}", userId);
            } catch (Exception e) {
                log.error("发送WebSocket通知失败，用户ID: {}", userId, e);
            }
        }
    }
    
    /**
     * 创建通知负载
     */
    private Map<String, Object> createNotificationPayload(Message message, User fromUser) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("id", message.getId());
        notification.put("type", message.getType().name());
        notification.put("title", message.getTitle());
        notification.put("content", message.getContent());
        notification.put("fromUser", fromUser != null ? fromUser.getUsername() : "系统");
        notification.put("createdAt", message.getCreatedAt() != null ? 
            message.getCreatedAt().toString() : java.time.LocalDateTime.now().toString());
        notification.put("isRead", message.getIsRead());
        notification.put("relatedPostId", message.getRelatedPostId());
        return notification;
    }
} 