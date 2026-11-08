package com.zhanglin.homepage.controller;

import com.zhanglin.homepage.common.exception.BusinessException;
import com.zhanglin.homepage.dto.LoginDTO;
import com.zhanglin.homepage.dto.ProfileUpdateDTO;
import com.zhanglin.homepage.security.LoginRequired;
import com.zhanglin.homepage.security.Role;
import com.zhanglin.homepage.security.TokenCookieUtils;
import com.zhanglin.homepage.security.UserContext;
import com.zhanglin.homepage.service.AuthService;
import com.zhanglin.homepage.service.GalleryService;
import com.zhanglin.homepage.service.ProfileService;
import com.zhanglin.homepage.service.ProjectService;
import com.zhanglin.homepage.service.UploadService;
import com.zhanglin.homepage.vo.LoginVO;
import com.zhanglin.homepage.vo.ProfileVO;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

/**
 * 页面控制器（Thymeleaf 服务端渲染，前后端不分离）
 *
 * <p>页面直接调 Service，不绕 HTTP 接口，少一层序列化开销；
 * 接口层（/api/**）保留下来，以后要拆前后端分离时直接复用。</p>
 *
 * @author 张林
 */
@Controller
@RequiredArgsConstructor
public class PageController extends BasePageController {

    private final ProfileService profileService;
    private final ProjectService projectService;
    private final GalleryService galleryService;
    private final UploadService uploadService;
    private final AuthService authService;
    private final TokenCookieUtils tokenCookieUtils;

    // ==================== 主页面 ====================

    @GetMapping("/")
    public String index(Model model) {
        ProfileVO profile = profileService.getProfileOrNull();
        String displayName = (profile != null && StringUtils.hasText(profile.getNickname()))
                ? profile.getNickname() : "未命名";

        model.addAttribute("profile", profile);
        model.addAttribute("displayName", displayName);
        model.addAttribute("initial", firstChar(displayName));
        model.addAttribute("year", LocalDate.now().getYear());
        // 主页只展示前 6 个已发布作品，更多去 /projects
        model.addAttribute("projects", projectService.listPublished(6));
        // 图库取最近 8 张，按「个人喜欢」优先展示
        model.addAttribute("images", galleryService.listPublished(null, 8));
        return "index";
    }

    // ==================== 登录 ====================

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "redirect", required = false) String redirect, Model model) {
        // 已经登录了就不用再看登录页
        if (UserContext.get() != null) {
            return "redirect:/";
        }
        model.addAttribute("form", new LoginDTO());
        model.addAttribute("redirect", redirect);
        // 从编辑页被踢过来的，给个提示，让用户知道为什么突然要登录
        model.addAttribute("needLogin", StringUtils.hasText(redirect));
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@Valid @ModelAttribute("form") LoginDTO form,
                          BindingResult bindingResult,
                          @RequestParam(value = "redirect", required = false) String redirect,
                          Model model,
                          HttpServletResponse response) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("redirect", redirect);
            model.addAttribute("needLogin", StringUtils.hasText(redirect));
            return "login";
        }

        try {
            LoginVO loginVO = authService.login(form);
            // token 放进 HttpOnly Cookie，浏览器后续请求会自动带上
            tokenCookieUtils.write(response, loginVO.getToken());
            return "redirect:" + safeRedirect(redirect);
        } catch (BusinessException e) {
            model.addAttribute("redirect", redirect);
            model.addAttribute("needLogin", StringUtils.hasText(redirect));
            model.addAttribute("error", e.getMessage());
            return "login";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpServletResponse response) {
        tokenCookieUtils.clear(response);
        return "redirect:/";
    }

    // ==================== 编辑资料（仅管理员） ====================

    @LoginRequired(minRole = Role.ADMIN)
    @GetMapping("/edit")
    public String editPage(Model model) {
        ProfileVO profile = profileService.getProfileOrNull();
        model.addAttribute("profile", profile);
        model.addAttribute("form", toForm(profile));
        model.addAttribute("storageType", uploadService.storageType());
        return "edit";
    }

    /**
     * 上传头像：和 /edit 分开，因为一个表单里混两种编码（普通字段 + 文件）容易出错，
     * 单独一个提交按钮更直观。
     */
    @LoginRequired(minRole = Role.ADMIN)
    @PostMapping("/edit/avatar")
    public String uploadAvatar(@RequestParam("avatarFile") MultipartFile file,
                               RedirectAttributes redirectAttributes) {
        try {
            profileService.updateAvatar(file);
            redirectAttributes.addFlashAttribute("success", "头像已更新");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("avatarError", e.getMessage());
        }
        return "redirect:/edit";
    }

    @LoginRequired(minRole = Role.ADMIN)
    @PostMapping("/edit")
    public String saveProfile(@Valid @ModelAttribute("form") ProfileUpdateDTO form,
                              BindingResult bindingResult,
                              Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("profile", profileService.getProfileOrNull());
            return "edit";
        }
        profileService.updateProfile(form);
        return "redirect:/?saved=1";
    }

    // ==================== 私有方法 ====================

    /**
     * 只允许跳回本站路径，防止 ?redirect=https://evil.com 这种开放重定向
     */
    private String safeRedirect(String redirect) {
        if (!StringUtils.hasText(redirect)) {
            return "/";
        }
        if (!redirect.startsWith("/") || redirect.startsWith("//")) {
            return "/";
        }
        return redirect;
    }

    private ProfileUpdateDTO toForm(ProfileVO profile) {
        ProfileUpdateDTO form = new ProfileUpdateDTO();
        if (profile != null) {
            form.setNickname(profile.getNickname());
            form.setAvatar(profile.getAvatar());
            form.setHeadline(profile.getHeadline());
            form.setBio(profile.getBio());
            form.setEmail(profile.getEmail());
            form.setGithub(profile.getGithub());
            form.setLocation(profile.getLocation());
        }
        return form;
    }

    /**
     * 取首字符做头像占位，兼容 emoji 这类多字节字符
     */
    private String firstChar(String text) {
        if (!StringUtils.hasText(text)) {
            return "·";
        }
        return new String(Character.toChars(text.codePointAt(0)));
    }

}
