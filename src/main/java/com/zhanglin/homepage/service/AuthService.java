package com.zhanglin.homepage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhanglin.homepage.dto.LoginDTO;
import com.zhanglin.homepage.dto.RegisterDTO;
import com.zhanglin.homepage.entity.SysUser;
import com.zhanglin.homepage.vo.LoginVO;
import com.zhanglin.homepage.vo.UserVO;

/**
 * 认证业务接口：注册、登录、当前用户
 *
 * @author 张林
 */
public interface AuthService extends IService<SysUser> {

    /**
     * 注册；系统里第一个注册的账号自动成为管理员，其余为普通用户
     */
    UserVO register(RegisterDTO dto);

    /**
     * 登录，成功后返回 JWT
     */
    LoginVO login(LoginDTO dto);

    /**
     * 查询当前登录用户
     */
    UserVO currentUser();

}
