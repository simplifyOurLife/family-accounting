package com.family.accounting.dto;

import lombok.Data;

import javax.validation.constraints.Size;

/**
 * 个人信息更新请求DTO
 */
@Data
public class ProfileDTO {

    /**
     * 昵称
     */
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;

    /**
     * 头像URL
     */
    @Size(max = 255, message = "头像URL长度不能超过255个字符")
    private String avatar;
}
