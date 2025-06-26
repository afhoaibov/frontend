package com.social.service;

import com.social.entity.Comment;
import com.social.entity.CommentLike;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {
    
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final RankingService rankingService;
    
    /**
     * 获取帖子的评论列表（分页）
     */
    public Page<Comment> getCommentsByPostId(Long postId, int page, int size, Long currentUserId) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Comment> comments = commentRepository.findByPostIdAndParentIdIsNullOrderByCreatedAtDesc(postId, pageable);
        
        // 为每个评论添加用户信息和回复信息
        comments.getContent().forEach(comment -> enrichComment(comment, currentUserId));
        
        return comments;
    }
    
    /**
     * 创建评论
     */
    @Transactional
    public Comment createComment(Long postId, Long userId, String content, Long parentId) {
        // 验证帖子是否存在
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));
        
        // 验证用户是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 如果是回复评论，验证父评论是否存在
        if (parentId != null) {
            Comment parentComment = commentRepository.findById(parentId)
                    .orElseThrow(() -> new RuntimeException("父评论不存在"));
            
            // 确保父评论属于同一个帖子
            if (!parentComment.getPostId().equals(postId)) {
                throw new RuntimeException("父评论不属于该帖子");
            }
        }
        
        // 创建评论
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setParentId(parentId);
        comment.setLikeCount(0);
        
        Comment savedComment = commentRepository.save(comment);
        
        // 更新帖子的评论数量
        if (parentId == null) {
            post.setCommentCount(post.getCommentCount() + 1);
            postRepository.save(post);
            
            // 实时更新排行榜
            rankingService.updateUserPostComments(post.getUserId());
            rankingService.updateUserCompositeScore(post.getUserId());
        }
        
        // 添加用户信息
        enrichComment(savedComment, userId);
        
        return savedComment;
    }
    
    /**
     * 删除评论
     */
    @Transactional
    public void deleteComment(Long postId, Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在"));
        
        // 验证评论是否属于该帖子
        if (!comment.getPostId().equals(postId)) {
            throw new RuntimeException("评论不属于该帖子");
        }
        
        // 验证用户是否有权限删除（评论作者或管理员）
        if (!comment.getUserId().equals(userId)) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            if (!"ADMIN".equals(user.getRole())) {
                throw new RuntimeException("没有权限删除此评论");
            }
        }
        
        // 删除评论的所有点赞记录
        commentLikeRepository.deleteByCommentId(commentId);
        
        // 删除评论及其所有回复
        deleteCommentAndReplies(commentId);
        
        // 更新帖子的评论数量
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));
        
        // 计算需要减少的评论数量
        long deletedCount = countCommentAndReplies(commentId);
        post.setCommentCount(Math.max(0, post.getCommentCount() - (int) deletedCount));
        postRepository.save(post);
        
        // 实时更新排行榜
        rankingService.updateUserPostComments(post.getUserId());
        rankingService.updateUserCompositeScore(post.getUserId());
    }
    
    /**
     * 点赞/取消点赞评论
     */
    @Transactional
    public Comment toggleCommentLike(Long postId, Long commentId, Long userId) {
        // 验证评论是否存在
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在"));
        
        // 验证评论是否属于该帖子
        if (!comment.getPostId().equals(postId)) {
            throw new RuntimeException("评论不属于该帖子");
        }
        
        // 验证用户是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 检查是否已经点赞
        boolean alreadyLiked = commentLikeRepository.existsByUserIdAndCommentId(userId, commentId);
        
        if (alreadyLiked) {
            // 如果已经点赞，则取消点赞
            commentLikeRepository.deleteByUserIdAndCommentId(userId, commentId);
            comment.setLikeCount(Math.max(0, comment.getLikeCount() - 1));
        } else {
            // 如果没有点赞，则添加点赞
            CommentLike commentLike = new CommentLike();
            commentLike.setUserId(userId);
            commentLike.setCommentId(commentId);
            commentLikeRepository.save(commentLike);
            comment.setLikeCount(comment.getLikeCount() + 1);
        }
        
        Comment savedComment = commentRepository.save(comment);
        
        // 添加用户信息和点赞状态
        enrichComment(savedComment, userId);
        
        return savedComment;
    }
    
    /**
     * 获取评论的回复列表
     */
    public List<Comment> getCommentReplies(Long commentId, Long currentUserId) {
        List<Comment> replies = commentRepository.findByParentIdOrderByCreatedAtAsc(commentId);
        replies.forEach(comment -> enrichComment(comment, currentUserId));
        return replies;
    }
    
    /**
     * 丰富评论信息（添加用户信息和回复信息）
     */
    private void enrichComment(Comment comment, Long currentUserId) {
        // 添加用户信息
        User user = userRepository.findById(comment.getUserId()).orElse(null);
        if (user != null) {
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("nickname", user.getNickname());
            userInfo.put("avatar", user.getAvatar());
            comment.setUserInfo(userInfo);
        }
        
        // 添加回复数量
        Long replyCount = commentRepository.countByParentId(comment.getId());
        comment.setReplyCount(replyCount.intValue());
        
        // 添加点赞数量（从数据库重新获取）
        Long likeCount = commentLikeRepository.countByCommentId(comment.getId());
        comment.setLikeCount(likeCount.intValue());
        
        // 添加当前用户是否已点赞
        if (currentUserId != null) {
            boolean isLiked = commentLikeRepository.existsByUserIdAndCommentId(currentUserId, comment.getId());
            comment.setIsLiked(isLiked);
        }
        
        // 如果是顶级评论，添加前几条回复
        if (comment.getParentId() == null) {
            List<Comment> replies = commentRepository.findByParentIdOrderByCreatedAtAsc(comment.getId());
            // 只取前3条回复
            List<Comment> limitedReplies = replies.stream()
                    .limit(3)
                    .peek(reply -> enrichComment(reply, currentUserId))
                    .collect(Collectors.toList());
            comment.setReplies(limitedReplies);
        }
    }
    
    /**
     * 递归删除评论及其所有回复
     */
    private void deleteCommentAndReplies(Long commentId) {
        List<Comment> replies = commentRepository.findByParentIdOrderByCreatedAtAsc(commentId);
        for (Comment reply : replies) {
            deleteCommentAndReplies(reply.getId());
        }
        commentRepository.deleteById(commentId);
    }
    
    /**
     * 递归计算评论及其回复的数量
     */
    private long countCommentAndReplies(Long commentId) {
        long count = 1; // 当前评论
        List<Comment> replies = commentRepository.findByParentIdOrderByCreatedAtAsc(commentId);
        for (Comment reply : replies) {
            count += countCommentAndReplies(reply.getId());
        }
        return count;
    }
} 