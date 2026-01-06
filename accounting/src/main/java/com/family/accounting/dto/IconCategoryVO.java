package com.family.accounting.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 图标分类视图对象 - 按分类组织的图标列表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IconCategoryVO {

    /**
     * 分类名称
     */
    private String category;

    /**
     * 该分类下的图标列表
     */
    private List<IconVO> icons;
}
