package com.family.accounting.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 统计数据视图对象
 */
@Data
public class StatisticsVO {

    /**
     * 统计周期类型: daily, weekly, monthly, yearly
     */
    private String periodType;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 收入总额
     */
    private BigDecimal totalIncome;

    /**
     * 支出总额
     */
    private BigDecimal totalExpense;

    /**
     * 结余（收入 - 支出）
     */
    private BigDecimal balance;

    /**
     * 分类统计（包含收入和支出，前端通过type字段区分）
     */
    private List<CategoryStatVO> categoryStats;
}
