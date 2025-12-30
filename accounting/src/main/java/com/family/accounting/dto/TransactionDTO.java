package com.family.accounting.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 交易记录创建/更新请求DTO
 */
@Data
public class TransactionDTO {

    /**
     * 账本ID（创建时必填）
     */
    private Long accountBookId;

    /**
     * 分类ID
     */
    @NotNull(message = "分类不能为空")
    private Long categoryId;

    /**
     * 类型: 1-支出 2-收入
     */
    @NotNull(message = "交易类型不能为空")
    private Integer type;

    /**
     * 金额
     */
    @NotNull(message = "金额不能为空")
    @Positive(message = "金额必须大于0")
    private BigDecimal amount;

    /**
     * 备注（可选）
     */
    @Size(max = 200, message = "备注长度不能超过200个字符")
    private String note;

    /**
     * 交易日期
     */
    @NotNull(message = "交易日期不能为空")
    private LocalDate transactionDate;
}
