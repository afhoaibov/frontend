package com.social.repository;

import com.social.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    @Query("SELECT m FROM Message m WHERE m.toUser.id = :toUserId ORDER BY m.createdAt DESC")
    Page<Message> findByToUserIdOrderByCreatedAtDesc(@Param("toUserId") Long toUserId, Pageable pageable);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE m.toUser.id = :userId AND m.isRead = false")
    Long countUnreadMessagesByUserId(@Param("userId") Long userId);
    
    @Modifying
    @Query("UPDATE Message m SET m.isRead = true WHERE m.toUser.id = :userId")
    void markAllAsReadByUserId(@Param("userId") Long userId);
    
    /**
     * 获取所有用户ID
     */
    @Query("SELECT DISTINCT u.id FROM User u")
    List<Long> getAllUserIds();
    
    /**
     * 获取系统消息（管理员发布的通知）
     */
    @Query("SELECT m FROM Message m WHERE m.fromUser.id = 1 ORDER BY m.createdAt DESC")
    Page<Message> findSystemMessagesOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * 获取用户消息列表（带关联对象）
     */
    @Query("SELECT m FROM Message m JOIN FETCH m.toUser WHERE m.toUser.id = :toUserId ORDER BY m.createdAt DESC")
    List<Message> findByToUserIdWithUserOrderByCreatedAtDesc(@Param("toUserId") Long toUserId);
    
    /**
     * 获取系统消息列表（带关联对象）
     */
    @Query("SELECT m FROM Message m JOIN FETCH m.fromUser JOIN FETCH m.toUser WHERE m.fromUser.id = 1 ORDER BY m.createdAt DESC")
    List<Message> findSystemMessagesWithUsersOrderByCreatedAtDesc();
} 