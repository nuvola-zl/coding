package com.zhanglin.homepage.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhanglin.homepage.common.PageResult;
import com.zhanglin.homepage.common.ResultCode;
import com.zhanglin.homepage.common.enums.ProjectStatus;
import com.zhanglin.homepage.common.exception.BusinessException;
import com.zhanglin.homepage.dto.ProjectSaveDTO;
import com.zhanglin.homepage.entity.Project;
import com.zhanglin.homepage.mapper.ProjectMapper;
import com.zhanglin.homepage.service.ProjectService;
import com.zhanglin.homepage.vo.ProjectVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * 作品业务实现
 *
 * @author 张林
 */
@Slf4j
@Service
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements ProjectService {

    private static final int MAX_LIST_LIMIT = 50;

    private static final DateTimeFormatter UPDATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public List<ProjectVO> listPublished(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, MAX_LIST_LIMIT));
        List<Project> projects = lambdaQuery()
                .eq(Project::getStatus, ProjectStatus.PUBLISHED.name())
                .orderByAsc(Project::getSortOrder)
                .orderByDesc(Project::getId)
                .last("LIMIT " + safeLimit)
                .list();
        return projects.stream().map(this::toVO).toList();
    }

    @Override
    public PageResult<ProjectVO> pagePublished(long pageNum, long pageSize) {
        Page<Project> page = new Page<>(Math.max(1, pageNum), Math.min(Math.max(1, pageSize), 50));
        page(page, Wrappers.<Project>lambdaQuery()
                .eq(Project::getStatus, ProjectStatus.PUBLISHED.name())
                .orderByAsc(Project::getSortOrder)
                .orderByDesc(Project::getId));
        return PageResult.of(page, this::toVO);
    }

    @Override
    public ProjectVO getPublishedDetail(Long id) {
        Project project = getById(id);
        // 草稿不能被猜 id 看到，统一按"不存在"处理
        if (project == null || !ProjectStatus.PUBLISHED.name().equals(project.getStatus())) {
            return null;
        }
        return toVO(project);
    }

    @Override
    public List<ProjectVO> listAll() {
        List<Project> projects = lambdaQuery()
                .orderByAsc(Project::getSortOrder)
                .orderByDesc(Project::getId)
                .list();
        return projects.stream().map(this::toVO).toList();
    }

    @Override
    public ProjectVO getForEdit(Long id) {
        Project project = getById(id);
        if (project == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "作品不存在");
        }
        return toVO(project);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveProject(Long id, ProjectSaveDTO dto) {
        Project project;
        if (id == null) {
            project = new Project();
        } else {
            project = getById(id);
            if (project == null) {
                throw new BusinessException(ResultCode.NOT_FOUND, "作品不存在");
            }
        }

        BeanUtils.copyProperties(dto, project);
        project.setStatus(ProjectStatus.of(dto.getStatus()).name());
        if (project.getSortOrder() == null) {
            project.setSortOrder(0);
        }
        // 技术栈统一成英文逗号，避免中文逗号混进来导致页面上少一个标签
        project.setTechStack(normalizeTechStack(project.getTechStack()));

        saveOrUpdate(project);
        log.info("作品已保存: id={}, title={}, status={}", project.getId(), project.getTitle(), project.getStatus());
        return project.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProject(Long id) {
        Project project = getById(id);
        if (project == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "作品不存在");
        }
        // removeById 走的是逻辑删除，deleted 置 1，数据还在库里
        removeById(id);
        log.info("作品已删除: id={}, title={}", id, project.getTitle());
    }

    // ==================== 私有方法 ====================

    private String normalizeTechStack(String techStack) {
        if (!StringUtils.hasText(techStack)) {
            return null;
        }
        return String.join(",", splitTechStack(techStack));
    }

    private List<String> splitTechStack(String techStack) {
        if (!StringUtils.hasText(techStack)) {
            return List.of();
        }
        return Arrays.stream(techStack.split("[,，]"))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
    }

    private ProjectVO toVO(Project project) {
        ProjectVO vo = new ProjectVO();
        BeanUtils.copyProperties(project, vo);
        vo.setTechList(splitTechStack(project.getTechStack()));
        vo.setStatusLabel(ProjectStatus.of(project.getStatus()).getLabel());
        if (project.getUpdateTime() != null) {
            vo.setUpdateTimeText(project.getUpdateTime().format(UPDATE_TIME_FORMATTER));
        }
        return vo;
    }

}
