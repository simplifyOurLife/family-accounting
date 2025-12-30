package com.family.accounting.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 账本实体类
 * 对应数据库表 t_account_book
 */
@Data
public class AccountBook {

    /**
     * 账本ID
     */
    private Long id;

    /**
     * 家庭ID
     */
    private Long familyId;

    /**
     * 账本名称
     */
    private String name;

    /**
     * 是否默认账本: 0-否 1-是
     */
    private Integer isDefault;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
