package com.social.controller;

import com.social.entity.User;
import com.social.repository.UserRepository;
import com.social.repository.PostRepository;
import com.social.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class RankingController {
    
    private final RankingService rankingService;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    
    /**
     * 获取动态数排行榜
     */
    @GetMapping("/post-count")
    public ResponseEntity<Map<String, Object>> getPostCountRanking(
            @RequestParam(defaultValue = "0") int start,
            @RequestParam(defaultValue = "9") int end) {
        
        Set<Object> userIds = rankingService.getPostCountRanking(start, end);
        List<User> users = userRepository.findAllById(
            userIds.stream().map(id -> Long.valueOf(id.toString())).collect(Collectors.toList())
        );
        
        Map<String, Object> response = new HashMap<>();
        response.put("users", users);
        response.put("total", rankingService.getPostCountRanking(0, -1).size());
        response.put("type", "post_count");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取动态点赞数排行榜
     */
    @GetMapping("/post-likes")
    public ResponseEntity<Map<String, Object>> getPostLikesRanking(
            @RequestParam(defaultValue = "0") int start,
            @RequestParam(defaultValue = "9") int end) {
        
        Set<Object> userIds = rankingService.getPostLikesRanking(start, end);
        List<User> users = userRepository.findAllById(
            userIds.stream().map(id -> Long.valueOf(id.toString())).collect(Collectors.toList())
        );
        
        Map<String, Object> response = new HashMap<>();
        response.put("users", users);
        response.put("total", rankingService.getPostLikesRanking(0, -1).size());
        response.put("type", "post_likes");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取动态评论数排行榜
     */
    @GetMapping("/post-comments")
    public ResponseEntity<Map<String, Object>> getPostCommentsRanking(
            @RequestParam(defaultValue = "0") int start,
            @RequestParam(defaultValue = "9") int end) {
        
        Set<Object> userIds = rankingService.getPostCommentsRanking(start, end);
        List<User> users = userRepository.findAllById(
            userIds.stream().map(id -> Long.valueOf(id.toString())).collect(Collectors.toList())
        );
        
        Map<String, Object> response = new HashMap<>();
        response.put("users", users);
        response.put("total", rankingService.getPostCommentsRanking(0, -1).size());
        response.put("type", "post_comments");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取综合评分排行榜
     */
    @GetMapping("/composite-score")
    public ResponseEntity<Map<String, Object>> getCompositeScoreRanking(
            @RequestParam(defaultValue = "0") int start,
            @RequestParam(defaultValue = "9") int end) {
        
        // 获取用户ID和对应的综合评分
        Map<String, Double> userScores = rankingService.getCompositeScoreRankingWithScores(start, end);
        
        // 获取用户详细信息
        List<Long> userIds = userScores.keySet().stream()
            .map(Long::valueOf)
            .collect(Collectors.toList());
        List<User> users = userRepository.findAllById(userIds);
        
        // 构建包含用户信息和分数的响应数据
        List<Map<String, Object>> userRankingData = users.stream()
            .map(user -> {
                Map<String, Object> userData = new HashMap<>();
                userData.put("user", user);
                userData.put("compositeScore", userScores.get(user.getId().toString()));
                
                // 计算各项指标
                long postCount = postRepository.countByUserId(user.getId());
                long totalLikes = postRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                    .stream()
                    .mapToLong(post -> post.getLikeCount() != null ? post.getLikeCount() : 0)
                    .sum();
                long totalComments = postRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                    .stream()
                    .mapToLong(post -> post.getCommentCount() != null ? post.getCommentCount() : 0)
                    .sum();
                
                userData.put("postCount", postCount);
                userData.put("totalLikes", totalLikes);
                userData.put("totalComments", totalComments);
                
                return userData;
            })
            .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("userRankings", userRankingData);
        response.put("total", rankingService.getCompositeScoreRanking(0, -1).size());
        response.put("type", "composite_score");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取用户排名
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserRank(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "composite_score") String type) {
        
        Long rank = rankingService.getUserRank(userId, type);
        
        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("type", type);
        response.put("rank", rank != null ? rank + 1 : null);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取用户详细排行榜数据
     */
    @GetMapping("/user/{userId}/details")
    public ResponseEntity<Map<String, Object>> getUserRankingDetails(@PathVariable Long userId) {
        Map<String, Object> rankingData = rankingService.getUserRankingData(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("data", rankingData);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 手动更新排行榜
     */
    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> updateRankings() {
        try {
            rankingService.manualUpdateRankings();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "排行榜更新成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "排行榜更新失败: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 实时更新指定用户的排行榜数据
     */
    @PostMapping("/update/user/{userId}")
    public ResponseEntity<Map<String, Object>> updateUserRanking(@PathVariable Long userId) {
        try {
            rankingService.updateUserPostCount(userId);
            rankingService.updateUserPostLikes(userId);
            rankingService.updateUserPostComments(userId);
            rankingService.updateUserCompositeScore(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "用户排行榜更新成功");
            response.put("userId", userId);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "用户排行榜更新失败: " + e.getMessage());
            response.put("userId", userId);
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 兼容接口：获取分数排行榜（重定向到综合评分排行榜）
     */
    @GetMapping("/score")
    public ResponseEntity<Map<String, Object>> getScoreRanking(
            @RequestParam(defaultValue = "0") int start,
            @RequestParam(defaultValue = "9") int end) {
        
        // 重定向到综合评分排行榜
        Set<Object> userIds = rankingService.getCompositeScoreRanking(start, end);
        List<User> users = userRepository.findAllById(
            userIds.stream().map(id -> Long.valueOf(id.toString())).collect(Collectors.toList())
        );
        
        Map<String, Object> response = new HashMap<>();
        response.put("users", users);
        response.put("total", rankingService.getCompositeScoreRanking(0, -1).size());
        response.put("type", "score");
        response.put("note", "此接口已重定向到综合评分排行榜");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 兼容接口：获取粉丝数排行榜（重定向到动态点赞数排行榜）
     */
    @GetMapping("/followers")
    public ResponseEntity<Map<String, Object>> getFollowerRanking(
            @RequestParam(defaultValue = "0") int start,
            @RequestParam(defaultValue = "9") int end) {
        
        // 重定向到动态点赞数排行榜
        Set<Object> userIds = rankingService.getPostLikesRanking(start, end);
        List<User> users = userRepository.findAllById(
            userIds.stream().map(id -> Long.valueOf(id.toString())).collect(Collectors.toList())
        );
        
        Map<String, Object> response = new HashMap<>();
        response.put("users", users);
        response.put("total", rankingService.getPostLikesRanking(0, -1).size());
        response.put("type", "followers");
        response.put("note", "此接口已重定向到动态点赞数排行榜");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 兼容接口：获取动态数排行榜（旧接口名）
     */
    @GetMapping("/posts")
    public ResponseEntity<Map<String, Object>> getPostsRanking(
            @RequestParam(defaultValue = "0") int start,
            @RequestParam(defaultValue = "9") int end) {
        
        // 重定向到动态数排行榜
        Set<Object> userIds = rankingService.getPostCountRanking(start, end);
        List<User> users = userRepository.findAllById(
            userIds.stream().map(id -> Long.valueOf(id.toString())).collect(Collectors.toList())
        );
        
        Map<String, Object> response = new HashMap<>();
        response.put("users", users);
        response.put("total", rankingService.getPostCountRanking(0, -1).size());
        response.put("type", "posts");
        response.put("note", "此接口已重定向到动态数排行榜");
        
        return ResponseEntity.ok(response);
    }
} 