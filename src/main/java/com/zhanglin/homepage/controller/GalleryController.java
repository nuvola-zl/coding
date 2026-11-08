package com.zhanglin.homepage.controller;

import com.zhanglin.homepage.common.PageResult;
import com.zhanglin.homepage.common.enums.GalleryCategory;
import com.zhanglin.homepage.service.GalleryService;
import com.zhanglin.homepage.vo.GalleryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 图片展示页（公开）
 *
 * @author 张林
 */
@Controller
@RequiredArgsConstructor
public class GalleryController extends BasePageController {

    private static final int PAGE_SIZE = 12;

    private final GalleryService galleryService;

    @GetMapping("/gallery")
    public String gallery(@RequestParam(value = "category", required = false) String category,
                          @RequestParam(value = "page", defaultValue = "1") long page,
                          Model model) {
        // 参数非法时回退到「全部」，不因为 URL 写错就报错
        GalleryCategory current = null;
        if (category != null && !category.isBlank() && !"ALL".equalsIgnoreCase(category)) {
            current = GalleryCategory.of(category);
        }

        PageResult<GalleryVO> result = galleryService.pagePublished(current, page, PAGE_SIZE);
        Map<String, Long> counts = galleryService.countPublished();

        model.addAttribute("result", result);
        model.addAttribute("images", result.getRecords());
        model.addAttribute("current", current == null ? "ALL" : current.name());
        model.addAttribute("counts", counts);
        model.addAttribute("total", result.getTotal());
        model.addAttribute("categories", GalleryCategory.values());
        return "gallery";
    }

}
