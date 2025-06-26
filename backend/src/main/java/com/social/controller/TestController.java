package com.social.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.social.config.SimpleWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {
    
    private final SimpleWebSocketHandler webSocketHandler;
    private final ObjectMapper objectMapper;
    
    /**
     * 测试WebSocket连接状态
     */
    @GetMapping("/websocket/status")
    public ResponseEntity<Map<String, Object>> getWebSocketStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("onlineUsers", webSocketHandler.getOnlineUserCount());
        response.put("activeSessions", webSocketHandler.getActiveSessionCount());
        response.put("onlineUserIds", webSocketHandler.getOnlineUserIds());
        return ResponseEntity.ok(response);
    }
    
    /**
     * 测试发送WebSocket通知
     */
    @PostMapping("/websocket/send")
    public ResponseEntity<Map<String, Object>> sendTestNotification(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            String message = (String) request.get("message");
            
            Map<String, Object> notification = new HashMap<>();
            notification.put("id", System.currentTimeMillis());
            notification.put("type", "TEST");
            notification.put("title", "测试通知");
            notification.put("content", message);
            notification.put("fromUser", "测试系统");
            notification.put("createdAt", java.time.LocalDateTime.now().toString());
            notification.put("isRead", false);
            
            webSocketHandler.sendNotificationToUser(userId, notification);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "测试通知发送成功");
            response.put("userId", userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("发送测试通知失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "发送测试通知失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 测试LocalDateTime序列化
     */
    @GetMapping("/test/datetime")
    public ResponseEntity<Map<String, Object>> testDateTimeSerialization() {
        try {
            Map<String, Object> testData = new HashMap<>();
            testData.put("timestamp", java.time.LocalDateTime.now());
            testData.put("message", "测试LocalDateTime序列化");
            
            // 测试序列化
            String json = objectMapper.writeValueAsString(testData);
            log.info("序列化结果: {}", json);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("serializedData", json);
            response.put("message", "LocalDateTime序列化测试成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("LocalDateTime序列化测试失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "LocalDateTime序列化测试失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
} 