-- ============================================================
-- 个人主页 - 增量脚本：文章 / 笔记模块（2026-11-08）
-- 已有 homepage 库时直接执行（Windows 下务必带 --default-character-set=utf8mb4，
-- 否则中文会按 GBK/latin1 解析，报 Data too long for column 或直接乱码）：
--   mysql -u root -p < sql/05-article.sql
-- ============================================================

USE `homepage`;

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
