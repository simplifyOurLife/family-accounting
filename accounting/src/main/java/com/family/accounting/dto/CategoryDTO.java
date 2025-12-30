package com.family.accounting.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 分类创建/更新请求DTO
 */
@Data
public class CategoryDTO {

    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称长度不能超过50个字符")
    private String name;

    /**
     * 父分类ID（可选，为空表示根分类）
     */
    private Long parentId;

    /**
     * 类型: 1-支出 2-收入
     */
    @NotNull(message = "分类类型不能为空")
    private Integer type;

    /**
     * 图标（可选）
     */
    @Size(max = 50, message = "图标名称长度不能超过50个字符")
    private String icon;

    /**
     * 排序（可选）
     */
    private Integer sortOrder;
}
