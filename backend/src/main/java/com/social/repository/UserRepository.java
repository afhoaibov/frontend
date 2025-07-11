package com.social.repository;

import com.social.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u ORDER BY u.score DESC, u.followerCount DESC")
    List<User> findTopUsersByScore();
    
    @Query("SELECT u FROM User u ORDER BY u.followerCount DESC, u.score DESC")
    List<User> findTopUsersByFollowers();
} 