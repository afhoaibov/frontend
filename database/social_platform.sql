/*
 Navicat Premium Dump SQL

 Source Server         : local
 Source Server Type    : MySQL
 Source Server Version : 80042 (8.0.42)
 Source Host           : localhost:3306
 Source Schema         : social_platform

 Target Server Type    : MySQL
 Target Server Version : 80042 (8.0.42)
 File Encoding         : 65001

 Date: 24/06/2025 16:15:57
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for comment_likes
-- ----------------------------
DROP TABLE IF EXISTS `comment_likes`;
CREATE TABLE `comment_likes`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `comment_id` bigint NOT NULL,
  `created_at` datetime(6) NULL DEFAULT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of comment_likes
-- ----------------------------
INSERT INTO `comment_likes` VALUES (14, 10, '2025-06-23 21:01:08.270000', 3);
INSERT INTO `comment_likes` VALUES (21, 14, '2025-06-23 22:08:40.010000', 1);
INSERT INTO `comment_likes` VALUES (22, 15, '2025-06-23 22:13:04.773000', 2);

-- ----------------------------
-- Table structure for comments
-- ----------------------------
DROP TABLE IF EXISTS `comments`;
CREATE TABLE `comments`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `post_id` bigint NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `parent_id` bigint NULL DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `like_count` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_post_id`(`post_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` DESC) USING BTREE,
  CONSTRAINT `comments_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `comments_ibfk_2` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `comments_ibfk_3` FOREIGN KEY (`parent_id`) REFERENCES `comments` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of comments
-- ----------------------------
INSERT INTO `comments` VALUES (10, 3, 21, '天气确实不错~👍', NULL, '2025-06-23 21:01:05', 1);
INSERT INTO `comments` VALUES (11, 3, 21, '今天出去打球吗？⚽，🏀，运动员', NULL, '2025-06-23 21:01:41', 0);
INSERT INTO `comments` VALUES (12, 2, 20, '222', NULL, '2025-06-23 21:03:20', 0);
INSERT INTO `comments` VALUES (13, 1, 22, '123456', NULL, '2025-06-23 22:03:09', 0);
INSERT INTO `comments` VALUES (14, 1, 23, '你好啊~~', NULL, '2025-06-23 22:08:38', 1);
INSERT INTO `comments` VALUES (15, 1, 24, '你好····', NULL, '2025-06-23 22:12:58', 1);
INSERT INTO `comments` VALUES (16, 2, 24, 'nihao~~', NULL, '2025-06-23 22:13:10', 0);
INSERT INTO `comments` VALUES (17, 12, 27, 'wdaw', NULL, '2025-06-23 23:20:54', 0);

-- ----------------------------
-- Table structure for follows
-- ----------------------------
DROP TABLE IF EXISTS `follows`;
CREATE TABLE `follows`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `follower_id` bigint NOT NULL,
  `following_id` bigint NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `unique_follow`(`follower_id` ASC, `following_id` ASC) USING BTREE,
  INDEX `idx_follower_id`(`follower_id` ASC) USING BTREE,
  INDEX `idx_following_id`(`following_id` ASC) USING BTREE,
  CONSTRAINT `follows_ibfk_1` FOREIGN KEY (`follower_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `follows_ibfk_2` FOREIGN KEY (`following_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of follows
-- ----------------------------

-- ----------------------------
-- Table structure for likes
-- ----------------------------
DROP TABLE IF EXISTS `likes`;
CREATE TABLE `likes`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `post_id` bigint NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `unique_like`(`user_id` ASC, `post_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_post_id`(`post_id` ASC) USING BTREE,
  CONSTRAINT `likes_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `likes_ibfk_2` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of likes
-- ----------------------------

-- ----------------------------
-- Table structure for messages
-- ----------------------------
DROP TABLE IF EXISTS `messages`;
CREATE TABLE `messages`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `from_user_id` bigint NULL DEFAULT NULL,
  `to_user_id` bigint NOT NULL,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `type` enum('LIKE','COMMENT','FOLLOW','SYSTEM','ACTIVITY','MAINTENANCE','OTHER') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `related_post_id` bigint NULL DEFAULT NULL,
  `is_read` tinyint(1) NULL DEFAULT 0,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `from_user_id`(`from_user_id` ASC) USING BTREE,
  INDEX `idx_to_user_id`(`to_user_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` DESC) USING BTREE,
  INDEX `idx_is_read`(`is_read` ASC) USING BTREE,
  CONSTRAINT `messages_ibfk_1` FOREIGN KEY (`from_user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `messages_ibfk_2` FOREIGN KEY (`to_user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 74 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of messages
-- ----------------------------
INSERT INTO `messages` VALUES (65, 1, 1, '实时通知', 'xxxxx', 'SYSTEM', NULL, 0, '2025-06-23 22:13:37');
INSERT INTO `messages` VALUES (66, 1, 2, '实时通知', 'xxxxx', 'SYSTEM', NULL, 0, '2025-06-23 22:13:37');
INSERT INTO `messages` VALUES (67, 1, 3, '实时通知', 'xxxxx', 'SYSTEM', NULL, 0, '2025-06-23 22:13:37');
INSERT INTO `messages` VALUES (68, 1, 4, '实时通知', 'xxxxx', 'SYSTEM', NULL, 0, '2025-06-23 22:13:38');
INSERT INTO `messages` VALUES (69, 1, 5, '实时通知', 'xxxxx', 'SYSTEM', NULL, 0, '2025-06-23 22:13:38');
INSERT INTO `messages` VALUES (70, 1, 6, '实时通知', 'xxxxx', 'SYSTEM', NULL, 0, '2025-06-23 22:13:38');
INSERT INTO `messages` VALUES (71, 1, 7, '实时通知', 'xxxxx', 'SYSTEM', NULL, 0, '2025-06-23 22:13:38');
INSERT INTO `messages` VALUES (72, 1, 8, '实时通知', 'xxxxx', 'SYSTEM', NULL, 0, '2025-06-23 22:13:38');
INSERT INTO `messages` VALUES (73, 1, 10, '实时通知', 'xxxxx', 'SYSTEM', NULL, 0, '2025-06-23 22:13:38');

-- ----------------------------
-- Table structure for posts
-- ----------------------------
DROP TABLE IF EXISTS `posts`;
CREATE TABLE `posts`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `like_count` int NULL DEFAULT 0,
  `comment_count` int NULL DEFAULT 0,
  `share_count` int NULL DEFAULT 0,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` DESC) USING BTREE,
  INDEX `idx_like_count`(`like_count` DESC) USING BTREE,
  CONSTRAINT `posts_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 28 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of posts
-- ----------------------------
INSERT INTO `posts` VALUES (3, 2, '我的动态xxxx', NULL, 3, 0, 0, '2025-06-22 22:10:04', '2025-06-23 21:16:12');
INSERT INTO `posts` VALUES (20, 3, 'hahah', NULL, 3, 1, 0, '2025-06-23 20:51:29', '2025-06-23 21:16:14');
INSERT INTO `posts` VALUES (21, 2, '今天天气不错啊~~~', NULL, 2, 2, 0, '2025-06-23 21:00:37', '2025-06-23 21:16:10');
INSERT INTO `posts` VALUES (22, 2, 'xxxxx', NULL, 0, 1, 0, '2025-06-23 21:57:54', '2025-06-23 22:03:09');
INSERT INTO `posts` VALUES (23, 2, '第一篇动态~~~', NULL, 1, 1, 0, '2025-06-23 22:08:26', '2025-06-23 22:08:38');
INSERT INTO `posts` VALUES (24, 2, 'ceshi~~~', NULL, 1, 2, 0, '2025-06-23 22:12:45', '2025-06-23 22:13:10');
INSERT INTO `posts` VALUES (25, 12, 'xxasx', NULL, 1, 0, 0, '2025-06-23 23:00:26', '2025-06-23 23:01:32');
INSERT INTO `posts` VALUES (26, 12, 'sdad', NULL, 0, 0, 0, '2025-06-23 23:12:20', '2025-06-23 23:12:20');
INSERT INTO `posts` VALUES (27, 12, '撒大大哇', NULL, 1, 1, 0, '2025-06-23 23:15:34', '2025-06-23 23:20:54');

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `bio` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `follower_count` int NULL DEFAULT 0,
  `following_count` int NULL DEFAULT 0,
  `post_count` int NULL DEFAULT 0,
  `like_count` int NULL DEFAULT 0,
  `score` int NULL DEFAULT 0,
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'USER',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `email`(`email` ASC) USING BTREE,
  INDEX `idx_username`(`username` ASC) USING BTREE,
  INDEX `idx_email`(`email` ASC) USING BTREE,
  INDEX `idx_score`(`score` DESC) USING BTREE,
  INDEX `idx_follower_count`(`follower_count` DESC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, 'admin', '123456', 'admin@example.com', '管理员', NULL, '系统管理员', 150, 50, 25, 300, 1000, 'ADMIN', '2025-06-21 11:49:55', '2025-06-21 11:50:26');
INSERT INTO `users` VALUES (2, 'user1', '123456', 'user1@example.com', '用户1', NULL, '这是用户1的个人简介', 120, 30, 37, 250, 850, 'USER', '2025-06-21 11:49:55', '2025-06-23 22:12:45');
INSERT INTO `users` VALUES (3, 'user2', '123456', 'user2@example.com', '用户2', NULL, '这是用户2的个人简介', 80, 25, 14, 180, 720, 'USER', '2025-06-21 11:49:55', '2025-06-23 20:51:29');
INSERT INTO `users` VALUES (4, 'user3', '123456', 'user3@example.com', '用户3', NULL, '这是用户3的个人简介', 60, 20, 8, 120, 650, 'USER', '2025-06-21 11:49:55', '2025-06-21 11:50:29');
INSERT INTO `users` VALUES (5, 'user4', '123456', 'user4@example.com', '用户4', NULL, '这是用户4的个人简介', 40, 15, 5, 80, 580, 'USER', '2025-06-21 11:49:55', '2025-06-21 11:50:30');
INSERT INTO `users` VALUES (6, 'test', '123456', 'test@example.com', '测试用户', NULL, '这是一个测试用户', NULL, NULL, NULL, NULL, 500, 'USER', '2025-06-21 11:49:55', '2025-06-21 11:50:30');
INSERT INTO `users` VALUES (7, 'demo', '123456', 'demo@example.com', '演示用户', NULL, '这是一个演示用户', NULL, NULL, NULL, NULL, 400, 'USER', '2025-06-21 11:49:55', '2025-06-21 11:50:32');
INSERT INTO `users` VALUES (8, 'user5', '123456', '123@qq.com', 'user5', NULL, NULL, 0, 0, 0, 0, 0, 'USER', '2025-06-23 21:59:44', '2025-06-23 21:59:44');
INSERT INTO `users` VALUES (10, 'user7', '123456', '1234@qq.com', 'user7', NULL, NULL, 0, 0, 0, 0, 0, 'USER', '2025-06-23 22:00:39', '2025-06-23 22:00:39');
INSERT INTO `users` VALUES (12, 'user9', '123456', 'xxxx@1112.com', 'user9', NULL, 'xxxxx', 0, 0, 3, 0, 0, 'USER', '2025-06-23 22:55:34', '2025-06-24 16:13:41');

SET FOREIGN_KEY_CHECKS = 1;
