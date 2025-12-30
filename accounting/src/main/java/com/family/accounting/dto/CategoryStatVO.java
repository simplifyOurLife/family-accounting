package com.family.accounting.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 分类统计视图对象
 */
@Data
public class CategoryStatVO {

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 分类图标
     */
    private String categoryIcon;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 占比（百分比，如 25.50 表示 25.50%）
     */
    private BigDecimal percentage;

    /**
     * 交易笔数
     */
    private Integer count;

    /**
     * 类型: 1-支出 2-收入
     */
    private Integer type;
}
