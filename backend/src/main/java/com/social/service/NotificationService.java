package com.social.service;

import com.social.config.SimpleWebSocketHandler;
import com.social.entity.Message;
import com.social.entity.User;
import com.social.repository.MessageRepository;
import com.social.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    
    private final SimpleWebSocketHandler webSocketHandler;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    
    /**
     * 发送点赞通知
     */
    public void sendLikeNotification(Long fromUserId, Long toUserId, Long postId) {
        try {
            User fromUser = userRepository.findById(fromUserId).orElse(null);
            if (fromUser == null) return;
            
            String content = String.format("%s 点赞了你的动态", fromUser.getNickname() != null ? fromUser.getNickname() : fromUser.getUsername());
            
            // 保存到数据库
            Message message = new Message();
            message.setFromUser(fromUser);
            message.setToUser(userRepository.findById(toUserId).orElse(null));
            message.setContent(content);
            message.setType(Message.MessageType.LIKE);
            message.setRelatedPostId(postId);
            messageRepository.save(message);
            
            // 发送WebSocket消息
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "LIKE");
            notification.put("content", content);
            notification.put("fromUser", fromUser.getUsername());
            notification.put("postId", postId);
            notification.put("timestamp", System.currentTimeMillis());
            
            webSocketHandler.sendNotificationToUser(toUserId, notification);
            
            log.info("发送点赞通知: {} -> {}", fromUserId, toUserId);
        } catch (Exception e) {
            log.error("发送点赞通知失败", e);
        }
    }
    
    /**
     * 发送评论通知
     */
    public void sendCommentNotification(Long fromUserId, Long toUserId, Long postId, String comment) {
        try {
            User fromUser = userRepository.findById(fromUserId).orElse(null);
            if (fromUser == null) return;
            
            String content = String.format("%s 评论了你的动态: %s", 
                fromUser.getNickname() != null ? fromUser.getNickname() : fromUser.getUsername(),
                comment.length() > 20 ? comment.substring(0, 20) + "..." : comment);
            
            // 保存到数据库
            Message message = new Message();
            message.setFromUser(fromUser);
            message.setToUser(userRepository.findById(toUserId).orElse(null));
            message.setContent(content);
            message.setType(Message.MessageType.COMMENT);
            message.setRelatedPostId(postId);
            messageRepository.save(message);
            
            // 发送WebSocket消息
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "COMMENT");
            notification.put("content", content);
            notification.put("fromUser", fromUser.getUsername());
            notification.put("postId", postId);
            notification.put("comment", comment);
            notification.put("timestamp", System.currentTimeMillis());
            
            webSocketHandler.sendNotificationToUser(toUserId, notification);
            
            log.info("发送评论通知: {} -> {}", fromUserId, toUserId);
        } catch (Exception e) {
            log.error("发送评论通知失败", e);
        }
    }
    
    /**
     * 发送关注通知
     */
    public void sendFollowNotification(Long fromUserId, Long toUserId) {
        try {
            User fromUser = userRepository.findById(fromUserId).orElse(null);
            if (fromUser == null) return;
            
            String content = String.format("%s 关注了你", 
                fromUser.getNickname() != null ? fromUser.getNickname() : fromUser.getUsername());
            
            // 保存到数据库
            Message message = new Message();
            message.setFromUser(fromUser);
            message.setToUser(userRepository.findById(toUserId).orElse(null));
            message.setContent(content);
            message.setType(Message.MessageType.FOLLOW);
            messageRepository.save(message);
            
            // 发送WebSocket消息
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "FOLLOW");
            notification.put("content", content);
            notification.put("fromUser", fromUser.getUsername());
            notification.put("timestamp", System.currentTimeMillis());
            
            webSocketHandler.sendNotificationToUser(toUserId, notification);
            
            log.info("发送关注通知: {} -> {}", fromUserId, toUserId);
        } catch (Exception e) {
            log.error("发送关注通知失败", e);
        }
    }
    
    /**
     * 发送系统通知
     */
    public void sendSystemNotification(Long toUserId, String content) {
        try {
            // 保存到数据库
            Message message = new Message();
            message.setToUser(userRepository.findById(toUserId).orElse(null));
            message.setContent(content);
            message.setType(Message.MessageType.SYSTEM);
            messageRepository.save(message);
            
            // 发送WebSocket消息
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "SYSTEM");
            notification.put("content", content);
            notification.put("timestamp", System.currentTimeMillis());
            
            webSocketHandler.sendNotificationToUser(toUserId, notification);
            
            log.info("发送系统通知: {}", toUserId);
        } catch (Exception e) {
            log.error("发送系统通知失败", e);
        }
    }
} 