package com.social.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @JsonIgnore
    @Column(nullable = false)
    private String password;
    
    @JsonIgnore
    @Column(unique = true)
    private String email;
    
    private String nickname;
    
    private String avatar;
    
    private String bio;
    
    @Column(name = "follower_count")
    private Integer followerCount = 0;
    
    @Column(name = "following_count")
    private Integer followingCount = 0;
    
    @Column(name = "post_count")
    private Integer postCount = 0;
    
    @Column(name = "like_count")
    private Integer likeCount = 0;
    
    @Column(name = "score")
    private Integer score = 0;
    
    @Column(name = "role")
    private String role = "USER";
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
} 