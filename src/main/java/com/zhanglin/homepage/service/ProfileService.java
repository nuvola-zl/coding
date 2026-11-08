package com.zhanglin.homepage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhanglin.homepage.dto.ProfileUpdateDTO;
import com.zhanglin.homepage.entity.Profile;
import com.zhanglin.homepage.vo.ProfileVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 个人信息业务接口
 *
 * @author 张林
 */
public interface ProfileService extends IService<Profile> {

    /**
     * 查询个人信息（个人主页只有一条记录）
     */
    ProfileVO getProfile();

    /**
     * 查询个人信息，没有初始化数据时返回 null（页面渲染用，不抛异常）
     */
    ProfileVO getProfileOrNull();

    /**
     * 修改个人信息；不存在则新建
     */
    ProfileVO updateProfile(ProfileUpdateDTO dto);

    /**
     * 上传并替换头像，旧头像文件会一并删除
     */
    ProfileVO updateAvatar(MultipartFile file);

}
