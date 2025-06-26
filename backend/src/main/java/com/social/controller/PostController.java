package com.social.controller;

import com.social.entity.Post;
import com.social.entity.User;
import com.social.repository.PostRepository;
import com.social.repository.UserRepository;
import com.social.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    
    @Autowired
    private PostService postService;
    
    /**
     * 发布动态
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createPost(@RequestBody Map<String, String> postData) {
        try {
            Long userId = Long.parseLong(postData.get("userId"));
            String content = postData.get("content");
            
            Post savedPost = postService.createPost(userId, content);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "动态发布成功");
            response.put("data", savedPost);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "发布失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 删除动态
     * DELETE /api/posts/{postId}
     */
    @DeleteMapping("/{postId}")
    public ResponseEntity<Map<String, Object>> deletePost(
            @PathVariable Long postId,
            @RequestBody Map<String, Object> requestBody) {
        
        try {
            Long userId = Long.valueOf(requestBody.get("userId").toString());
            
            postService.deletePost(postId, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "动态删除成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取动态列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            Page<Post> posts = postService.getPosts(page, size);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", posts.getContent());
            response.put("totalElements", posts.getTotalElements());
            response.put("totalPages", posts.getTotalPages());
            response.put("currentPage", posts.getNumber());
            response.put("size", posts.getSize());
            response.put("hasNext", posts.hasNext());
            response.put("hasPrevious", posts.hasPrevious());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取用户的动态列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserPosts(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            Page<Post> posts = postService.getUserPosts(userId, page, size);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", posts.getContent());
            response.put("totalElements", posts.getTotalElements());
            response.put("totalPages", posts.getTotalPages());
            response.put("currentPage", posts.getNumber());
            response.put("size", posts.getSize());
            response.put("hasNext", posts.hasNext());
            response.put("hasPrevious", posts.hasPrevious());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取单个动态详情
     */
    @GetMapping("/{postId}")
    public ResponseEntity<Map<String, Object>> getPost(@PathVariable Long postId) {
        try {
            Post post = postService.getPost(postId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", post);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 点赞动态
     */
    @PostMapping("/{postId}/like")
    public ResponseEntity<Map<String, Object>> likePost(
            @PathVariable Long postId,
            @RequestBody Map<String, Long> likeData) {
        
        try {
            Long userId = likeData.get("userId");
            
            // 验证用户是否存在
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "用户不存在，请先登录");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 使用PostService处理点赞
            postService.likePost(postId, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "点赞成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "点赞失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 取消点赞动态
     */
    @DeleteMapping("/{postId}/like")
    public ResponseEntity<Map<String, Object>> unlikePost(
            @PathVariable Long postId,
            @RequestBody Map<String, Long> unlikeData) {
        
        try {
            Long userId = unlikeData.get("userId");
            
            // 验证用户是否存在
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "用户不存在，请先登录");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 使用PostService处理取消点赞
            postService.unlikePost(postId, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "取消点赞成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "取消点赞失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
} 