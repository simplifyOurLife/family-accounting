package com.family.accounting.controller;

import com.family.accounting.config.DefaultCategoryConfig;
import com.family.accounting.dto.IconCategoryVO;
import com.family.accounting.dto.IconVO;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 分类图标功能测试
 */
public class CategoryControllerIconTest {

    @Test
    public void testIconLibraryNotEmpty() {
        // 验证图标库不为空
        assertNotNull(DefaultCategoryConfig.ICON_LIBRARY);
        assertFalse(DefaultCategoryConfig.ICON_LIBRARY.isEmpty());
        
        System.out.println("图标库包含 " + DefaultCategoryConfig.ICON_LIBRARY.size() + " 个分类");
        
        // 统计总图标数
        int totalIcons = 0;
        for (Map.Entry<String, List<DefaultCategoryConfig.IconInfo>> entry : 
                DefaultCategoryConfig.ICON_LIBRARY.entrySet()) {
            String category = entry.getKey();
            int iconCount = entry.getValue().size();
            totalIcons += iconCount;
            System.out.println("  - " + category + ": " + iconCount + " 个图标");
        }
        
        System.out.println("总计: " + totalIcons + " 个图标");
        assertTrue(totalIcons > 50, "图标库应该包含至少50个图标");
    }

    @Test
    public void testIconIdValidation() {
        // 测试有效图标ID
        assertTrue(DefaultCategoryConfig.isValidIconId("food"));
        assertTrue(DefaultCategoryConfig.isValidIconId("salary"));
        assertTrue(DefaultCategoryConfig.isValidIconId("cart-o"));
        
        // 测试无效图标ID
        assertFalse(DefaultCategoryConfig.isValidIconId("invalid-icon"));
        assertFalse(DefaultCategoryConfig.isValidIconId(null));
        assertFalse(DefaultCategoryConfig.isValidIconId(""));
    }

    @Test
    public void testIconIdOrDefault() {
        // 有效图标ID应该返回原值
        assertEquals("food", DefaultCategoryConfig.getIconIdOrDefault("food"));
        assertEquals("salary", DefaultCategoryConfig.getIconIdOrDefault("salary"));
        
        // 无效图标ID应该返回默认图标
        assertEquals(DefaultCategoryConfig.DEFAULT_ICON, 
                DefaultCategoryConfig.getIconIdOrDefault("invalid"));
        assertEquals(DefaultCategoryConfig.DEFAULT_ICON, 
                DefaultCategoryConfig.getIconIdOrDefault(null));
        assertEquals(DefaultCategoryConfig.DEFAULT_ICON, 
                DefaultCategoryConfig.getIconIdOrDefault(""));
    }

    @Test
    public void testIconCategoryVOStructure() {
        // 模拟控制器返回的数据结构
        List<IconCategoryVO> result = new ArrayList<>();
        
        for (Map.Entry<String, List<DefaultCategoryConfig.IconInfo>> entry : 
                DefaultCategoryConfig.ICON_LIBRARY.entrySet()) {
            String categoryName = entry.getKey();
            List<DefaultCategoryConfig.IconInfo> iconInfos = entry.getValue();
            
            List<IconVO> icons = new ArrayList<>();
            for (DefaultCategoryConfig.IconInfo iconInfo : iconInfos) {
                icons.add(new IconVO(
                        iconInfo.getId(),
                        iconInfo.getName(),
                        categoryName,
                        iconInfo.getType()
                ));
            }
            
            result.add(new IconCategoryVO(categoryName, icons));
        }
        
        // 验证结构
        assertFalse(result.isEmpty());
        
        // 验证每个分类都有图标
        for (IconCategoryVO category : result) {
            assertNotNull(category.getCategory());
            assertNotNull(category.getIcons());
            assertFalse(category.getIcons().isEmpty());
            
            // 验证每个图标都有必要的字段
            for (IconVO icon : category.getIcons()) {
                assertNotNull(icon.getId());
                assertNotNull(icon.getName());
                assertNotNull(icon.getCategory());
                assertNotNull(icon.getType());
                assertTrue(icon.getType().equals("vant") || icon.getType().equals("custom"));
            }
        }
        
        System.out.println("✓ 图标API数据结构验证通过");
    }

    @Test
    public void testDefaultCategoriesHaveValidIcons() {
        // 验证默认支出分类的图标都是有效的
        for (DefaultCategoryConfig.CategoryTemplate template : 
                DefaultCategoryConfig.DEFAULT_EXPENSE_CATEGORIES) {
            assertTrue(DefaultCategoryConfig.isValidIconId(template.getIcon()),
                    "支出分类 '" + template.getName() + "' 的图标 '" + template.getIcon() + "' 无效");
            
            // 验证子分类图标
            if (template.hasChildren()) {
                for (DefaultCategoryConfig.CategoryTemplate child : template.getChildren()) {
                    assertTrue(DefaultCategoryConfig.isValidIconId(child.getIcon()),
                            "支出子分类 '" + child.getName() + "' 的图标 '" + child.getIcon() + "' 无效");
                }
            }
        }
        
        // 验证默认收入分类的图标都是有效的
        for (DefaultCategoryConfig.CategoryTemplate template : 
                DefaultCategoryConfig.DEFAULT_INCOME_CATEGORIES) {
            assertTrue(DefaultCategoryConfig.isValidIconId(template.getIcon()),
                    "收入分类 '" + template.getName() + "' 的图标 '" + template.getIcon() + "' 无效");
        }
        
        System.out.println("✓ 所有默认分类的图标都是有效的");
    }
}
