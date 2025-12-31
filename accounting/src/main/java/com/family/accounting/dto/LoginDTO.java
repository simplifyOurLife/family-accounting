package com.family.accounting.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 用户登录请求DTO
 */
@Data
public class LoginDTO {

    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    private String phone;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 图片验证码唯一标识
     */
    @NotBlank(message = "图片验证码标识不能为空")
    private String captchaKey;

    /**
     * 图片验证码
     */
    @NotBlank(message = "图片验证码不能为空")
    @Size(min = 4, max = 4, message = "图片验证码必须是4位")
    private String captchaCode;
}
