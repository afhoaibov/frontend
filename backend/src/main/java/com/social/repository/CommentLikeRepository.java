package com.social.repository;

import com.social.entity.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    
    // 根据用户ID和评论ID查找点赞记录
    Optional<CommentLike> findByUserIdAndCommentId(Long userId, Long commentId);
    
    // 检查用户是否已点赞某评论
    boolean existsByUserIdAndCommentId(Long userId, Long commentId);
    
    // 根据评论ID统计点赞数量
    @Query("SELECT COUNT(cl) FROM CommentLike cl WHERE cl.commentId = :commentId")
    Long countByCommentId(@Param("commentId") Long commentId);
    
    // 根据用户ID查询用户点赞的所有评论
    List<CommentLike> findByUserId(Long userId);
    
    // 根据评论ID查询所有点赞记录
    List<CommentLike> findByCommentId(Long commentId);
    
    // 删除用户对某评论的点赞
    @Modifying
    @Query("DELETE FROM CommentLike cl WHERE cl.userId = :userId AND cl.commentId = :commentId")
    void deleteByUserIdAndCommentId(@Param("userId") Long userId, @Param("commentId") Long commentId);
    
    // 删除评论的所有点赞记录
    @Modifying
    @Query("DELETE FROM CommentLike cl WHERE cl.commentId = :commentId")
    void deleteByCommentId(@Param("commentId") Long commentId);
    
    // 删除用户的所有点赞记录
    @Modifying
    @Query("DELETE FROM CommentLike cl WHERE cl.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);
    
    // 批量删除多个评论的点赞记录
    @Modifying
    @Query("DELETE FROM CommentLike cl WHERE cl.commentId IN :commentIds")
    void deleteByCommentIds(@Param("commentIds") List<Long> commentIds);
} 