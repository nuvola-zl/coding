package com.zhanglin.homepage.controller;

import com.zhanglin.homepage.service.ArticleService;
import com.zhanglin.homepage.vo.ArticleVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 文章对外页面：列表 + 详情（只展示已发布的）
 *
 * @author 张林
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/articles")
public class ArticleController extends BasePageController {

    private static final int PAGE_SIZE = 8;

    private final ArticleService articleService;

    @GetMapping
    public String list(@RequestParam(value = "page", defaultValue = "1") long page,
                       @RequestParam(value = "tag", required = false) String tag,
                       Model model) {
        model.addAttribute("result", articleService.pagePublished(page, PAGE_SIZE, tag));
        model.addAttribute("tags", articleService.listPublishedTags());
        model.addAttribute("activeTag", tag);
        return "articles";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, HttpServletResponse response) {
        ArticleVO article = articleService.getPublishedDetail(id);
        if (article == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "error/404";
        }
        model.addAttribute("article", article);
        return "article";
    }

}
