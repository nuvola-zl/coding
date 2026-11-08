package com.zhanglin.homepage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhanglin.homepage.common.PageResult;
import com.zhanglin.homepage.dto.ProjectSaveDTO;
import com.zhanglin.homepage.entity.Project;
import com.zhanglin.homepage.vo.ProjectVO;

import java.util.List;

/**
 * 作品业务接口
 *
 * @author 张林
 */
public interface ProjectService extends IService<Project> {

    /**
     * 主页用：取最新的 N 个已发布作品
     */
    List<ProjectVO> listPublished(int limit);

    /**
     * 作品列表页：已发布作品分页
     */
    PageResult<ProjectVO> pagePublished(long pageNum, long pageSize);

    /**
     * 详情页：只允许查看已发布的作品，找不到或未发布都返回 null
     * （由调用方决定渲染 404 页面还是报错）
     */
    ProjectVO getPublishedDetail(Long id);

    /**
     * 后台列表：包含草稿
     */
    List<ProjectVO> listAll();

    /**
     * 后台编辑页：按 id 取原始数据（草稿也能取）
     */
    ProjectVO getForEdit(Long id);

    /**
     * 新增或修改，返回主键
     *
     * @param id  null 表示新增
     * @param dto 表单数据
     */
    Long saveProject(Long id, ProjectSaveDTO dto);

    /**
     * 逻辑删除
     */
    void deleteProject(Long id);

}
