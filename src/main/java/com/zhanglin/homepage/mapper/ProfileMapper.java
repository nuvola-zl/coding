package com.zhanglin.homepage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhanglin.homepage.entity.Profile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 个人信息 Mapper
 *
 * <p>继承 BaseMapper 即拥有单表增删改查能力；
 * 复杂 SQL 写在 resources/mapper/ProfileMapper.xml 里。</p>
 *
 * @author 张林
 */
@Mapper
public interface ProfileMapper extends BaseMapper<Profile> {

    /**
     * 自定义 SQL 示例（对应 ProfileMapper.xml）
     *
     * @param nickname 昵称
     * @return 个人信息
     */
    Profile selectByNickname(@Param("nickname") String nickname);

}
