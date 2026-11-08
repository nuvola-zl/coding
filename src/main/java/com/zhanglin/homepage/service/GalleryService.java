package com.zhanglin.homepage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhanglin.homepage.common.PageResult;
import com.zhanglin.homepage.common.enums.GalleryCategory;
import com.zhanglin.homepage.dto.GallerySaveDTO;
import com.zhanglin.homepage.entity.Gallery;
import com.zhanglin.homepage.vo.GalleryVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 图库业务
 *
 * @author 张林
 */
public interface GalleryService extends IService<Gallery> {

    /**
     * 查询已发布图片，category 为 null 表示不限分类
     */
    List<GalleryVO> listPublished(GalleryCategory category, int limit);

    /**
     * 分页查询已发布图片
     */
    PageResult<GalleryVO> pagePublished(GalleryCategory category, long pageNum, long pageSize);

    /**
     * 后台列表：草稿也返回
     */
    List<GalleryVO> listAll(GalleryCategory category);

    /**
     * 各分类下已发布图片数量，用于 Tab 上的数字徽标
     */
    Map<String, Long> countPublished();

    /**
     * 某个作品关联的已发布图片。
     *
     * <p>对应 {@code gallery.project_id}：在后台给图片选「关联作品」就会挂到这个作品下面，
     * 作品详情页据此展示项目截图墙。没关联的图片不会出现在这里。
     */
    List<GalleryVO> listPublishedByProject(Long projectId);

    GalleryVO getForEdit(Long id);

    /**
     * 新增：图片必传
     */
    Long create(MultipartFile file, GallerySaveDTO dto);

    /**
     * 修改：file 为 null 或空时表示不换图，只改元信息
     */
    Long update(Long id, MultipartFile file, GallerySaveDTO dto);

    void deleteGallery(Long id);

}
