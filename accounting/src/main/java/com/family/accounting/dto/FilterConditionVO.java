package com.family.accounting.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 筛选条件VO（用于保存和应用筛选条件）
 */
public class FilterConditionVO {
    
    /**
     * 开始日期
     */
    private LocalDate startDate;
    
    /**
     * 结束日期
     */
    private LocalDate endDate;
    
    /**
     * 最小金额
     */
    private BigDecimal minAmount;
    
    /**
     * 最大金额
     */
    private BigDecimal maxAmount;
    
    /**
     * 分类ID列表
     */
    private List<Long> categoryIds;
    
    /**
     * 成员ID列表
     */
    private List<Long> memberIds;
    
    /**
     * 交易类型：1-支出 2-收入
     */
    private Integer type;
    
    /**
     * 账本ID
     */
    private Long accountBookId;

    // Getters and Setters
    
    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(BigDecimal minAmount) {
        this.minAmount = minAmount;
    }

    public BigDecimal getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(BigDecimal maxAmount) {
        this.maxAmount = maxAmount;
    }

    public List<Long> getCategoryIds() {
        return categoryIds;
    }

    public void setCategoryIds(List<Long> categoryIds) {
        this.categoryIds = categoryIds;
    }

    public List<Long> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(List<Long> memberIds) {
        this.memberIds = memberIds;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Long getAccountBookId() {
        return accountBookId;
    }

    public void setAccountBookId(Long accountBookId) {
        this.accountBookId = accountBookId;
    }
}
