package com.family.accounting.controller;

import com.family.accounting.dto.*;
import com.family.accounting.security.SecurityUtils;
import com.family.accounting.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 用户认证控制器
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

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
     * @param dto 登录信息
     * @return Token信息
     */
    @PostMapping("/login")
    public Result<TokenVO> login(@Valid @RequestBody LoginDTO dto) {
        TokenVO tokenVO = userService.login(dto);
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
     * @return 成功响应
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        // JWT是无状态的，客户端删除token即可
        // 如需服务端失效，可以使用token黑名单机制
        return Result.success("登出成功", null);
    }
}
