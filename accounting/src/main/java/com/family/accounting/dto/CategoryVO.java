package com.family.accounting.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 分类视图对象（支持树形结构）
 */
@Data
public class CategoryVO {

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
     * 类型描述
     */
    private String typeText;

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

    /**
     * 子分类列表
     */
    private List<CategoryVO> children;

    /**
     * 是否有子分类
     */
    private Boolean hasChildren;

    /**
     * 关联交易数量（用于判断是否可删除）
     */
    private Integer transactionCount;
}
