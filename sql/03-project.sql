-- ============================================================
-- 个人主页 - 增量脚本：作品/项目模块（2026-11-08）
-- 已有 homepage 库时直接执行：
--   mysql -u root -p < sql/03-project.sql
-- ============================================================

USE `homepage`;

CREATE TABLE IF NOT EXISTS `project`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `title`       VARCHAR(100) NOT NULL COMMENT '项目名称',
    `summary`     VARCHAR(200)          DEFAULT NULL COMMENT '一句话简介（列表页展示）',
    `description` TEXT COMMENT '详细介绍，支持换行',
    `cover`       VARCHAR(255)          DEFAULT NULL COMMENT '封面图地址，留空用占位图',
    `tech_stack`  VARCHAR(255)          DEFAULT NULL COMMENT '技术栈，逗号分隔，如 Spring Boot,Redis',
    `repo_url`    VARCHAR(255)          DEFAULT NULL COMMENT '源码仓库地址',
    `demo_url`    VARCHAR(255)          DEFAULT NULL COMMENT '在线演示地址',
    `period`      VARCHAR(50)           DEFAULT NULL COMMENT '时间区间，如 2026.03 - 2026.06',
    `sort_order`  INT          NOT NULL DEFAULT 0 COMMENT '排序，越小越靠前',
    `status`      VARCHAR(20)  NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT-草稿 PUBLISHED-已发布',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删 1-已删',
    PRIMARY KEY (`id`),
    KEY `idx_status_sort` (`status`, `sort_order`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='作品/项目';

-- 示例数据（可以直接在后台改或删）
INSERT INTO `project` (`title`, `summary`, `description`, `tech_stack`, `repo_url`, `period`, `sort_order`, `status`)
VALUES ('苍穹外卖', '前后端一体的餐饮外卖系统，做了订单状态流转和微信支付对接',
        '完整的点餐 SaaS：管理端做菜品、套餐、订单管理，用户端做微信小程序点餐。\n\n我负责的部分：订单状态机、来单提醒（WebSocket）、微信支付回调、报表统计。',
        'Spring Boot,MyBatis,Redis,MySQL,WebSocket', 'https://github.com/linwanqing', '2026.03 - 2026.06', 1, 'PUBLISHED'),
       ('黑马点评', 'Redis 实战项目：秒杀、点赞、关注、附近商户',
        '围绕 Redis 做的一整套高并发场景练习。\n\n重点练了：缓存穿透/击穿/雪崩的处理方案、Lua 脚本保证秒杀原子性、基于 GEO 的附近商户查询、基于 Redis 的feed 流关注。',
        'Spring Boot,Redis,MySQL,Lua', 'https://github.com/linwanqing', '2026.05 - 2026.07', 2, 'PUBLISHED'),
       ('个人主页', '你在看的这个站，Spring Boot + Thymeleaf 全栈练习',
        '从零搭的一套后端分层框架：统一响应、全局异常、MyBatis-Plus 分页与自动填充、JWT 登录鉴权。\n\n前端用 Thymeleaf 服务端渲染，登录态放 HttpOnly Cookie，读接口全公开、写接口需管理员。',
        'Spring Boot,MyBatis-Plus,Thymeleaf,JWT,MySQL', 'https://github.com/linwanqing', '2026.11 - 至今', 3, 'PUBLISHED');
