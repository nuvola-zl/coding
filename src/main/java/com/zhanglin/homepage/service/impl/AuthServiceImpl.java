package com.zhanglin.homepage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhanglin.homepage.common.ResultCode;
import com.zhanglin.homepage.common.exception.BusinessException;
import com.zhanglin.homepage.config.AuthProperties;
import com.zhanglin.homepage.dto.LoginDTO;
import com.zhanglin.homepage.dto.RegisterDTO;
import com.zhanglin.homepage.entity.SysUser;
import com.zhanglin.homepage.mapper.SysUserMapper;
import com.zhanglin.homepage.security.JwtUtils;
import com.zhanglin.homepage.security.LoginUser;
import com.zhanglin.homepage.security.Role;
import com.zhanglin.homepage.security.UserContext;
import com.zhanglin.homepage.service.AuthService;
import com.zhanglin.homepage.vo.LoginVO;
import com.zhanglin.homepage.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 认证业务实现
 *
 * @author 张林
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements AuthService {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthProperties authProperties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO register(RegisterDTO dto) {
        // 注册开关：默认关闭。个人站点不需要别人注册，开着只是白送攻击面
        if (!authProperties.isRegisterEnabled()) {
            throw new BusinessException(ResultCode.FORBIDDEN, "注册功能已关闭");
        }

        String username = dto.getUsername().trim();

        long existCount = lambdaQuery().eq(SysUser::getUsername, username).count();
        if (existCount > 0) {
            throw new BusinessException("账号 " + username + " 已被注册");
        }

        // 第一个注册的人是站长（管理员），后面注册的都是普通用户，只能登录不能改内容
        boolean firstUser = count() == 0;

        SysUser user = new SysUser();
        user.setUsername(username);
        // 只存密文，数据库里永远看不到明文密码
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(StringUtils.hasText(dto.getNickname()) ? dto.getNickname().trim() : username);
        user.setAvatar(dto.getAvatar());
        user.setRole(firstUser ? Role.ADMIN.name() : Role.USER.name());
        user.setStatus(1);
        save(user);

        log.info("用户注册成功: username={}, role={}", username, user.getRole());
        return toVO(user);
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        String username = dto.getUsername().trim();
        SysUser user = lambdaQuery().eq(SysUser::getUsername, username).one();

        // 账号不存在和密码错误给同一个提示，避免被人枚举出哪些账号存在
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "账号或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(ResultCode.FORBIDDEN, "该账号已被禁用");
        }

        LoginUser loginUser = LoginUser.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .role(Role.of(user.getRole()))
                .build();

        log.info("用户登录成功: username={}, role={}", username, user.getRole());
        return LoginVO.builder()
                .token(jwtUtils.generate(loginUser))
                .tokenType("Bearer")
                .expiresIn(jwtUtils.getExpireSeconds())
                .user(toVO(user))
                .build();
    }

    @Override
    public UserVO currentUser() {
        Long userId = UserContext.requireUserId();
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "账号不存在或已注销");
        }
        return toVO(user);
    }

    private UserVO toVO(SysUser user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

}
