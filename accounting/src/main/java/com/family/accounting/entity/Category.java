package com.family.accounting.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分类实体类
 * 对应数据库表 t_category
 */
@Data
public class Category {

    /**
     * 分类ID
     */
    private Long id;

    /**
     * 家庭ID
     */
    private Long familyId;

    /**
     * 父分类ID
     */
    private Long parentId;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 类型: 1-支出 2-收入
     */
    private Integer type;

    /**
     * 图标
     */
    private String icon;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    // 分类类型常量
    public static final int TYPE_EXPENSE = 1;
    public static final int TYPE_INCOME = 2;
}
