-- ============================================================
-- 个人主页 - 增量脚本：登录注册（2026-11-08）
-- 已有 homepage 库时直接执行本文件即可：
--   mysql -u root -p < sql/02-auth.sql
-- ============================================================

USE `homepage`;

CREATE TABLE IF NOT EXISTS `sys_user`
(
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`    VARCHAR(50) NOT NULL COMMENT '登录账号',
    `password`    VARCHAR(100) NOT NULL COMMENT 'BCrypt 加密后的密码（不是明文）',
    `nickname`    VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    `avatar`      VARCHAR(255) DEFAULT NULL COMMENT '头像',
    `role`        VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色：USER-普通用户 ADMIN-管理员',
    `status`      TINYINT     NOT NULL DEFAULT 1 COMMENT '状态：1-正常 0-禁用',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删 1-已删',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='系统用户';
