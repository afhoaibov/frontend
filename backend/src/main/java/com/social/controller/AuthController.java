package com.social.controller;

import com.social.entity.User;
import com.social.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final UserRepository userRepository;
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");
        
        // 简单的用户验证（实际项目中应该使用加密密码）
        User user = userRepository.findByUsername(username).orElse(null);
        
        if (user != null && password.equals(user.getPassword())) {
            Map<String, Object> response = new HashMap<>();
            response.put("token", "mock-token-" + user.getId());
            response.put("user", user);
            response.put("message", "登录成功");
            
            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "用户名或密码错误");
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> registerData) {
        String username = registerData.get("username");
        String password = registerData.get("password");
        String email = registerData.get("email");
        
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(username)) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "用户名已存在");
            
            return ResponseEntity.badRequest().body(response);
        }
        
        // 创建新用户
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setEmail(email);
        newUser.setNickname(username);
        newUser.setScore(0);
        newUser.setFollowerCount(0);
        newUser.setFollowingCount(0);
        newUser.setPostCount(0);
        
        User savedUser = userRepository.save(newUser);
        
        Map<String, Object> response = new HashMap<>();
        response.put("token", "mock-token-" + savedUser.getId());
        response.put("user", savedUser);
        response.put("message", "注册成功");
        
        return ResponseEntity.ok(response);
    }
} 