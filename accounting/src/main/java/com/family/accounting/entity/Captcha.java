package com.family.accounting.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 图片验证码实体类
 * 对应数据库表 t_captcha
 */
@Data
public class Captcha {

    /**
     * 验证码ID
     */
    private Long id;

    /**
     * 验证码唯一标识(UUID)
     */
    private String captchaKey;

    /**
     * 验证码内容
     */
    private String code;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 过期时间
     */
    private LocalDateTime expiredAt;
}
