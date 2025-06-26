package com.social.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Entity
@Table(name = "comments")
public class Comment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "post_id", nullable = false)
    private Long postId;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "parent_id")
    private Long parentId;
    
    @Column(name = "like_count")
    private Integer likeCount = 0;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // 临时字段，用于API响应时包含用户信息
    @Transient
    private Map<String, Object> userInfo;
    
    // 临时字段，用于API响应时包含子评论
    @Transient
    private List<Comment> replies;
    
    // 临时字段，用于API响应时包含回复数量
    @Transient
    private Integer replyCount = 0;
    
    // 临时字段，用于API响应时标记当前用户是否已点赞
    @Transient
    private Boolean isLiked = false;
} 