package com.family.accounting.dto;

import lombok.Data;

/**
 * 图片验证码响应VO
 */
@Data
public class CaptchaVO {

    /**
     * 验证码唯一标识（UUID）
     */
    private String captchaKey;

    /**
     * base64编码的验证码图片
     */
    private String captchaImage;
}
