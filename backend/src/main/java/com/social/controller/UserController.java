package com.social.controller;

import com.social.entity.User;
import com.social.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserRepository userRepository;
    
    /**
     * 获取用户信息
     */
    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getUserInfo(@PathVariable Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        
        if (user == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "用户不存在");
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("user", user);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取所有用户列表
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        List<User> users = userRepository.findAll();
        
        Map<String, Object> response = new HashMap<>();
        response.put("users", users);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 更新用户信息
     */
    @PutMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> updateUserInfo(
            @PathVariable Long userId,
            @RequestBody Map<String, String> userData) {
        
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "用户不存在");
                return ResponseEntity.notFound().build();
            }
            
            // 更新用户信息
            if (userData.containsKey("nickname")) {
                user.setNickname(userData.get("nickname"));
            }
            if (userData.containsKey("bio")) {
                user.setBio(userData.get("bio"));
            }
            if (userData.containsKey("email")) {
                user.setEmail(userData.get("email"));
            }
            if (userData.containsKey("avatar")) {
                user.setAvatar(userData.get("avatar"));
            }
            
            User updatedUser = userRepository.save(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "用户信息更新成功");
            response.put("user", updatedUser);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "更新失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
} 