-- ============================================================
-- 图片展示（图库）
-- 分类：PERSONAL 个人喜欢 / PROJECT 项目图片
-- ============================================================
USE `homepage`;

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
