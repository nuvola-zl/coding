package com.zhanglin.homepage.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhanglin.homepage.common.ResultCode;
import com.zhanglin.homepage.common.exception.BusinessException;
import com.zhanglin.homepage.dto.ProfileUpdateDTO;
import com.zhanglin.homepage.entity.Profile;
import com.zhanglin.homepage.mapper.ProfileMapper;
import com.zhanglin.homepage.service.ProfileService;
import com.zhanglin.homepage.service.UploadService;
import com.zhanglin.homepage.service.storage.StorageService;
import com.zhanglin.homepage.vo.ProfileVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * 个人信息业务实现
 *
 * @author 张林
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl extends ServiceImpl<ProfileMapper, Profile> implements ProfileService {

    /** 头像上传的存储子目录 */
    private static final String AVATAR_DIR = "avatar";

    private final UploadService uploadService;
    private final StorageService storageService;

    @Override
    public ProfileVO getProfile() {
        Profile profile = getFirstProfile();
        if (profile == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(),
                    "个人信息尚未初始化，请先执行 sql/init.sql 初始化数据");
        }
        return toVO(profile);
    }

    @Override
    public ProfileVO getProfileOrNull() {
        Profile profile = getFirstProfile();
        return profile == null ? null : toVO(profile);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProfileVO updateProfile(ProfileUpdateDTO dto) {
        Profile profile = getFirstProfile();
        if (profile == null) {
            profile = new Profile();
            BeanUtils.copyProperties(dto, profile);
            save(profile);
            log.info("个人信息首次创建, id={}", profile.getId());
        } else {
            BeanUtils.copyProperties(dto, profile);
            updateById(profile);
            log.info("个人信息已更新, id={}", profile.getId());
        }
        return toVO(profile);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProfileVO updateAvatar(MultipartFile file) {
        Profile profile = getFirstProfile();
        if (profile == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(),
                    "个人信息尚未初始化，请先执行 sql/init.sql 初始化数据");
        }

        String oldAvatar = profile.getAvatar();
        // 先上传成功拿到新地址，再改库；顺序反了会出现「库里有地址、文件却不存在」的坏数据
        String newAvatar = uploadService.uploadImage(file, AVATAR_DIR).getUrl();

        profile.setAvatar(newAvatar);
        updateById(profile);

        // 旧头像是我们自己存的才删，用户填的外部图片地址不能被误删
        if (!java.util.Objects.equals(oldAvatar, newAvatar)) {
            storageService.deleteQuietly(oldAvatar);
        }

        log.info("头像已更新, profileId={}, url={}", profile.getId(), newAvatar);
        return toVO(profile);
    }

    /**
     * 取表中第一条记录（整个项目只维护一条个人信息）
     */
    private Profile getFirstProfile() {        return getOne(Wrappers.<Profile>lambdaQuery()
                .orderByAsc(Profile::getId)
                .last("LIMIT 1"));
    }

    private ProfileVO toVO(Profile profile) {
        ProfileVO vo = new ProfileVO();
        BeanUtils.copyProperties(profile, vo);
        return vo;
    }

}
