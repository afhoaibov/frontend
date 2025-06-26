package com.social.service;

import com.social.entity.Post;
import com.social.entity.User;
import com.social.repository.CommentLikeRepository;
import com.social.repository.CommentRepository;
import com.social.repository.PostRepository;
import com.social.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PostService {
    
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final RankingService rankingService;
    
    /**
     * 创建动态
     */
    @Transactional
    public Post createPost(Long userId, String content) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        Post post = new Post();
        post.setUserId(userId);
        post.setContent(content);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setShareCount(0);
        
        Post savedPost = postRepository.save(post);
        
        // 更新用户的动态数量
        user.setPostCount(user.getPostCount() + 1);
        userRepository.save(user);
        
        // 实时更新排行榜
        rankingService.updateUserPostCount(userId);
        rankingService.updateUserCompositeScore(userId);
        
        return savedPost;
    }
    
    /**
     * 删除动态
     */
    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("动态不存在"));
        
        // 验证用户是否有权限删除（动态作者或管理员）
        if (!post.getUserId().equals(userId)) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            if (!"ADMIN".equals(user.getRole())) {
                throw new RuntimeException("没有权限删除此动态");
            }
        }
        
        Long postAuthorId = post.getUserId();
        
        // 1. 删除动态的所有评论点赞记录
        List<Long> commentIds = commentRepository.findCommentIdsByPostId(postId);
        if (!commentIds.isEmpty()) {
            commentLikeRepository.deleteByCommentIds(commentIds);
        }
        
        // 2. 删除动态的所有评论
        commentRepository.deleteByPostId(postId);
        
        // 3. 删除动态的点赞记录（如果有likes表的话）
        // 这里假设有PostLikeRepository，如果没有可以跳过
        // postLikeRepository.deleteByPostId(postId);
        
        // 4. 删除动态本身
        postRepository.deleteById(postId);
        
        // 5. 更新用户的动态数量
        User postAuthor = userRepository.findById(postAuthorId).orElse(null);
        if (postAuthor != null && postAuthor.getPostCount() > 0) {
            postAuthor.setPostCount(postAuthor.getPostCount() - 1);
            userRepository.save(postAuthor);
        }
        
        // 实时更新排行榜
        rankingService.updateUserPostCount(postAuthorId);
        rankingService.updateUserPostLikes(postAuthorId);
        rankingService.updateUserPostComments(postAuthorId);
        rankingService.updateUserCompositeScore(postAuthorId);
    }
    
    /**
     * 点赞动态
     */
    @Transactional
    public void likePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("动态不存在"));
        
        // 增加点赞数
        post.setLikeCount(post.getLikeCount() + 1);
        postRepository.save(post);
        
        // 实时更新排行榜
        rankingService.updateUserPostLikes(post.getUserId());
        rankingService.updateUserCompositeScore(post.getUserId());
    }
    
    /**
     * 取消点赞动态
     */
    @Transactional
    public void unlikePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("动态不存在"));
        
        // 减少点赞数
        if (post.getLikeCount() > 0) {
            post.setLikeCount(post.getLikeCount() - 1);
            postRepository.save(post);
            
            // 实时更新排行榜
            rankingService.updateUserPostLikes(post.getUserId());
            rankingService.updateUserCompositeScore(post.getUserId());
        }
    }
    
    /**
     * 添加评论（更新动态评论数）
     */
    @Transactional
    public void addCommentToPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("动态不存在"));
        
        // 增加评论数
        post.setCommentCount(post.getCommentCount() + 1);
        postRepository.save(post);
        
        // 实时更新排行榜
        rankingService.updateUserPostComments(post.getUserId());
        rankingService.updateUserCompositeScore(post.getUserId());
    }
    
    /**
     * 删除评论（更新动态评论数）
     */
    @Transactional
    public void removeCommentFromPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("动态不存在"));
        
        // 减少评论数
        if (post.getCommentCount() > 0) {
            post.setCommentCount(post.getCommentCount() - 1);
            postRepository.save(post);
            
            // 实时更新排行榜
            rankingService.updateUserPostComments(post.getUserId());
            rankingService.updateUserCompositeScore(post.getUserId());
        }
    }
    
    /**
     * 获取动态列表
     */
    public Page<Post> getPosts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postRepository.findAllByOrderByCreatedAtDesc(pageable);
        
        // 为每个动态添加用户信息
        posts.getContent().forEach(this::enrichPost);
        
        return posts;
    }
    
    /**
     * 获取用户的动态列表
     */
    public Page<Post> getUserPosts(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        
        // 为每个动态添加用户信息
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("nickname", user.getNickname());
            userInfo.put("avatar", user.getAvatar());
            userInfo.put("bio", user.getBio());
            
            posts.getContent().forEach(post -> {
                post.setUserInfo(userInfo);
            });
        }
        
        return posts;
    }
    
    /**
     * 获取单个动态详情
     */
    public Post getPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("动态不存在"));
        
        enrichPost(post);
        
        return post;
    }
    
    /**
     * 丰富动态信息（添加用户信息）
     */
    private void enrichPost(Post post) {
        User user = userRepository.findById(post.getUserId()).orElse(null);
        if (user != null) {
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("nickname", user.getNickname());
            userInfo.put("avatar", user.getAvatar());
            userInfo.put("bio", user.getBio());
            post.setUserInfo(userInfo);
        }
    }
} 