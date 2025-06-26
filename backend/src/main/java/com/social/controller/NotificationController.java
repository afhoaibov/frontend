package com.social.controller;

import com.social.entity.Message;
import com.social.entity.User;
import com.social.repository.MessageRepository;
import com.social.repository.UserRepository;
import com.social.service.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final WebSocketNotificationService webSocketNotificationService;
    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);
    
    /**
     * 获取用户消息列表
     */
    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getUserMessages(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Message> messages = messageRepository.findByToUserIdOrderByCreatedAtDesc(userId, pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("messages", messages.getContent());
            response.put("totalElements", messages.getTotalElements());
            response.put("totalPages", messages.getTotalPages());
            response.put("currentPage", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取用户消息列表失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "获取消息列表失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取未读消息数量
     */
    @GetMapping("/{userId}/unread")
    public ResponseEntity<Map<String, Object>> getUnreadCount(@PathVariable Long userId) {
        try {
            Long unreadCount = messageRepository.countUnreadMessagesByUserId(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("unreadCount", unreadCount);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取未读消息数量失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "获取未读消息数量失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 标记所有消息为已读
     */
    @PutMapping("/{userId}/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead(@PathVariable Long userId) {
        try {
            messageRepository.markAllAsReadByUserId(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "所有消息已标记为已读");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("标记消息为已读失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "标记消息为已读失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 管理员发布通知
     */
    @PostMapping("/admin/publish")
    public ResponseEntity<Map<String, Object>> publishNotification(@RequestBody Map<String, Object> request) {
        try {
            String type = (String) request.get("type");
            String title = (String) request.get("title");
            String content = (String) request.get("content");
            String targetType = (String) request.get("targetType");
            
            // 修复类型转换问题：将Integer转换为Long
            List<Long> targetUserIds = null;
            Object targetUserIdsObj = request.get("targetUserIds");
            if (targetUserIdsObj instanceof List) {
                List<?> rawList = (List<?>) targetUserIdsObj;
                targetUserIds = rawList.stream()
                    .map(item -> {
                        if (item instanceof Integer) {
                            return ((Integer) item).longValue();
                        } else if (item instanceof Long) {
                            return (Long) item;
                        } else {
                            return Long.valueOf(item.toString());
                        }
                    })
                    .collect(Collectors.toList());
            }
            
            // 获取系统用户（管理员）
            User systemUser = userRepository.findById(1L).orElse(null);
            if (systemUser == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "系统用户不存在");
                return ResponseEntity.badRequest().body(response);
            }
            
            if ("ALL".equals(targetType)) {
                // 发送给所有用户
                List<Long> allUserIds = messageRepository.getAllUserIds();
                log.info("发送给所有用户，用户数量: {}", allUserIds.size());
                for (Long userId : allUserIds) {
                    User toUser = userRepository.findById(userId).orElse(null);
                    if (toUser != null) {
                        Message message = new Message();
                        message.setFromUser(systemUser);
                        message.setToUser(toUser);
                        message.setType(Message.MessageType.valueOf(type));
                        message.setTitle(title);
                        message.setContent(content);
                        message.setIsRead(false);
                        message.setCreatedAt(LocalDateTime.now());
                        Message savedMessage = messageRepository.save(message);
                        
                        log.info("保存通知成功，通知ID: {}, 发送给用户: {}", savedMessage.getId(), userId);
                        
                        // 发送WebSocket通知给在线用户
                        try {
                            webSocketNotificationService.sendNotificationToUser(userId, savedMessage, systemUser);
                            log.info("WebSocket通知发送成功，用户ID: {}", userId);
                        } catch (Exception e) {
                            log.error("WebSocket通知发送失败，用户ID: {}", userId, e);
                        }
                    }
                }
            } else if ("SPECIFIC".equals(targetType) && targetUserIds != null) {
                // 发送给指定用户
                log.info("发送给指定用户，用户数量: {}", targetUserIds.size());
                for (Long userId : targetUserIds) {
                    User toUser = userRepository.findById(userId).orElse(null);
                    if (toUser != null) {
                        Message message = new Message();
                        message.setFromUser(systemUser);
                        message.setToUser(toUser);
                        message.setType(Message.MessageType.valueOf(type));
                        message.setTitle(title);
                        message.setContent(content);
                        message.setIsRead(false);
                        message.setCreatedAt(LocalDateTime.now());
                        Message savedMessage = messageRepository.save(message);
                        
                        log.info("保存通知成功，通知ID: {}, 发送给用户: {}", savedMessage.getId(), userId);
                        
                        // 发送WebSocket通知给在线用户
                        try {
                            webSocketNotificationService.sendNotificationToUser(userId, savedMessage, systemUser);
                            log.info("WebSocket通知发送成功，用户ID: {}", userId);
                        } catch (Exception e) {
                            log.error("WebSocket通知发送失败，用户ID: {}", userId, e);
                        }
                    }
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "通知发布成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("发布通知失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "发布通知失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取管理员通知列表
     */
    @GetMapping("/admin")
    public ResponseEntity<Map<String, Object>> getAdminNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Message> notifications = messageRepository.findSystemMessagesOrderByCreatedAtDesc(pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("notifications", notifications.getContent());
            response.put("totalElements", notifications.getTotalElements());
            response.put("totalPages", notifications.getTotalPages());
            response.put("currentPage", page);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取管理员通知列表失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "获取通知列表失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 删除通知
     */
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Map<String, Object>> deleteNotification(@PathVariable Long notificationId) {
        try {
            messageRepository.deleteById(notificationId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "通知删除成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("删除通知失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "删除通知失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
} 