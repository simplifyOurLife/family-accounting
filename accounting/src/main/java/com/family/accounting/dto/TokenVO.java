package com.family.accounting.dto;

import lombok.Data;

/**
 * 登录Token响应VO
 */
@Data
public class TokenVO {

    /**
     * JWT Token
     */
    private String token;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 昵称
     */
    private String nickname;
}
