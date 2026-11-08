package com.zhanglin.homepage.controller;

import com.zhanglin.homepage.dto.ProjectSaveDTO;
import com.zhanglin.homepage.security.LoginRequired;
import com.zhanglin.homepage.security.Role;
import com.zhanglin.homepage.service.ProjectService;
import com.zhanglin.homepage.vo.ProjectVO;
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

/**
 * 作品后台管理（仅管理员）
 *
 * @author 张林
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/projects")
@LoginRequired(minRole = Role.ADMIN)
public class AdminProjectController extends BasePageController {

    private final ProjectService projectService;

    @GetMapping
    public String list(@RequestParam(value = "saved", required = false) String saved,
                       @RequestParam(value = "deleted", required = false) String deleted,
                       Model model) {
        model.addAttribute("projects", projectService.listAll());
        model.addAttribute("saved", saved != null);
        model.addAttribute("deleted", deleted != null);
        return "admin/projects";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        ProjectSaveDTO form = new ProjectSaveDTO();
        form.setSortOrder(0);
        form.setStatus("DRAFT");
        model.addAttribute("form", form);
        model.addAttribute("formAction", "/admin/projects/new");
        model.addAttribute("editing", false);
        return "admin/project-form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("form") ProjectSaveDTO form,
                         BindingResult bindingResult,
                         Model model) {
        if (bindingResult.hasErrors()) {
            return backToForm(model, null);
        }
        projectService.saveProject(null, form);
        return "redirect:/admin/projects?saved=1";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        ProjectVO project = projectService.getForEdit(id);
        model.addAttribute("form", toForm(project));
        model.addAttribute("formAction", "/admin/projects/" + id + "/edit");
        model.addAttribute("editing", true);
        return "admin/project-form";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id,
                       @Valid @ModelAttribute("form") ProjectSaveDTO form,
                       BindingResult bindingResult,
                       Model model) {
        if (bindingResult.hasErrors()) {
            return backToForm(model, id);
        }
        projectService.saveProject(id, form);
        return "redirect:/admin/projects?saved=1";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        projectService.deleteProject(id);
        return "redirect:/admin/projects?deleted=1";
    }

    // ==================== 私有方法 ====================

    private String backToForm(Model model, Long id) {
        model.addAttribute("formAction", id == null ? "/admin/projects/new" : "/admin/projects/" + id + "/edit");
        model.addAttribute("editing", id != null);
        return "admin/project-form";
    }

    private ProjectSaveDTO toForm(ProjectVO vo) {
        ProjectSaveDTO form = new ProjectSaveDTO();
        form.setTitle(vo.getTitle());
        form.setSummary(vo.getSummary());
        form.setDescription(vo.getDescription());
        form.setCover(vo.getCover());
        form.setTechStack(vo.getTechStack());
        form.setRepoUrl(vo.getRepoUrl());
        form.setDemoUrl(vo.getDemoUrl());
        form.setPeriod(vo.getPeriod());
        form.setSortOrder(vo.getSortOrder());
        form.setStatus(vo.getStatus());
        return form;
    }

}
