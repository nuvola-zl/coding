package com.zhanglin.homepage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhanglin.homepage.common.PageResult;
import com.zhanglin.homepage.dto.ArticleSaveDTO;
import com.zhanglin.homepage.entity.Article;
import com.zhanglin.homepage.vo.ArticleVO;

import java.util.List;

/**
 * 文章业务接口
 *
 * @author 张林
 */
public interface ArticleService extends IService<Article> {

    /**
     * 主页 / 侧栏用：取最近发布的 N 篇（不含正文，省流量）
     */
    List<ArticleVO> listLatest(int limit);

    /**
     * 文章列表页：已发布文章分页
     *
     * @param tag 可选，按标签筛选；为空表示全部
     */
    PageResult<ArticleVO> pagePublished(long pageNum, long pageSize, String tag);

    /**
     * 已发布文章里出现过的所有标签，按出现次数从多到少
     */
    List<String> listPublishedTags();

    /**
     * 详情页：只返回已发布的，草稿或不存在都返回 null
     * （顺带把阅读量 +1，并带上前后篇）
     */
    ArticleVO getPublishedDetail(Long id);

    /**
     * 后台列表：包含草稿（不含正文）
     */
    List<ArticleVO> listAll();

    /**
     * 后台编辑页：取原始 Markdown 正文
     */
    ArticleVO getForEdit(Long id);

    /**
     * 新增或修改，返回主键
     *
     * @param id  null 表示新增
     * @param dto 表单数据
     */
    Long saveArticle(Long id, ArticleSaveDTO dto);

    /**
     * 逻辑删除
     */
    void deleteArticle(Long id);

    /**
     * 后台编辑器里的实时预览：只渲染不落库
     */
    String previewHtml(String markdown);

}
