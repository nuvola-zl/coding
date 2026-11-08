package com.zhanglin.homepage.controller;

import com.zhanglin.homepage.common.Result;
import com.zhanglin.homepage.dto.ArticleSaveDTO;
import com.zhanglin.homepage.security.LoginRequired;
import com.zhanglin.homepage.security.Role;
import com.zhanglin.homepage.service.ArticleService;
import com.zhanglin.homepage.vo.ArticleVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 文章后台管理（仅管理员）
 *
 * @author 张林
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/articles")
@LoginRequired(minRole = Role.ADMIN)
public class AdminArticleController extends BasePageController {

    private final ArticleService articleService;

    @GetMapping
    public String list(@RequestParam(value = "saved", required = false) String saved,
                       @RequestParam(value = "deleted", required = false) String deleted,
                       Model model) {
        model.addAttribute("articles", articleService.listAll());
        model.addAttribute("saved", saved != null);
        model.addAttribute("deleted", deleted != null);
        return "admin/articles";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        ArticleSaveDTO form = new ArticleSaveDTO();
        form.setStatus("DRAFT");
        model.addAttribute("form", form);
        model.addAttribute("formAction", "/admin/articles/new");
        model.addAttribute("editing", false);
        return "admin/article-form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("form") ArticleSaveDTO form,
                         BindingResult bindingResult,
                         Model model) {
        if (bindingResult.hasErrors()) {
            return backToForm(model, null);
        }
        articleService.saveArticle(null, form);
        return "redirect:/admin/articles?saved=1";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        ArticleVO article = articleService.getForEdit(id);
        model.addAttribute("form", toForm(article));
        model.addAttribute("formAction", "/admin/articles/" + id + "/edit");
        model.addAttribute("editing", true);
        return "admin/article-form";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id,
                       @Valid @ModelAttribute("form") ArticleSaveDTO form,
                       BindingResult bindingResult,
                       Model model) {
        if (bindingResult.hasErrors()) {
            return backToForm(model, id);
        }
        articleService.saveArticle(id, form);
        return "redirect:/admin/articles?saved=1";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return "redirect:/admin/articles?deleted=1";
    }

    /**
     * 编辑器里的实时预览。
     *
     * <p>走的是和正式保存完全同一条渲染 + 净化链路，所以预览里看到的排版
     * 就是发布后的排版，不会出现"预览好好的，发出去样式全乱"。</p>
     *
     * <p>返回 JSON 而不是 HTML 片段：页面上的 fetch 拿到的是一次完整响应，
     * 出错时也能从 message 里看出原因（比如没登录）。</p>
     */
    @PostMapping("/preview")
    @ResponseBody
    public Result<String> preview(@RequestParam(value = "content", defaultValue = "") String content) {
        return Result.ok(articleService.previewHtml(content));
    }

    // ==================== 私有方法 ====================

    private String backToForm(Model model, Long id) {
        model.addAttribute("formAction", id == null ? "/admin/articles/new" : "/admin/articles/" + id + "/edit");
        model.addAttribute("editing", id != null);
        return "admin/article-form";
    }

    private ArticleSaveDTO toForm(ArticleVO vo) {
        ArticleSaveDTO form = new ArticleSaveDTO();
        form.setTitle(vo.getTitle());
        form.setSummary(vo.getSummary());
        form.setContent(vo.getContent());
        form.setCover(vo.getCover());
        form.setTags(vo.getTags());
        form.setStatus(vo.getStatus());
        return form;
    }

}
