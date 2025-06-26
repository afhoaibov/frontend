package com.social.service;

import com.social.entity.User;
import com.social.repository.UserRepository;
import com.social.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.redis.host", havingValue = "localhost", matchIfMissing = true)
public class RankingService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    
    private static final String POST_COUNT_RANKING_KEY = "ranking:post_count";
    private static final String POST_LIKES_RANKING_KEY = "ranking:post_likes";
    private static final String POST_COMMENTS_RANKING_KEY = "ranking:post_comments";
    private static final String COMPOSITE_SCORE_RANKING_KEY = "ranking:composite_score";
    
    /**
     * 实时更新用户发布动态数排行榜
     */
    public void updateUserPostCount(Long userId) {
        try {
            if (userId != null && redisTemplate != null) {
                // 从数据库获取用户最新动态数
                long postCount = postRepository.countByUserId(userId);
                redisTemplate.opsForZSet().add(POST_COUNT_RANKING_KEY, userId.toString(), postCount);
                log.info("实时更新用户{}的动态数排行榜，动态数: {}", userId, postCount);
                
                // 同时更新综合评分
                updateUserCompositeScore(userId);
            }
        } catch (Exception e) {
            log.error("更新用户动态数排行榜失败: userId={}", userId, e);
        }
    }
    
    /**
     * 实时更新用户动态总点赞数排行榜
     */
    public void updateUserPostLikes(Long userId) {
        try {
            if (userId != null && redisTemplate != null) {
                // 计算用户所有动态的总点赞数
                long totalLikes = postRepository.findByUserIdOrderByCreatedAtDesc(userId)
                    .stream()
                    .mapToLong(post -> post.getLikeCount() != null ? post.getLikeCount() : 0)
                    .sum();
                
                redisTemplate.opsForZSet().add(POST_LIKES_RANKING_KEY, userId.toString(), totalLikes);
                log.info("实时更新用户{}的动态点赞数排行榜，总点赞数: {}", userId, totalLikes);
                
                // 同时更新综合评分
                updateUserCompositeScore(userId);
            }
        } catch (Exception e) {
            log.error("更新用户动态点赞数排行榜失败: userId={}", userId, e);
        }
    }
    
    /**
     * 实时更新用户动态总评论数排行榜
     */
    public void updateUserPostComments(Long userId) {
        try {
            if (userId != null && redisTemplate != null) {
                // 计算用户所有动态的总评论数
                long totalComments = postRepository.findByUserIdOrderByCreatedAtDesc(userId)
                    .stream()
                    .mapToLong(post -> post.getCommentCount() != null ? post.getCommentCount() : 0)
                    .sum();
                
                redisTemplate.opsForZSet().add(POST_COMMENTS_RANKING_KEY, userId.toString(), totalComments);
                log.info("实时更新用户{}的动态评论数排行榜，总评论数: {}", userId, totalComments);
                
                // 同时更新综合评分
                updateUserCompositeScore(userId);
            }
        } catch (Exception e) {
            log.error("更新用户动态评论数排行榜失败: userId={}", userId, e);
        }
    }
    
    /**
     * 更新用户综合评分排行榜
     * 综合评分 = 动态数 × 10 + 总点赞数 × 5 + 总评论数 × 3
     */
    public void updateUserCompositeScore(Long userId) {
        try {
            if (userId != null && redisTemplate != null) {
                // 获取各项数据
                long postCount = postRepository.countByUserId(userId);
                
                long totalLikes = postRepository.findByUserIdOrderByCreatedAtDesc(userId)
                    .stream()
                    .mapToLong(post -> post.getLikeCount() != null ? post.getLikeCount() : 0)
                    .sum();
                
                long totalComments = postRepository.findByUserIdOrderByCreatedAtDesc(userId)
                    .stream()
                    .mapToLong(post -> post.getCommentCount() != null ? post.getCommentCount() : 0)
                    .sum();
                
                // 计算综合评分
                double compositeScore = postCount * 10 + totalLikes * 5 + totalComments * 3;
                
                redisTemplate.opsForZSet().add(COMPOSITE_SCORE_RANKING_KEY, userId.toString(), compositeScore);
                log.info("实时更新用户{}的综合评分排行榜，评分: {} (动态数:{}, 点赞数:{}, 评论数:{})", 
                    userId, compositeScore, postCount, totalLikes, totalComments);
            }
        } catch (Exception e) {
            log.error("更新用户综合评分排行榜失败: userId={}", userId, e);
        }
    }
    
    /**
     * 获取动态数排行榜
     */
    public Set<Object> getPostCountRanking(int start, int end) {
        try {
            if (redisTemplate != null) {
                return redisTemplate.opsForZSet().reverseRange(POST_COUNT_RANKING_KEY, start, end);
            }
        } catch (Exception e) {
            log.error("获取动态数排行榜时发生错误", e);
        }
        return null;
    }
    
    /**
     * 获取动态点赞数排行榜
     */
    public Set<Object> getPostLikesRanking(int start, int end) {
        try {
            if (redisTemplate != null) {
                return redisTemplate.opsForZSet().reverseRange(POST_LIKES_RANKING_KEY, start, end);
            }
        } catch (Exception e) {
            log.error("获取动态点赞数排行榜时发生错误", e);
        }
        return null;
    }
    
    /**
     * 获取动态评论数排行榜
     */
    public Set<Object> getPostCommentsRanking(int start, int end) {
        try {
            if (redisTemplate != null) {
                return redisTemplate.opsForZSet().reverseRange(POST_COMMENTS_RANKING_KEY, start, end);
            }
        } catch (Exception e) {
            log.error("获取动态评论数排行榜时发生错误", e);
        }
        return null;
    }
    
    /**
     * 获取综合评分排行榜
     */
    public Set<Object> getCompositeScoreRanking(int start, int end) {
        try {
            if (redisTemplate != null) {
                return redisTemplate.opsForZSet().reverseRange(COMPOSITE_SCORE_RANKING_KEY, start, end);
            }
        } catch (Exception e) {
            log.error("获取综合评分排行榜时发生错误", e);
        }
        return null;
    }
    
    /**
     * 获取综合评分排行榜（带分数）
     */
    public Map<String, Double> getCompositeScoreRankingWithScores(int start, int end) {
        try {
            if (redisTemplate != null) {
                Set<Object> userIds = redisTemplate.opsForZSet().reverseRange(COMPOSITE_SCORE_RANKING_KEY, start, end);
                Map<String, Double> result = new HashMap<>();
                
                if (userIds != null) {
                    for (Object userId : userIds) {
                        Double score = redisTemplate.opsForZSet().score(COMPOSITE_SCORE_RANKING_KEY, userId.toString());
                        if (score != null) {
                            result.put(userId.toString(), score);
                        }
                    }
                }
                
                return result;
            }
        } catch (Exception e) {
            log.error("获取综合评分排行榜（带分数）时发生错误", e);
        }
        return new HashMap<>();
    }
    
    /**
     * 获取用户排名
     */
    public Long getUserRank(Long userId, String rankingType) {
        try {
            if (userId != null && redisTemplate != null) {
                String key = getRankingKey(rankingType);
                return redisTemplate.opsForZSet().reverseRank(key, userId.toString());
            }
        } catch (Exception e) {
            log.error("获取用户排名时发生错误: userId={}, rankingType={}", userId, rankingType, e);
        }
        return null;
    }
    
    /**
     * 获取用户排行榜数据
     */
    public Map<String, Object> getUserRankingData(Long userId) {
        Map<String, Object> data = new HashMap<>();
        try {
            if (userId != null && redisTemplate != null) {
                // 获取各项排名
                data.put("postCountRank", getUserRank(userId, "post_count"));
                data.put("postLikesRank", getUserRank(userId, "post_likes"));
                data.put("postCommentsRank", getUserRank(userId, "post_comments"));
                data.put("compositeScoreRank", getUserRank(userId, "composite_score"));
                
                // 获取具体数值
                data.put("postCount", postRepository.countByUserId(userId));
                
                long totalLikes = postRepository.findByUserIdOrderByCreatedAtDesc(userId)
                    .stream()
                    .mapToLong(post -> post.getLikeCount() != null ? post.getLikeCount() : 0)
                    .sum();
                data.put("totalLikes", totalLikes);
                
                long totalComments = postRepository.findByUserIdOrderByCreatedAtDesc(userId)
                    .stream()
                    .mapToLong(post -> post.getCommentCount() != null ? post.getCommentCount() : 0)
                    .sum();
                data.put("totalComments", totalComments);
                
                double compositeScore = (long)data.get("postCount") * 10 + totalLikes * 5 + totalComments * 3;
                data.put("compositeScore", compositeScore);
            }
        } catch (Exception e) {
            log.error("获取用户排行榜数据时发生错误: userId={}", userId, e);
        }
        return data;
    }
    
    /**
     * 手动触发排行榜更新
     */
    public void manualUpdateRankings() {
        log.info("手动触发排行榜更新...");
        updateAllRankings();
    }
    
    /**
     * 定时更新所有排行榜数据
     */
    @Scheduled(fixedRate = 300000) // 每5分钟更新一次
    public void updateAllRankings() {
        try {
            log.info("开始更新所有排行榜数据...");
            
            if (redisTemplate == null) {
                log.error("RedisTemplate为空，跳过排行榜更新");
                return;
            }
            
            // 获取所有用户并更新排行榜
            List<User> allUsers = userRepository.findAll();
            for (User user : allUsers) {
                if (user != null && user.getId() != null) {
                    updateUserPostCount(user.getId());
                    updateUserPostLikes(user.getId());
                    updateUserPostComments(user.getId());
                    updateUserCompositeScore(user.getId());
                }
            }
            
            log.info("所有排行榜数据更新完成，共处理{}个用户", allUsers.size());
        } catch (Exception e) {
            log.error("更新排行榜数据时发生错误: {}", e.getMessage(), e);
        }
    }
    
    private String getRankingKey(String rankingType) {
        switch (rankingType) {
            case "post_count":
                return POST_COUNT_RANKING_KEY;
            case "post_likes":
                return POST_LIKES_RANKING_KEY;
            case "post_comments":
                return POST_COMMENTS_RANKING_KEY;
            case "composite_score":
                return COMPOSITE_SCORE_RANKING_KEY;
            default:
                return COMPOSITE_SCORE_RANKING_KEY;
        }
    }
} 