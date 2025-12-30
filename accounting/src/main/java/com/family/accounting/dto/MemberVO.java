package com.family.accounting.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 家庭成员视图对象
 */
@Data
public class MemberVO {

    /**
     * 成员记录ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户手机号
     */
    private String phone;

    /**
     * 用户昵称
     */
    private String userNickname;

    /**
     * 在家庭中的昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 是否是管理员
     */
    private Boolean isAdmin;

    /**
     * 加入时间
     */
    private LocalDateTime joinedAt;
}
