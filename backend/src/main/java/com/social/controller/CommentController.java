package com.social.controller;

import com.social.entity.Comment;
import com.social.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
@CrossOrigin(origins = "*")
public class CommentController {
    
    @Autowired
    private CommentService commentService;
    
    /**
     * 获取动态评论列表
     * GET /api/posts/{postId}/comments?page={page}&size={size}&userId={userId}
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long userId) {
        
        try {
            Page<Comment> comments = commentService.getCommentsByPostId(postId, page, size, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", comments.getContent());
            response.put("totalElements", comments.getTotalElements());
            response.put("totalPages", comments.getTotalPages());
            response.put("currentPage", comments.getNumber());
            response.put("size", comments.getSize());
            response.put("hasNext", comments.hasNext());
            response.put("hasPrevious", comments.hasPrevious());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 发表评论
     * POST /api/posts/{postId}/comments
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createComment(
            @PathVariable Long postId,
            @RequestBody Map<String, Object> requestBody) {
        
        try {
            // 从请求体中获取参数
            Long userId = Long.valueOf(requestBody.get("userId").toString());
            String content = requestBody.get("content").toString();
            Long parentId = null;
            
            if (requestBody.containsKey("parentId") && requestBody.get("parentId") != null) {
                parentId = Long.valueOf(requestBody.get("parentId").toString());
            }
            
            // 验证参数
            if (content == null || content.trim().isEmpty()) {
                throw new RuntimeException("评论内容不能为空");
            }
            
            Comment comment = commentService.createComment(postId, userId, content, parentId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", comment);
            response.put("message", "评论发表成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 删除评论
     * DELETE /api/posts/{postId}/comments/{commentId}
     * 支持两种方式传递userId：
     * 1. 请求体: { "userId": 1 }
     * 2. 请求头: X-User-Id: 1
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Map<String, Object>> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestParam(required = false) Long userId,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestBody(required = false) Map<String, Object> requestBody) {
        
        try {
            // 优先从请求体获取userId，其次从请求头，最后从查询参数
            Long finalUserId = null;
            if (requestBody != null && requestBody.containsKey("userId")) {
                finalUserId = Long.valueOf(requestBody.get("userId").toString());
            } else if (headerUserId != null) {
                finalUserId = headerUserId;
            } else if (userId != null) {
                finalUserId = userId;
            }
            
            if (finalUserId == null) {
                throw new RuntimeException("用户ID不能为空");
            }
            
            commentService.deleteComment(postId, commentId, finalUserId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "评论删除成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 点赞/取消点赞评论
     * POST /api/posts/{postId}/comments/{commentId}/like
     */
    @PostMapping("/{commentId}/like")
    public ResponseEntity<Map<String, Object>> toggleCommentLike(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestBody Map<String, Object> requestBody) {
        
        try {
            Long userId = Long.valueOf(requestBody.get("userId").toString());
            
            Comment comment = commentService.toggleCommentLike(postId, commentId, userId);
            
            // 根据当前点赞状态返回不同的消息
            boolean isLiked = comment.getIsLiked();
            String message = isLiked ? "点赞成功" : "取消点赞成功";
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", comment);
            response.put("message", message);
            response.put("isLiked", isLiked);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取评论的回复列表
     * GET /api/posts/{postId}/comments/{commentId}/replies?userId={userId}
     */
    @GetMapping("/{commentId}/replies")
    public ResponseEntity<Map<String, Object>> getCommentReplies(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestParam(required = false) Long userId) {
        
        try {
            List<Comment> replies = commentService.getCommentReplies(commentId, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", replies);
            response.put("total", replies.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
} 