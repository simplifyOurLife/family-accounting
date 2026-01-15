package com.family.accounting.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 保存筛选条件DTO
 */
public class SavedFilterDTO {
    
    /**
     * 筛选条件名称
     */
    @NotBlank(message = "筛选条件名称不能为空")
    private String name;
    
    /**
     * 筛选条件
     */
    @NotNull(message = "筛选条件不能为空")
    private FilterConditionVO filterCondition;

    // Getters and Setters
    
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
}
