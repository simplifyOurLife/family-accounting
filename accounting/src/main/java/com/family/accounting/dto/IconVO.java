package com.family.accounting.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 图标视图对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IconVO {

    /**
     * 图标唯一标识
     */
    private String id;

    /**
     * 图标显示名称
     */
    private String name;

    /**
     * 图标分类（餐饮、交通等）
     */
    private String category;

    /**
     * 图标类型（vant/custom）
     */
    private String type;
}
