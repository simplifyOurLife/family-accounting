package com.family.accounting.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 统计数据访问接口
 */
@Mapper
public interface StatisticsMapper {

    /**
     * 按日期范围统计收入总额
     *
     * @param accountBookId 账本ID
     * @param startDate     开始日期
     * @param endDate       结束日期
     * @return 收入总额
     */
    BigDecimal sumIncomeByDateRange(
            @Param("accountBookId") Long accountBookId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 按日期范围统计支出总额
     *
     * @param accountBookId 账本ID
     * @param startDate     开始日期
     * @param endDate       结束日期
     * @return 支出总额
     */
    BigDecimal sumExpenseByDateRange(
            @Param("accountBookId") Long accountBookId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 按分类统计金额
     *
     * @param accountBookId 账本ID
     * @param type          类型: 1-支出 2-收入
     * @param startDate     开始日期
     * @param endDate       结束日期
     * @return 分类统计列表 (categoryId, categoryName, amount)
     */
    List<Map<String, Object>> sumByCategory(
            @Param("accountBookId") Long accountBookId,
            @Param("type") Integer type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
