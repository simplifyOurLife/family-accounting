package com.family.accounting.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 账本视图对象
 */
@Data
public class AccountBookVO {

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
     * 是否默认账本
     */
    private Boolean isDefault;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 关联交易数量（用于判断是否可删除）
     */
    private Integer transactionCount;
}
