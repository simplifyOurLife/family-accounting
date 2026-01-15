package com.family.accounting.mapper;

import com.family.accounting.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 分类数据访问接口
 */
@Mapper
public interface CategoryMapper {

    /**
     * 根据ID查询分类
     *
     * @param id 分类ID
     * @return 分类实体
     */
    Category findById(@Param("id") Long id);

    /**
     * 根据家庭ID和类型查询分类列表
     *
     * @param familyId 家庭ID
     * @param type     类型: 1-支出 2-收入
     * @return 分类列表
     */
    List<Category> findByFamilyIdAndType(@Param("familyId") Long familyId, @Param("type") Integer type);

    /**
     * 根据家庭ID查询所有分类
     *
     * @param familyId 家庭ID
     * @return 分类列表
     */
    List<Category> findByFamilyId(@Param("familyId") Long familyId);

    /**
     * 根据父分类ID查询子分类
     *
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    List<Category> findByParentId(@Param("parentId") Long parentId);

    /**
     * 插入新分类
     *
     * @param category 分类实体
     * @return 影响行数
     */
    int insert(Category category);

    /**
     * 更新分类
     *
     * @param category 分类实体
     * @return 影响行数
     */
    int update(Category category);

    /**
     * 删除分类
     *
     * @param id 分类ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 统计分类下的交易数量
     *
     * @param categoryId 分类ID
     * @return 交易数量
     */
    int countTransactionsByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 统计家庭分类数量
     *
     * @param familyId 家庭ID
     * @return 分类数量
     */
    int countByFamilyId(@Param("familyId") Long familyId);
    
    /**
     * 递归查询分类及其所有子分类的ID列表
     *
     * @param categoryId 分类ID
     * @return 分类ID列表（包含自身和所有子分类）
     */
    List<Long> findAllDescendantIds(@Param("categoryId") Long categoryId);
}
