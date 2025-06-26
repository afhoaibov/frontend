package com.social.repository;

import com.social.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    
    /**
     * 按创建时间倒序获取所有动态
     */
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * 按用户ID和创建时间倒序获取动态
     */
    Page<Post> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    /**
     * 获取用户的所有动态
     */
    List<Post> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    /**
     * 统计用户的动态数量
     */
    long countByUserId(Long userId);
    
    @Query("SELECT p FROM Post p ORDER BY p.createdAt DESC")
    Page<Post> findAllOrderByCreatedAtDesc(Pageable pageable);
    
    @Query("SELECT p FROM Post p ORDER BY p.likeCount DESC, p.commentCount DESC")
    Page<Post> findTopPostsByLikes(Pageable pageable);
    
    @Query("SELECT p FROM Post p WHERE p.userId IN :userIds ORDER BY p.createdAt DESC")
    Page<Post> findByUserIdsOrderByCreatedAtDesc(List<Long> userIds, Pageable pageable);
} 