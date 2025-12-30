package com.family.accounting.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 交易记录视图对象
 */
@Data
public class TransactionVO {

    /**
     * 交易ID
     */
    private Long id;

    /**
     * 账本ID
     */
    private Long accountBookId;

    /**
     * 账本名称
     */
    private String accountBookName;

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
     * 记录人ID
     */
    private Long userId;

    /**
     * 记录人昵称
     */
    private String userNickname;

    /**
     * 类型: 1-支出 2-收入
     */
    private Integer type;

    /**
     * 类型描述
     */
    private String typeText;

    /**
     * 金额
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
}
