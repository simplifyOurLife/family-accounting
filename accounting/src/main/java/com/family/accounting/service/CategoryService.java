package com.family.accounting.service;

import com.family.accounting.config.DefaultCategoryConfig;
import com.family.accounting.dto.CategoryDTO;
import com.family.accounting.dto.CategoryVO;
import com.family.accounting.entity.Category;
import com.family.accounting.entity.FamilyMember;
import com.family.accounting.exception.BusinessException;
import com.family.accounting.mapper.CategoryMapper;
import com.family.accounting.mapper.FamilyMemberMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 分类服务类
 */
@Service
public class CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private FamilyMemberMapper familyMemberMapper;

    /**
     * 获取分类树
     *
     * @param userId 用户ID
     * @param type   类型: 1-支出 2-收入，为空则获取所有
     * @return 分类树列表
     */
    public List<CategoryVO> getCategoryTree(Long userId, Integer type) {
        Long familyId = getFamilyIdByUser(userId);

        List<Category> categories;
        if (type != null) {
            categories = categoryMapper.findByFamilyIdAndType(familyId, type);
        } else {
            categories = categoryMapper.findByFamilyId(familyId);
        }

        return buildCategoryTree(categories);
    }

    /**
     * 获取所有分类（扁平列表）
     *
     * @param userId 用户ID
     * @param type   类型
     * @return 分类列表
     */
    public List<CategoryVO> getCategoryList(Long userId, Integer type) {
        Long familyId = getFamilyIdByUser(userId);

        List<Category> categories;
        if (type != null) {
            categories = categoryMapper.findByFamilyIdAndType(familyId, type);
        } else {
            categories = categoryMapper.findByFamilyId(familyId);
        }

        return categories.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取分类
     *
     * @param userId     用户ID
     * @param categoryId 分类ID
     * @return 分类信息
     */
    public CategoryVO getCategoryById(Long userId, Long categoryId) {
        Long familyId = getFamilyIdByUser(userId);

        Category category = categoryMapper.findById(categoryId);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }

        // 验证分类属于用户的家庭
        if (!category.getFamilyId().equals(familyId)) {
            throw new BusinessException("无权访问此分类");
        }

        return convertToVO(category);
    }

    /**
     * 创建分类
     *
     * @param userId 用户ID
     * @param dto    分类信息
     * @return 创建的分类
     */
    @Transactional
    public CategoryVO createCategory(Long userId, CategoryDTO dto) {
        Long familyId = getFamilyIdByUser(userId);

        // 验证类型
        if (dto.getType() != Category.TYPE_EXPENSE && dto.getType() != Category.TYPE_INCOME) {
            throw new BusinessException("无效的分类类型");
        }

        // 如果有父分类，验证父分类存在且属于同一家庭
        if (dto.getParentId() != null) {
            Category parent = categoryMapper.findById(dto.getParentId());
            if (parent == null) {
                throw new BusinessException("父分类不存在");
            }
            if (!parent.getFamilyId().equals(familyId)) {
                throw new BusinessException("无权在此父分类下创建子分类");
            }
            // 子分类类型必须与父分类一致
            if (!parent.getType().equals(dto.getType())) {
                throw new BusinessException("子分类类型必须与父分类一致");
            }
        }

        // 处理图标ID - 如果提供了图标ID则验证有效性，否则使用默认图标
        String iconId = dto.getIcon();
        if (iconId != null && !iconId.trim().isEmpty()) {
            // 验证图标ID有效性，如果无效则使用默认图标
            iconId = DefaultCategoryConfig.getIconIdOrDefault(iconId);
        } else {
            iconId = DefaultCategoryConfig.DEFAULT_ICON;
        }

        Category category = new Category();
        category.setFamilyId(familyId);
        category.setParentId(dto.getParentId());
        category.setName(dto.getName());
        category.setType(dto.getType());
        category.setIcon(iconId);
        category.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);

        categoryMapper.insert(category);

        return convertToVO(category);
    }


    /**
     * 更新分类
     *
     * @param userId     用户ID
     * @param categoryId 分类ID
     * @param dto        分类信息
     * @return 更新后的分类
     */
    @Transactional
    public CategoryVO updateCategory(Long userId, Long categoryId, CategoryDTO dto) {
        Long familyId = getFamilyIdByUser(userId);

        Category category = categoryMapper.findById(categoryId);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }

        // 验证分类属于用户的家庭
        if (!category.getFamilyId().equals(familyId)) {
            throw new BusinessException("无权修改此分类");
        }

        // 更新字段
        if (dto.getName() != null) {
            category.setName(dto.getName());
        }
        if (dto.getIcon() != null) {
            // 验证图标ID有效性，如果无效则使用默认图标
            String iconId = DefaultCategoryConfig.getIconIdOrDefault(dto.getIcon());
            category.setIcon(iconId);
        }
        if (dto.getSortOrder() != null) {
            category.setSortOrder(dto.getSortOrder());
        }

        categoryMapper.update(category);

        return convertToVO(category);
    }

    /**
     * 删除分类
     *
     * @param userId     用户ID
     * @param categoryId 分类ID
     */
    @Transactional
    public void deleteCategory(Long userId, Long categoryId) {
        Long familyId = getFamilyIdByUser(userId);

        Category category = categoryMapper.findById(categoryId);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }

        // 验证分类属于用户的家庭
        if (!category.getFamilyId().equals(familyId)) {
            throw new BusinessException("无权删除此分类");
        }

        // 检查是否有关联交易
        int transactionCount = categoryMapper.countTransactionsByCategoryId(categoryId);
        if (transactionCount > 0) {
            throw new BusinessException("该分类下有" + transactionCount + "笔交易记录，无法删除");
        }

        // 检查是否有子分类
        List<Category> children = categoryMapper.findByParentId(categoryId);
        if (children != null && !children.isEmpty()) {
            throw new BusinessException("该分类下有子分类，请先删除子分类");
        }

        categoryMapper.deleteById(categoryId);
    }

    /**
     * 检查分类是否可删除
     *
     * @param userId     用户ID
     * @param categoryId 分类ID
     * @return true-可删除，false-不可删除
     */
    public boolean canDeleteCategory(Long userId, Long categoryId) {
        Long familyId = getFamilyIdByUser(userId);

        Category category = categoryMapper.findById(categoryId);
        if (category == null || !category.getFamilyId().equals(familyId)) {
            return false;
        }

        // 检查是否有关联交易
        int transactionCount = categoryMapper.countTransactionsByCategoryId(categoryId);
        if (transactionCount > 0) {
            return false;
        }

        // 检查是否有子分类
        List<Category> children = categoryMapper.findByParentId(categoryId);
        return children == null || children.isEmpty();
    }

    /**
     * 获取用户所属家庭ID
     */
    private Long getFamilyIdByUser(Long userId) {
        FamilyMember member = familyMemberMapper.findByUserId(userId);
        if (member == null) {
            throw new BusinessException("您还没有加入任何家庭");
        }
        return member.getFamilyId();
    }

    /**
     * 构建分类树
     */
    private List<CategoryVO> buildCategoryTree(List<Category> categories) {
        // 转换为VO
        List<CategoryVO> voList = categories.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 按parentId分组
        Map<Long, List<CategoryVO>> childrenMap = voList.stream()
                .filter(vo -> vo.getParentId() != null)
                .collect(Collectors.groupingBy(CategoryVO::getParentId));

        // 设置子分类
        for (CategoryVO vo : voList) {
            List<CategoryVO> children = childrenMap.get(vo.getId());
            vo.setChildren(children != null ? children : new ArrayList<>());
            vo.setHasChildren(children != null && !children.isEmpty());
        }

        // 返回根分类（parentId为null的）
        return voList.stream()
                .filter(vo -> vo.getParentId() == null)
                .collect(Collectors.toList());
    }

    /**
     * 将Category实体转换为CategoryVO
     */
    private CategoryVO convertToVO(Category category) {
        CategoryVO vo = new CategoryVO();
        vo.setId(category.getId());
        vo.setFamilyId(category.getFamilyId());
        vo.setParentId(category.getParentId());
        vo.setName(category.getName());
        vo.setType(category.getType());
        vo.setTypeText(category.getType() == Category.TYPE_EXPENSE ? "支出" : "收入");
        // 使用默认图标回退机制
        vo.setIcon(DefaultCategoryConfig.getIconIdOrDefault(category.getIcon()));
        vo.setSortOrder(category.getSortOrder());
        vo.setCreatedAt(category.getCreatedAt());

        // 获取关联交易数量
        int transactionCount = categoryMapper.countTransactionsByCategoryId(category.getId());
        vo.setTransactionCount(transactionCount);

        return vo;
    }
}
