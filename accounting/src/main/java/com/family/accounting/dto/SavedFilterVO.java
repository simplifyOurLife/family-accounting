package com.family.accounting.dto;

import java.time.LocalDateTime;

/**
 * 保存的筛选条件VO
 */
public class SavedFilterVO {
    
    /**
     * 筛选条件ID
     */
    private Long id;
    
    /**
     * 筛选条件名称
     */
    private String name;
    
    /**
     * 筛选条件
     */
    private FilterConditionVO filterCondition;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    // Getters and Setters
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FilterConditionVO getFilterCondition() {
        return filterCondition;
    }

    public void setFilterCondition(FilterConditionVO filterCondition) {
        this.filterCondition = filterCondition;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
