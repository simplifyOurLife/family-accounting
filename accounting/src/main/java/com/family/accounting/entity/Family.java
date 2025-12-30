package com.family.accounting.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 家庭实体类
 * 对应数据库表 t_family
 */
@Data
public class Family {

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
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
