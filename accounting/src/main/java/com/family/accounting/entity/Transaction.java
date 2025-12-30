package com.family.accounting.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 交易记录实体类
 * 对应数据库表 t_transaction
 */
@Data
public class Transaction {

    /**
     * 交易ID
     */
    private Long id;

    /**
     * 账本ID
     */
    private Long accountBookId;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 记录人ID
     */
    private Long userId;

    /**
     * 类型: 1-支出 2-收入
     */
    private Integer type;

    /**
     * 金额（使用BigDecimal保证精度）
     */
    private BigDecimal amount;

    /**
     * 备注
     */
    private String note;

    /**
     * 交易日期
     */
    private LocalDate transactionDate;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    // 交易类型常量
    public static final int TYPE_EXPENSE = 1;
    public static final int TYPE_INCOME = 2;
}
