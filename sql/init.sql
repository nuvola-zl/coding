-- ============================================================
-- 个人主页 - 初始化脚本（全量 schema，空库直接跑这一个文件）
-- 用法: mysql -u root -p < sql/init.sql
--
-- 注意：sql/02 ~ 05 是给「已经有库」的环境用的增量脚本，内容和本文件的对应段落一致。
-- 改表结构时两边都要改，否则新老环境会不一致 —— 这是手工管库的固有代价，
-- 后面接 Flyway 就能消掉（见 README「待办」）。
-- ============================================================

CREATE DATABASE IF NOT EXISTS `homepage`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE `homepage`;

-- ------------------------------------------------------------
-- 个人信息表（骨架阶段仅此一张表，后续模块按需扩展）
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `profile`;
CREATE TABLE `profile`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `nickname`    VARCHAR(50)  NOT NULL COMMENT '昵称',
    `avatar`      VARCHAR(255)          DEFAULT NULL COMMENT '头像地址',
    `headline`    VARCHAR(120)          DEFAULT NULL COMMENT '一句话简介',
    `bio`         TEXT COMMENT '详细介绍',
    `email`       VARCHAR(100)          DEFAULT NULL COMMENT '邮箱',
    `github`      VARCHAR(255)          DEFAULT NULL COMMENT 'GitHub 主页',
    `location`    VARCHAR(100)          DEFAULT NULL COMMENT '所在城市',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删 1-已删',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='个人信息';

-- 初始化一条数据，方便接口直接有返回
INSERT INTO `profile` (`nickname`, `avatar`, `headline`, `bio`, `email`, `github`, `location`)
VALUES ('张林', NULL, 'Java 全栈练习中', '这里以后写自我介绍。', 'zhanglin@example.com',
        'https://github.com/', '杭州');

-- ------------------------------------------------------------
-- 系统用户表（登录注册）
-- 说明：不预置账号，第一个通过 /api/auth/register 注册的账号自动成为 ADMIN
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`
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

-- ------------------------------------------------------------
-- 作品/项目表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `project`;
CREATE TABLE `project`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `title`       VARCHAR(100) NOT NULL COMMENT '项目名称',
    `summary`     VARCHAR(200)          DEFAULT NULL COMMENT '一句话简介（列表页展示）',
    `description` TEXT COMMENT '详细介绍，支持换行',
    `cover`       VARCHAR(255)          DEFAULT NULL COMMENT '封面图地址，留空用占位图',
    `tech_stack`  VARCHAR(255)          DEFAULT NULL COMMENT '技术栈，逗号分隔',
    `repo_url`    VARCHAR(255)          DEFAULT NULL COMMENT '源码仓库地址',
    `demo_url`    VARCHAR(255)          DEFAULT NULL COMMENT '在线演示地址',
    `period`      VARCHAR(50)           DEFAULT NULL COMMENT '时间区间',
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

-- ------------------------------------------------------------
-- 图片展示表（同一份 DDL 的增量版本见 sql/04-gallery.sql）
-- ------------------------------------------------------------

CREATE TABLE IF NOT EXISTS `gallery`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `title`       VARCHAR(100) NOT NULL COMMENT '图片标题',
    `description` VARCHAR(500)          DEFAULT NULL COMMENT '图片说明',
    `url`         VARCHAR(500) NOT NULL COMMENT '图片地址（OSS 完整地址或本地 /uploads/xxx）',
    `category`    VARCHAR(20)  NOT NULL DEFAULT 'PERSONAL' COMMENT '分类：PERSONAL 个人喜欢 / PROJECT 项目图片',
    `project_id`  BIGINT                DEFAULT NULL COMMENT '可选：关联的作品 id（项目图片用）',
    `sort_order`  INT          NOT NULL DEFAULT 0 COMMENT '排序，越小越靠前',
    `status`      VARCHAR(20)  NOT NULL DEFAULT 'PUBLISHED' COMMENT '状态：DRAFT 草稿 / PUBLISHED 已发布',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常 / 1 已删除',
    `create_time` DATETIME              DEFAULT NULL COMMENT '创建时间',
    `update_time` DATETIME              DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_category_status` (`category`, `status`, `sort_order`),
    KEY `idx_project` (`project_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='图片展示';

-- ------------------------------------------------------------
-- 文章 / 笔记表（同一份 DDL 的增量版本见 sql/05-article.sql）
-- ------------------------------------------------------------

CREATE TABLE IF NOT EXISTS `article`
(
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `title`           VARCHAR(150) NOT NULL COMMENT '标题',
    `summary`         VARCHAR(300)          DEFAULT NULL COMMENT '摘要，留空则自动从正文截取',
    `content`         MEDIUMTEXT   NOT NULL COMMENT '正文，Markdown 原文（编辑时改的就是这份）',
    `content_html`    MEDIUMTEXT COMMENT '渲染并净化后的 HTML，保存时生成，详情页直接输出',
    `cover`           VARCHAR(255)          DEFAULT NULL COMMENT '封面图地址',
    `tags`            VARCHAR(255)          DEFAULT NULL COMMENT '标签，英文逗号分隔',
    `status`          VARCHAR(20)  NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT 草稿 / PUBLISHED 已发布',
    `view_count`      INT          NOT NULL DEFAULT 0 COMMENT '阅读量',
    `reading_minutes` INT          NOT NULL DEFAULT 1 COMMENT '预计阅读分钟数，保存时按字数估算',
    `publish_time`    DATETIME              DEFAULT NULL COMMENT '首次发布时间，草稿为空',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删 1-已删',
    PRIMARY KEY (`id`),
    -- 列表页固定是「status = PUBLISHED 按发布时间倒序」，做成联合索引可以直接走索引排序
    KEY `idx_status_publish` (`status`, `publish_time`),
    KEY `idx_update_time` (`update_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='文章 / 笔记';

-- 示例文章：直接在后台改或删
INSERT INTO `article` (`title`, `summary`, `content`, `tags`, `status`, `reading_minutes`, `publish_time`)
VALUES ('为什么我把密钥从 application.yml 里挪出去了',
        '一个公开仓库最容易被忽略的泄露点，往往不是代码，而是配置文件。',
        '# 起因\n\n项目推到 GitHub 的第二天，我盯着 `application.yml` 看了一会儿，发现 OSS 的 AccessKey 就明晃晃地躺在里面。\n\n虽然是练手项目，但这份代码是公开的。任何一个人 clone 下来，都能拿着这对 key 去刷我的 OSS 账单。\n\n## 我试过的三种做法\n\n| 做法 | 优点 | 问题 |\n| --- | --- | --- |\n| 直接写在 yml 里 | 最简单 | 推上去就泄露了 |\n| 全部用环境变量 | 不落盘，最干净 | 本地每次开 IDEA 都要配一遍，很烦 |\n| 拆一个 local 文件并 gitignore | 本地零配置，线上走环境变量 | 换机器要记得复制模板 |\n\n最后选了第三种。核心是这一行：\n\n```yaml\nspring:\n  config:\n    import:\n      - optional:file:./config/application-local.yml\n```\n\n`optional:` 前缀很关键——文件不存在时程序照常启动，只是回退到本地磁盘存储，不会一上来就报错。\n\n## 踩过的坑\n\n> imported 的文件优先级**低于**导入它的文件。\n\n也就是说，如果 `application-dev.yml` 里还留着一行 `password: ${DB_PASSWORD:}`，它会用空字符串把本地文件里的真实密码覆盖掉。这个坑我调了半小时，表现是「密码明明填了，但就是连不上」。\n\n**结论**：占位符和真实值不要同时存在两个文件里，谁覆盖谁记不住，不如只留一处。',
        'Spring Boot,安全,配置管理', 'PUBLISHED', 3, NOW()),
       ('MyBatis-Plus 逻辑删除：那些文档里没写清楚的细节',
        '逻辑删除不是加个注解就完事，查询、唯一索引、连表都要跟着改。',
        '# 加注解只是开始\n\n在字段上打 `@TableLogic`，MyBatis-Plus 就会把 `delete` 改写成 `update ... set deleted = 1`，把查询自动追加 `deleted = 0`。看起来很省事，但有几个地方它管不到。\n\n## 三个容易翻车的地方\n\n1. **唯一索引会和逻辑删除打架**。`username` 上有唯一索引，删掉一个用户后这个用户名就再也注册不了了，因为那条 `deleted = 1` 的记录还在。\n2. **手写 SQL 不会自动加条件**。XML 里自己写的 `select` 必须手动写 `and deleted = 0`，忘了就会把已删除的数据查出来。\n3. **统计类查询要留意**。`count(*)` 走的是 MyBatis-Plus 生成的语句，没问题；但自己写的报表 SQL 要自己加。\n\n## 我的处理方式\n\n```java\n// 唯一索引改成 (username, deleted) 联合唯一\n// 删除时把 deleted 置为 id 而不是 1，就能重复删除同名记录\nALTER TABLE sys_user DROP INDEX uk_username;\nALTER TABLE sys_user ADD UNIQUE KEY uk_username (username, deleted);\n```\n\n目前这个站只有我一个账号，还没走到这一步，但记下来免得以后忘。',
        'MyBatis-Plus,MySQL,踩坑', 'PUBLISHED', 4, NOW());

INSERT INTO `article` (`title`, `summary`, `content`, `tags`, `status`, `reading_minutes`)
VALUES ('待写：Thymeleaf 片段复用笔记',
        '草稿示例，随便写点东西证明草稿不会出现在前台。',
        '# 提纲\n\n- `th:fragment` 和 `th:replace` 的区别\n- 片段传参\n- 为什么不要在片段里塞业务逻辑\n',
        'Thymeleaf,前端', 'DRAFT', 1);
