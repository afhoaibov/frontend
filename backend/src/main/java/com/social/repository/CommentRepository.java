package com.social.repository;

import com.social.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    // 根据帖子ID分页查询评论（只查询顶级评论，parent_id为null）
    Page<Comment> findByPostIdAndParentIdIsNullOrderByCreatedAtDesc(Long postId, Pageable pageable);
    
    // 根据帖子ID查询所有评论
    List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);
    
    // 根据父评论ID查询子评论
    List<Comment> findByParentIdOrderByCreatedAtAsc(Long parentId);
    
    // 根据用户ID查询评论
    Page<Comment> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    // 根据帖子ID统计评论数量
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.postId = :postId")
    Long countByPostId(@Param("postId") Long postId);
    
    // 根据父评论ID统计回复数量
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.parentId = :parentId")
    Long countByParentId(@Param("parentId") Long parentId);
    
    // 删除帖子的所有评论
    @Modifying
    @Query("DELETE FROM Comment c WHERE c.postId = :postId")
    void deleteByPostId(@Param("postId") Long postId);
    
    // 删除用户的所有评论
    @Modifying
    @Query("DELETE FROM Comment c WHERE c.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);
    
    // 根据帖子ID查询所有评论ID
    @Query("SELECT c.id FROM Comment c WHERE c.postId = :postId")
    List<Long> findCommentIdsByPostId(@Param("postId") Long postId);
} 