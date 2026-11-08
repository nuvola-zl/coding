package com.zhanglin.homepage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhanglin.homepage.common.PageResult;
import com.zhanglin.homepage.common.ResultCode;
import com.zhanglin.homepage.common.enums.ContentStatus;
import com.zhanglin.homepage.common.exception.BusinessException;
import com.zhanglin.homepage.common.markdown.MarkdownRenderer;
import com.zhanglin.homepage.dto.ArticleSaveDTO;
import com.zhanglin.homepage.entity.Article;
import com.zhanglin.homepage.mapper.ArticleMapper;
import com.zhanglin.homepage.service.ArticleService;
import com.zhanglin.homepage.vo.ArticleVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 文章业务实现
 *
 * @author 张林
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    private static final int MAX_LIST_LIMIT = 20;

    /** 自动摘要的长度 */
    private static final int AUTO_SUMMARY_CHARS = 120;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 列表类查询排除正文两列。
     *
     * <p>{@code content} / {@code content_html} 是 MEDIUMTEXT，一篇长文的 HTML 可能有几十 KB。
     * 列表页一次查 10 条、每条都拖这么大一坨回来，IO 和内存都白花——这就是常说的
     * "select *" 的代价。这里用 MyBatis-Plus 的字段过滤，只查列表需要的列。</p>
     */
    private static final Predicate<TableFieldInfo> WITHOUT_BODY =
            info -> !"content".equals(info.getColumn()) && !"content_html".equals(info.getColumn());

    private final MarkdownRenderer markdownRenderer;

    @Override
    public List<ArticleVO> listLatest(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, MAX_LIST_LIMIT));
        List<Article> articles = lambdaQuery()
                .select(Article.class, WITHOUT_BODY)
                .eq(Article::getStatus, ContentStatus.PUBLISHED.name())
                .orderByDesc(Article::getPublishTime)
                .orderByDesc(Article::getId)
                .last("LIMIT " + safeLimit)
                .list();
        return articles.stream().map(this::toVO).toList();
    }

    @Override
    public PageResult<ArticleVO> pagePublished(long pageNum, long pageSize, String tag) {
        Page<Article> page = new Page<>(Math.max(1, pageNum), Math.min(Math.max(1, pageSize), 50));
        page(page, Wrappers.<Article>lambdaQuery()
                .select(Article.class, WITHOUT_BODY)
                .eq(Article::getStatus, ContentStatus.PUBLISHED.name())
                // FIND_IN_SET 精确匹配逗号分隔列表里的一项。
                // 用 like '%tag%' 会把 "SQL" 匹配到 "MySQL"，用 {0} 占位符则会被预编译成参数，不存在拼接注入
                .apply(StringUtils.hasText(tag), "FIND_IN_SET({0}, tags)", tag)
                .orderByDesc(Article::getPublishTime)
                .orderByDesc(Article::getId));
        return PageResult.of(page, this::toVO);
    }

    @Override
    public List<String> listPublishedTags() {
        List<Article> articles = lambdaQuery()
                .select(Article::getTags)
                .eq(Article::getStatus, ContentStatus.PUBLISHED.name())
                .list();

        // 表里标签是逗号串，这里在内存里拆开计数。
        // 个人站点文章量在几百以内，这点开销可以忽略；真要上规模，应该单独建 article_tag 关联表。
        Map<String, Long> counter = articles.stream()
                .flatMap(article -> splitTags(article.getTags()).stream())
                .collect(Collectors.groupingBy(tag -> tag, LinkedHashMap::new, Collectors.counting()));

        return counter.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .toList();
    }

    @Override
    public ArticleVO getPublishedDetail(Long id) {
        Article article = getById(id);
        // 草稿不能让访客猜 id 看到，统一按"不存在"处理
        if (article == null || !ContentStatus.PUBLISHED.name().equals(article.getStatus())) {
            return null;
        }

        // 兜底：手工 SQL 插入或早期数据没有 content_html，读的时候补渲染并回写一次
        if (!StringUtils.hasText(article.getContentHtml())) {
            String html = markdownRenderer.render(article.getContent());
            article.setContentHtml(html);
            lambdaUpdate()
                    .eq(Article::getId, id)
                    .set(Article::getContentHtml, html)
                    .update();
            log.info("文章正文 HTML 缺失，已补渲染: id={}", id);
        }

        baseMapper.incrementViewCount(id);
        article.setViewCount(article.getViewCount() == null ? 1 : article.getViewCount() + 1);

        ArticleVO vo = toVO(article);
        vo.setPrevArticle(findNeighbor(article, true));
        vo.setNextArticle(findNeighbor(article, false));
        return vo;
    }

    @Override
    public List<ArticleVO> listAll() {
        List<Article> articles = lambdaQuery()
                .select(Article.class, WITHOUT_BODY)
                .orderByDesc(Article::getUpdateTime)
                .orderByDesc(Article::getId)
                .list();
        return articles.stream().map(this::toVO).toList();
    }

    @Override
    public ArticleVO getForEdit(Long id) {
        Article article = getById(id);
        if (article == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "文章不存在");
        }
        return toVO(article);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveArticle(Long id, ArticleSaveDTO dto) {
        Article article;
        if (id == null) {
            article = new Article();
        } else {
            article = getById(id);
            if (article == null) {
                throw new BusinessException(ResultCode.NOT_FOUND, "文章不存在");
            }
        }

        BeanUtils.copyProperties(dto, article);
        ContentStatus status = ContentStatus.of(dto.getStatus());
        article.setStatus(status.name());
        article.setTags(normalizeTags(dto.getTags()));

        // 渲染 + 净化：存进去的就是可以直接输出的 HTML
        article.setContentHtml(markdownRenderer.render(dto.getContent()));
        article.setReadingMinutes(markdownRenderer.estimateReadingMinutes(dto.getContent()));
        if (!StringUtils.hasText(dto.getSummary())) {
            article.setSummary(markdownRenderer.autoSummary(dto.getContent(), AUTO_SUMMARY_CHARS));
        }

        // 首次发布时记录发布时间；之后再改不覆盖，这样列表的顺序不会因为改个错字就跳上去
        if (status == ContentStatus.PUBLISHED && article.getPublishTime() == null) {
            article.setPublishTime(LocalDateTime.now());
        }

        saveOrUpdate(article);
        log.info("文章已保存: id={}, title={}, status={}", article.getId(), article.getTitle(), article.getStatus());
        return article.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteArticle(Long id) {
        Article article = getById(id);
        if (article == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "文章不存在");
        }
        removeById(id);
        log.info("文章已删除: id={}, title={}", id, article.getTitle());
    }

    @Override
    public String previewHtml(String markdown) {
        return markdownRenderer.render(markdown);
    }

    // ==================== 私有方法 ====================

    /**
     * 找上一篇 / 下一篇。
     *
     * <p>排序键是 (publish_time, id) 两个字段，所以"更早"不能只写 {@code publish_time < ?}——
     * 同一秒发布的两篇会互相漏掉。要把「时间更早」和「时间相同但 id 更小」两种情况都算进来，
     * 也就是字典序比较。</p>
     */
    private ArticleVO findNeighbor(Article current, boolean earlier) {
        LocalDateTime publishTime = current.getPublishTime();
        if (publishTime == null) {
            return null;
        }

        LambdaQueryWrapper<Article> wrapper = Wrappers.<Article>lambdaQuery()
                .select(Article::getId, Article::getTitle)
                .eq(Article::getStatus, ContentStatus.PUBLISHED.name());

        if (earlier) {
            wrapper.and(w -> w.lt(Article::getPublishTime, publishTime)
                            .or(x -> x.eq(Article::getPublishTime, publishTime)
                                    .lt(Article::getId, current.getId())))
                    .orderByDesc(Article::getPublishTime)
                    .orderByDesc(Article::getId);
        } else {
            wrapper.and(w -> w.gt(Article::getPublishTime, publishTime)
                            .or(x -> x.eq(Article::getPublishTime, publishTime)
                                    .gt(Article::getId, current.getId())))
                    .orderByAsc(Article::getPublishTime)
                    .orderByAsc(Article::getId);
        }

        Article neighbor = getOne(wrapper.last("LIMIT 1"), false);
        return neighbor == null ? null : toVO(neighbor);
    }

    private String normalizeTags(String tags) {
        List<String> list = splitTags(tags);
        return list.isEmpty() ? null : String.join(",", list);
    }

    private List<String> splitTags(String tags) {
        if (!StringUtils.hasText(tags)) {
            return List.of();
        }
        return Arrays.stream(tags.split("[,，]"))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
    }

    private ArticleVO toVO(Article article) {
        ArticleVO vo = new ArticleVO();
        BeanUtils.copyProperties(article, vo);
        vo.setTagList(splitTags(article.getTags()));
        vo.setStatusLabel(ContentStatus.of(article.getStatus()).getLabel());
        if (article.getPublishTime() != null) {
            vo.setPublishTimeText(article.getPublishTime().format(DATE_FORMATTER));
        }
        if (article.getUpdateTime() != null) {
            vo.setUpdateTimeText(article.getUpdateTime().format(DATE_FORMATTER));
        }
        return vo;
    }

}
