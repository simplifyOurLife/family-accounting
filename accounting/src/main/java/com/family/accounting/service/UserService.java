package com.family.accounting.service;

import com.family.accounting.dto.LoginDTO;
import com.family.accounting.dto.RegisterDTO;
import com.family.accounting.dto.TokenVO;
import com.family.accounting.dto.UserVO;
import com.family.accounting.entity.User;
import com.family.accounting.exception.BusinessException;
import com.family.accounting.mapper.UserMapper;
import com.family.accounting.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户服务类
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Autowired
    private CaptchaService captchaService;

    /**
     * 用户注册
     *
     * @param dto 注册信息
     * @return 用户信息
     */
    @Transactional
    public UserVO register(RegisterDTO dto) {
        // 验证图片验证码
        if (!captchaService.verifyCaptcha(dto.getCaptchaKey(), dto.getCaptchaCode())) {
            throw new BusinessException("图片验证码错误或已过期");
        }

        // 检查手机号是否已注册
        if (userMapper.existsByPhone(dto.getPhone()) > 0) {
            throw new BusinessException("该手机号已注册");
        }

        // 创建用户实体
        User user = new User();
        user.setPhone(dto.getPhone());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname());

        // 插入数据库
        userMapper.insert(user);

        // 返回用户信息
        return convertToVO(user);
    }

    /**
     * 用户登录
     *
     * @param dto       登录信息
     * @param ipAddress IP地址
     * @return Token信息
     */
    public TokenVO login(LoginDTO dto, String ipAddress) {
        // 安全检查：IP限制和账户锁定
        securityService.validateLoginSecurity(dto.getPhone(), ipAddress);

        try {
            // 验证图片验证码
            if (!captchaService.verifyCaptcha(dto.getCaptchaKey(), dto.getCaptchaCode())) {
                // 记录失败的登录尝试
                securityService.recordLoginAttempt(dto.getPhone(), ipAddress, false);
                throw new BusinessException("图片验证码错误或已过期");
            }

            // 查询用户
            User user = userMapper.findByPhone(dto.getPhone());
            if (user == null) {
                // 记录失败的登录尝试
                securityService.recordLoginAttempt(dto.getPhone(), ipAddress, false);
                throw new BusinessException("用户不存在");
            }

            // 验证密码
            if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
                // 记录失败的登录尝试
                securityService.recordLoginAttempt(dto.getPhone(), ipAddress, false);
                throw new BusinessException("密码错误");
            }

            // 登录成功，记录成功的登录尝试
            securityService.recordLoginAttempt(dto.getPhone(), ipAddress, true);

            // 生成token
            String token = jwtUtil.generateToken(user.getId(), user.getPhone());

            // 返回token信息
            TokenVO tokenVO = new TokenVO();
            tokenVO.setToken(token);
            tokenVO.setUserId(user.getId());
            tokenVO.setPhone(user.getPhone());
            tokenVO.setNickname(user.getNickname());

            return tokenVO;
        } catch (BusinessException e) {
            throw e;
        }
    }

    /**
     * 根据ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    public UserVO getUserById(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return convertToVO(user);
    }

    /**
     * 根据手机号获取用户信息
     *
     * @param phone 手机号
     * @return 用户信息
     */
    public UserVO getUserByPhone(String phone) {
        User user = userMapper.findByPhone(phone);
        if (user == null) {
            return null;
        }
        return convertToVO(user);
    }

    /**
     * 修改密码
     *
     * @param userId      用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        // 查询用户
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }

        // 更新密码
        String encodedPassword = passwordEncoder.encode(newPassword);
        userMapper.updatePassword(userId, encodedPassword);

        // 使所有现有JWT令牌失效
        tokenBlacklistService.invalidateAllUserTokens(userId, "密码修改");
    }

    /**
     * 验证密码是否正确
     *
     * @param userId   用户ID
     * @param password 密码
     * @return 是否正确
     */
    public boolean verifyPassword(Long userId, String password) {
        User user = userMapper.findById(userId);
        if (user == null) {
            return false;
        }
        return passwordEncoder.matches(password, user.getPassword());
    }

    /**
     * 更新个人信息
     *
     * @param userId   用户ID
     * @param nickname 昵称
     * @param avatar   头像URL
     * @return 更新后的用户信息
     */
    @Transactional
    public UserVO updateProfile(Long userId, String nickname, String avatar) {
        // 查询用户
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 更新用户信息
        user.setNickname(nickname);
        user.setAvatar(avatar);
        userMapper.update(user);

        // 返回更新后的用户信息
        return convertToVO(user);
    }

    /**
     * 将User实体转换为UserVO
     */
    private UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setPhone(user.getPhone());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setCreatedAt(user.getCreatedAt());
        return vo;
    }
}
