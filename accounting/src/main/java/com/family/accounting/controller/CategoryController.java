package com.family.accounting.controller;

import com.family.accounting.config.DefaultCategoryConfig;
import com.family.accounting.dto.*;
import com.family.accounting.security.SecurityUtils;
import com.family.accounting.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 分类管理控制器
 */
@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 获取分类树
     *
     * @param type 类型: 1-支出 2-收入，为空则获取所有
     * @return 分类树列表
     */
    @GetMapping
    public Result<List<CategoryVO>> getCategoryTree(@RequestParam(required = false) Integer type) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<CategoryVO> categories = categoryService.getCategoryTree(userId, type);
        return Result.success(categories);
    }

    /**
     * 获取分类列表（扁平结构）
     *
     * @param type 类型: 1-支出 2-收入，为空则获取所有
     * @return 分类列表
     */
    @GetMapping("/list")
    public Result<List<CategoryVO>> getCategoryList(@RequestParam(required = false) Integer type) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<CategoryVO> categories = categoryService.getCategoryList(userId, type);
        return Result.success(categories);
    }

    /**
     * 获取可用图标列表
     * 返回按分类组织的图标列表，供前端图标选择器使用
     *
     * @return 按分类组织的图标列表
     */
    @GetMapping("/icons")
    public Result<List<IconCategoryVO>> getAvailableIcons() {
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

        return Result.success(result);
    }

    /**
     * 根据ID获取分类详情
     *
     * @param id 分类ID
     * @return 分类信息
     */
    @GetMapping("/{id}")
    public Result<CategoryVO> getCategoryById(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        CategoryVO category = categoryService.getCategoryById(userId, id);
        return Result.success(category);
    }

    /**
     * 创建分类
     *
     * @param dto 分类信息
     * @return 创建的分类
     */
    @PostMapping
    public Result<CategoryVO> createCategory(@Valid @RequestBody CategoryDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        CategoryVO category = categoryService.createCategory(userId, dto);
        return Result.success("创建成功", category);
    }

    /**
     * 更新分类
     *
     * @param id  分类ID
     * @param dto 分类信息
     * @return 更新后的分类
     */
    @PutMapping("/{id}")
    public Result<CategoryVO> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        CategoryVO category = categoryService.updateCategory(userId, id, dto);
        return Result.success("更新成功", category);
    }

    /**
     * 删除分类
     *
     * @param id 分类ID
     * @return 成功响应
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        categoryService.deleteCategory(userId, id);
        return Result.success("删除成功", null);
    }

    /**
     * 检查分类是否可删除
     *
     * @param id 分类ID
     * @return 是否可删除
     */
    @GetMapping("/{id}/can-delete")
    public Result<Boolean> canDeleteCategory(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        boolean canDelete = categoryService.canDeleteCategory(userId, id);
        return Result.success(canDelete);
    }
}
