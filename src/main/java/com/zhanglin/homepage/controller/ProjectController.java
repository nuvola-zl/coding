package com.zhanglin.homepage.controller;

import com.zhanglin.homepage.common.PageResult;
import com.zhanglin.homepage.service.GalleryService;
import com.zhanglin.homepage.service.ProjectService;
import com.zhanglin.homepage.vo.ProjectVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 作品对外页面：列表 + 详情（只展示已发布的）
 *
 * @author 张林
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/projects")
public class ProjectController extends BasePageController {

    private static final int PAGE_SIZE = 6;

    private final ProjectService projectService;
    private final GalleryService galleryService;

    @GetMapping
    public String list(@RequestParam(value = "page", defaultValue = "1") long page, Model model) {
        PageResult<ProjectVO> result = projectService.pagePublished(page, PAGE_SIZE);
        model.addAttribute("result", result);
        return "projects";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, HttpServletResponse response) {
        ProjectVO project = projectService.getPublishedDetail(id);
        if (project == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "error/404";
        }
        model.addAttribute("project", project);
        // 关联的项目截图：后台给图片选「关联作品」后就会挂到这里
        model.addAttribute("images", galleryService.listPublishedByProject(id));
        return "project";
    }

}
