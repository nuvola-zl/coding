package com.zhanglin.homepage.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhanglin.homepage.common.PageResult;
import com.zhanglin.homepage.common.ResultCode;
import com.zhanglin.homepage.common.enums.GalleryCategory;
import com.zhanglin.homepage.common.enums.ProjectStatus;
import com.zhanglin.homepage.common.exception.BusinessException;
import com.zhanglin.homepage.dto.GallerySaveDTO;
import com.zhanglin.homepage.entity.Gallery;
import com.zhanglin.homepage.entity.Project;
import com.zhanglin.homepage.mapper.GalleryMapper;
import com.zhanglin.homepage.mapper.ProjectMapper;
import com.zhanglin.homepage.service.GalleryService;
import com.zhanglin.homepage.service.storage.StorageService;
import com.zhanglin.homepage.service.UploadService;
import com.zhanglin.homepage.vo.GalleryVO;
import com.zhanglin.homepage.vo.UploadResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 图库业务实现
 *
 * @author 张林
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GalleryServiceImpl extends ServiceImpl<GalleryMapper, Gallery> implements GalleryService {

    /** 图片存到 OSS 的这个目录下，和头像分开 */
    private static final String UPLOAD_DIR = "gallery";

    private static final int MAX_LIST_LIMIT = 100;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final UploadService uploadService;
    private final StorageService storageService;
    private final ProjectMapper projectMapper;

    @Override
    public List<GalleryVO> listPublished(GalleryCategory category, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, MAX_LIST_LIMIT));
        List<Gallery> list = lambdaQuery()
                .eq(Gallery::getStatus, ProjectStatus.PUBLISHED.name())
                .eq(category != null, Gallery::getCategory, category == null ? null : category.name())
                .orderByAsc(Gallery::getSortOrder)
                .orderByDesc(Gallery::getId)
                .last("LIMIT " + safeLimit)
                .list();
        return toVOList(list);
    }

    @Override
    public PageResult<GalleryVO> pagePublished(GalleryCategory category, long pageNum, long pageSize) {
        Page<Gallery> page = new Page<>(Math.max(1, pageNum), Math.min(Math.max(1, pageSize), 60));
        page(page, Wrappers.<Gallery>lambdaQuery()
                .eq(Gallery::getStatus, ProjectStatus.PUBLISHED.name())
                .eq(category != null, Gallery::getCategory, category == null ? null : category.name())
                .orderByAsc(Gallery::getSortOrder)
                .orderByDesc(Gallery::getId));

        // 整页一次性转换，作品标题只查一次库
        PageResult<GalleryVO> result = new PageResult<>();
        result.setRecords(toVOList(page.getRecords()));
        result.setTotal(page.getTotal());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        result.setPages(page.getPages());
        return result;
    }

    @Override
    public List<GalleryVO> listAll(GalleryCategory category) {
        List<Gallery> list = lambdaQuery()
                .eq(category != null, Gallery::getCategory, category == null ? null : category.name())
                .orderByAsc(Gallery::getSortOrder)
                .orderByDesc(Gallery::getId)
                .list();
        return toVOList(list);
    }

    @Override
    public Map<String, Long> countPublished() {
        Map<String, Long> result = new LinkedHashMap<>();
        for (GalleryCategory category : GalleryCategory.values()) {
            result.put(category.name(), lambdaQuery()
                    .eq(Gallery::getCategory, category.name())
                    .eq(Gallery::getStatus, ProjectStatus.PUBLISHED.name())
                    .count());
        }
        return result;
    }

    @Override
    public List<GalleryVO> listPublishedByProject(Long projectId) {
        if (projectId == null) {
            return List.of();
        }
        List<Gallery> list = lambdaQuery()
                .eq(Gallery::getProjectId, projectId)
                .eq(Gallery::getStatus, ProjectStatus.PUBLISHED.name())
                .orderByAsc(Gallery::getSortOrder)
                .orderByDesc(Gallery::getId)
                .list();
        return toVOList(list);
    }

    @Override
    public GalleryVO getForEdit(Long id) {
        Gallery gallery = getById(id);
        if (gallery == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "图片不存在");
        }
        return toVOList(List.of(gallery)).get(0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(MultipartFile file, GallerySaveDTO dto) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "请选择要上传的图片");
        }
        UploadResultVO uploaded = uploadService.uploadImage(file, UPLOAD_DIR);

        Gallery gallery = new Gallery();
        applyForm(gallery, dto);
        gallery.setUrl(uploaded.getUrl());
        save(gallery);
        log.info("图片已新增: id={}, category={}, url={}", gallery.getId(), gallery.getCategory(), gallery.getUrl());
        return gallery.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long update(Long id, MultipartFile file, GallerySaveDTO dto) {
        Gallery gallery = getById(id);
        if (gallery == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "图片不存在");
        }

        String oldUrl = gallery.getUrl();
        applyForm(gallery, dto);

        // 换了新图：先上传成功再删旧图，顺序反了会出现「新的没传上、旧的已删掉」
        if (file != null && !file.isEmpty()) {
            UploadResultVO uploaded = uploadService.uploadImage(file, UPLOAD_DIR);
            gallery.setUrl(uploaded.getUrl());
        }

        updateById(gallery);
        if (!Objects.equals(oldUrl, gallery.getUrl())) {
            storageService.deleteQuietly(oldUrl);
        }
        log.info("图片已更新: id={}", id);
        return id;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGallery(Long id) {
        Gallery gallery = getById(id);
        if (gallery == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "图片不存在");
        }
        removeById(id);
        // 数据是逻辑删除，但文件删了能省存储费；如果以后想「恢复」，把这一行去掉即可
        storageService.deleteQuietly(gallery.getUrl());
        log.info("图片已删除: id={}, url={}", id, gallery.getUrl());
    }

    // ==================== 私有方法 ====================

    private void applyForm(Gallery gallery, GallerySaveDTO dto) {
        gallery.setTitle(dto.getTitle().trim());
        gallery.setDescription(StringUtils.hasText(dto.getDescription()) ? dto.getDescription().trim() : null);
        gallery.setCategory(GalleryCategory.of(dto.getCategory()).name());
        gallery.setProjectId(dto.getProjectId());
        gallery.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        gallery.setStatus(ProjectStatus.of(dto.getStatus()).name());
    }

    /**
     * 批量转 VO：关联作品标题用一次查询取回，避免每张图片都去查一次库（N+1 问题）
     */
    private List<GalleryVO> toVOList(List<Gallery> list) {
        Set<Long> projectIds = list.stream()
                .map(Gallery::getProjectId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> projectTitles = projectIds.isEmpty()
                ? Map.of()
                : projectMapper.selectBatchIds(projectIds).stream()
                .collect(Collectors.toMap(Project::getId, Project::getTitle, (a, b) -> a));

        return list.stream().map(gallery -> {
            GalleryVO vo = new GalleryVO();
            vo.setId(gallery.getId());
            vo.setTitle(gallery.getTitle());
            vo.setDescription(gallery.getDescription());
            vo.setUrl(gallery.getUrl());
            vo.setCategory(gallery.getCategory());
            vo.setCategoryLabel(GalleryCategory.of(gallery.getCategory()).getLabel());
            vo.setProjectId(gallery.getProjectId());
            vo.setProjectTitle(gallery.getProjectId() == null ? null : projectTitles.get(gallery.getProjectId()));
            vo.setSortOrder(gallery.getSortOrder());
            vo.setStatus(gallery.getStatus());
            vo.setStatusLabel(ProjectStatus.of(gallery.getStatus()).getLabel());
            if (gallery.getCreateTime() != null) {
                vo.setCreateTimeText(gallery.getCreateTime().format(DATE_FORMATTER));
            }
            return vo;
        }).toList();
    }

}
