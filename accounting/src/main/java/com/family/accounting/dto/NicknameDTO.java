package com.family.accounting.dto;

import lombok.Data;

import javax.validation.constraints.Size;

/**
 * 设置成员昵称请求DTO
 */
@Data
public class NicknameDTO {

    /**
     * 昵称
     */
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;
}
