package com.zhanglin.homepage.controller;

import com.zhanglin.homepage.common.enums.GalleryCategory;
import com.zhanglin.homepage.dto.GallerySaveDTO;
import com.zhanglin.homepage.security.LoginRequired;
import com.zhanglin.homepage.security.Role;
import com.zhanglin.homepage.service.GalleryService;
import com.zhanglin.homepage.service.ProjectService;
import com.zhanglin.homepage.service.UploadService;
import com.zhanglin.homepage.vo.GalleryVO;
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
import org.springframework.web.multipart.MultipartFile;

/**
 * 图库后台管理（仅管理员）
 *
 * @author 张林
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/gallery")
@LoginRequired(minRole = Role.ADMIN)
public class AdminGalleryController extends BasePageController {

    private final GalleryService galleryService;
    private final ProjectService projectService;
    private final UploadService uploadService;

    @GetMapping
    public String list(@RequestParam(value = "category", required = false) String category,
                       @RequestParam(value = "saved", required = false) String saved,
                       @RequestParam(value = "deleted", required = false) String deleted,
                       Model model) {
        GalleryCategory current = null;
        if (category != null && !category.isBlank() && !"ALL".equalsIgnoreCase(category)) {
            current = GalleryCategory.of(category);
        }
        model.addAttribute("images", galleryService.listAll(current));
        model.addAttribute("current", current == null ? "ALL" : current.name());
        model.addAttribute("counts", galleryService.countPublished());
        model.addAttribute("categories", GalleryCategory.values());
        model.addAttribute("storageType", uploadService.storageType());
        model.addAttribute("saved", saved != null);
        model.addAttribute("deleted", deleted != null);
        return "admin/gallery";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        GallerySaveDTO form = new GallerySaveDTO();
        form.setCategory(GalleryCategory.PERSONAL.name());
        form.setStatus("PUBLISHED");
        form.setSortOrder(0);
        model.addAttribute("form", form);
        model.addAttribute("formAction", "/admin/gallery/new");
        model.addAttribute("editing", false);
        fillOptions(model);
        return "admin/gallery-form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("form") GallerySaveDTO form,
                         BindingResult bindingResult,
                         @RequestParam(value = "file", required = false) MultipartFile file,
                         Model model) {
        if (bindingResult.hasErrors()) {
            return backToForm(model, null, null, bindingResult);
        }
        if (file == null || file.isEmpty()) {
            // 页面表单里这是必填项，用 bindingResult 报错可以复用模板上的错误提示样式
            bindingResult.rejectValue("title", "file.required", "请选择要上传的图片");
            return backToForm(model, null, null, bindingResult);
        }
        galleryService.create(file, form);
        return "redirect:/admin/gallery?saved=1";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        GalleryVO image = galleryService.getForEdit(id);
        model.addAttribute("form", toForm(image));
        model.addAttribute("formAction", "/admin/gallery/" + id + "/edit");
        model.addAttribute("editing", true);
        model.addAttribute("previewUrl", image.getUrl());
        fillOptions(model);
        return "admin/gallery-form";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id,
                       @Valid @ModelAttribute("form") GallerySaveDTO form,
                       BindingResult bindingResult,
                       @RequestParam(value = "file", required = false) MultipartFile file,
                       Model model) {
        if (bindingResult.hasErrors()) {
            return backToForm(model, id, galleryService.getForEdit(id).getUrl(), bindingResult);
        }
        galleryService.update(id, file, form);
        return "redirect:/admin/gallery?saved=1";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        galleryService.deleteGallery(id);
        return "redirect:/admin/gallery?deleted=1";
    }

    // ==================== 私有方法 ====================

    private void fillOptions(Model model) {
        // 项目图片可以关联到某个作品，下拉框数据来自作品表
        model.addAttribute("projects", projectService.listAll());
        model.addAttribute("categories", GalleryCategory.values());
        model.addAttribute("storageType", uploadService.storageType());
    }

    private String backToForm(Model model, Long id, String previewUrl, BindingResult bindingResult) {
        model.addAttribute("formAction", id == null ? "/admin/gallery/new" : "/admin/gallery/" + id + "/edit");
        model.addAttribute("editing", id != null);
        model.addAttribute("previewUrl", previewUrl);
        // 文件选择的错误是手动 reject 到 title 上的，模板里单独展示更清楚
        model.addAttribute("fileError", bindingResult.getFieldError("title") != null
                && "file.required".equals(bindingResult.getFieldError("title").getCode())
                ? "请选择要上传的图片" : null);
        fillOptions(model);
        return "admin/gallery-form";
    }

    private GallerySaveDTO toForm(GalleryVO vo) {
        GallerySaveDTO form = new GallerySaveDTO();
        form.setTitle(vo.getTitle());
        form.setDescription(vo.getDescription());
        form.setCategory(vo.getCategory());
        form.setProjectId(vo.getProjectId());
        form.setSortOrder(vo.getSortOrder());
        form.setStatus(vo.getStatus());
        return form;
    }

}
