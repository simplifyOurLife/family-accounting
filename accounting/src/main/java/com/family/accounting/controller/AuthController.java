package com.family.accounting.controller;

import com.family.accounting.dto.*;
import com.family.accounting.security.SecurityUtils;
import com.family.accounting.service.CaptchaService;
import com.family.accounting.service.TokenBlacklistService;
import com.family.accounting.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 用户认证控制器
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Autowired
    private CaptchaService captchaService;

    /**
     * 获取图片验证码
     *
     * @return 验证码信息（包含captchaKey和base64编码的图片）
     */
    @GetMapping("/captcha")
    public Result<CaptchaVO> getCaptcha() {
        CaptchaVO captchaVO = captchaService.generateCaptcha();
        return Result.success(captchaVO);
    }

    /**
     * 用户注册
     *
     * @param dto 注册信息
     * @return 用户信息
     */
    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody RegisterDTO dto) {
        UserVO userVO = userService.register(dto);
        return Result.success("注册成功", userVO);
    }

    /**
     * 用户登录
     *
     * @param dto     登录信息
     * @param request HTTP请求对象
     * @return Token信息
     */
    @PostMapping("/login")
    public Result<TokenVO> login(@Valid @RequestBody LoginDTO dto, HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        TokenVO tokenVO = userService.login(dto, ipAddress);
        return Result.success("登录成功", tokenVO);
    }

    /**
     * 获取当前用户信息
     *
     * @return 用户信息
     */
    @GetMapping("/info")
    public Result<UserVO> info() {
        Long userId = SecurityUtils.getCurrentUserId();
        UserVO userVO = userService.getUserById(userId);
        return Result.success(userVO);
    }

    /**
     * 用户登出
     *
     * @param request HTTP请求对象
     * @return 成功响应
     */
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        try {
            // 获取当前用户ID
            Long userId = SecurityUtils.getCurrentUserId();
            
            // 获取当前请求的JWT令牌
            String token = extractTokenFromRequest(request);
            
            if (StringUtils.hasText(token) && userId != null) {
                // 将当前令牌加入黑名单
                tokenBlacklistService.addToBlacklist(token, userId, "用户登出");
            }
        } catch (Exception e) {
            // 登出失败不影响响应，只记录日志
            // 客户端删除token即可实现登出效果
        }
        
        return Result.success("登出成功", null);
    }

    /**
     * 获取客户端IP地址
     *
     * @param request HTTP请求对象
     * @return IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * 从请求头中提取JWT令牌
     *
     * @param request HTTP请求对象
     * @return JWT令牌
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
