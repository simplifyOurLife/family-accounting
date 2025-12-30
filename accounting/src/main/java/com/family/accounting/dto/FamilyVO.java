package com.family.accounting.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 家庭信息视图对象
 */
@Data
public class FamilyVO {

    /**
     * 家庭ID
     */
    private Long id;

    /**
     * 家庭名称
     */
    private String name;

    /**
     * 管理员用户ID
     */
    private Long adminId;

    /**
     * 管理员昵称
     */
    private String adminNickname;

    /**
     * 成员数量
     */
    private Integer memberCount;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
