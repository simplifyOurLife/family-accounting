package com.family.accounting.controller;

import com.family.accounting.dto.*;
import com.family.accounting.security.SecurityUtils;
import com.family.accounting.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 用户设置控制器
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 修改密码
     *
     * @param dto 密码修改信息
     * @return 成功响应
     */
    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody PasswordDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        userService.changePassword(userId, dto.getOldPassword(), dto.getNewPassword());
        return Result.success("密码修改成功", null);
    }

    /**
     * 更新个人信息
     *
     * @param dto 个人信息
     * @return 更新后的用户信息
     */
    @PutMapping("/profile")
    public Result<UserVO> updateProfile(@Valid @RequestBody ProfileDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        UserVO userVO = userService.updateProfile(userId, dto.getNickname(), dto.getAvatar());
        return Result.success("个人信息更新成功", userVO);
    }
}
