package com.family.accounting.service;

import com.family.accounting.dto.CategoryDTO;
import com.family.accounting.dto.CategoryVO;
import com.family.accounting.entity.Category;
import com.family.accounting.entity.FamilyMember;
import com.family.accounting.mapper.CategoryMapper;
import com.family.accounting.mapper.FamilyMemberMapper;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 分类服务属性测试
 * <p>
 * Feature: family-accounting
 * Property 7: 分类树结构完整性
 * Property 8: 分类删除约束
 * Validates: Requirements 3.1, 3.2, 3.4, 3.5
 */
class CategoryServicePropertyTest {

    /**
     * Property 7: 分类树结构完整性
     * For any category created with a parent reference, the category should appear as a child
     * of that parent in the category tree, and categories without parent should be root nodes.
     * <p>
     * **Validates: Requirements 3.1, 3.2**
     */
    @Property(tries = 100)
    void categoryWithParentShouldAppearAsChildInTree(
            @ForAll @LongRange(min = 1, max = 10000) Long userId,
            @ForAll @LongRange(min = 1, max = 10000) Long familyId,
            @ForAll @LongRange(min = 1, max = 10000) Long parentCategoryId,
            @ForAll @StringLength(min = 1, max = 50) String categoryName,
            @ForAll @IntRange(min = 1, max = 2) int categoryType
    ) {
        // Given: mock mappers
        CategoryMapper categoryMapper = Mockito.mock(CategoryMapper.class);
        FamilyMemberMapper familyMemberMapper = Mockito.mock(FamilyMemberMapper.class);

        CategoryService categoryService = new CategoryService();
        setField(categoryService, "categoryMapper", categoryMapper);
        setField(categoryService, "familyMemberMapper", familyMemberMapper);

        // Setup: user belongs to a family
        FamilyMember member = new FamilyMember();
        member.setFamilyId(familyId);
        member.setUserId(userId);
        when(familyMemberMapper.findByUserId(userId)).thenReturn(member);

        // Setup: parent category exists
        Category parentCategory = new Category();
        parentCategory.setId(parentCategoryId);
        parentCategory.setFamilyId(familyId);
        parentCategory.setParentId(null);
        parentCategory.setName("Parent Category");
        parentCategory.setType(categoryType);
        when(categoryMapper.findById(parentCategoryId)).thenReturn(parentCategory);

        // Setup: capture inserted category
        AtomicLong idCounter = new AtomicLong(parentCategoryId + 1);
        List<Category> insertedCategories = new ArrayList<>();
        doAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            category.setId(idCounter.getAndIncrement());
            insertedCategories.add(category);
            return 1;
        }).when(categoryMapper).insert(any(Category.class));

        // When: creating a category with parent
        CategoryDTO dto = new CategoryDTO();
        dto.setName(categoryName);
        dto.setParentId(parentCategoryId);
        dto.setType(categoryType);

        CategoryVO result = categoryService.createCategory(userId, dto);

        // Then: category should be created with correct parent reference
        assertNotNull(result);
        assertEquals(parentCategoryId, result.getParentId());
        assertEquals(categoryName, result.getName());
        assertEquals(categoryType, result.getType().intValue());

        // Verify the inserted category has correct parent
        assertEquals(1, insertedCategories.size());
        Category inserted = insertedCategories.get(0);
        assertEquals(parentCategoryId, inserted.getParentId());
        assertEquals(familyId, inserted.getFamilyId());
    }


    /**
     * Property 7: 分类树结构完整性 - 根分类测试
     * For any category created without a parent reference, the category should be a root node.
     * <p>
     * **Validates: Requirements 3.1**
     */
    @Property(tries = 100)
    void categoryWithoutParentShouldBeRootNode(
            @ForAll @LongRange(min = 1, max = 10000) Long userId,
            @ForAll @LongRange(min = 1, max = 10000) Long familyId,
            @ForAll @StringLength(min = 1, max = 50) String categoryName,
            @ForAll @IntRange(min = 1, max = 2) int categoryType
    ) {
        // Given: mock mappers
        CategoryMapper categoryMapper = Mockito.mock(CategoryMapper.class);
        FamilyMemberMapper familyMemberMapper = Mockito.mock(FamilyMemberMapper.class);

        CategoryService categoryService = new CategoryService();
        setField(categoryService, "categoryMapper", categoryMapper);
        setField(categoryService, "familyMemberMapper", familyMemberMapper);

        // Setup: user belongs to a family
        FamilyMember member = new FamilyMember();
        member.setFamilyId(familyId);
        member.setUserId(userId);
        when(familyMemberMapper.findByUserId(userId)).thenReturn(member);

        // Setup: capture inserted category
        AtomicLong idCounter = new AtomicLong(1);
        List<Category> insertedCategories = new ArrayList<>();
        doAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            category.setId(idCounter.getAndIncrement());
            insertedCategories.add(category);
            return 1;
        }).when(categoryMapper).insert(any(Category.class));

        // When: creating a category without parent
        CategoryDTO dto = new CategoryDTO();
        dto.setName(categoryName);
        dto.setParentId(null); // No parent - root category
        dto.setType(categoryType);

        CategoryVO result = categoryService.createCategory(userId, dto);

        // Then: category should be created as root node (no parent)
        assertNotNull(result);
        assertNull(result.getParentId());
        assertEquals(categoryName, result.getName());
        assertEquals(categoryType, result.getType().intValue());

        // Verify the inserted category has no parent
        assertEquals(1, insertedCategories.size());
        Category inserted = insertedCategories.get(0);
        assertNull(inserted.getParentId());
        assertEquals(familyId, inserted.getFamilyId());
    }

    /**
     * Property 7: 分类树结构完整性 - 树形结构构建测试
     * For any set of categories with parent-child relationships, the tree structure
     * should correctly reflect these relationships.
     * <p>
     * **Validates: Requirements 3.1, 3.2**
     */
    @Property(tries = 100)
    void categoryTreeShouldReflectParentChildRelationships(
            @ForAll @LongRange(min = 1, max = 10000) Long userId,
            @ForAll @LongRange(min = 1, max = 10000) Long familyId,
            @ForAll @IntRange(min = 1, max = 2) int categoryType
    ) {
        // Given: mock mappers
        CategoryMapper categoryMapper = Mockito.mock(CategoryMapper.class);
        FamilyMemberMapper familyMemberMapper = Mockito.mock(FamilyMemberMapper.class);

        CategoryService categoryService = new CategoryService();
        setField(categoryService, "categoryMapper", categoryMapper);
        setField(categoryService, "familyMemberMapper", familyMemberMapper);

        // Setup: user belongs to a family
        FamilyMember member = new FamilyMember();
        member.setFamilyId(familyId);
        member.setUserId(userId);
        when(familyMemberMapper.findByUserId(userId)).thenReturn(member);

        // Setup: create a parent category and two child categories
        Category parentCategory = new Category();
        parentCategory.setId(1L);
        parentCategory.setFamilyId(familyId);
        parentCategory.setParentId(null);
        parentCategory.setName("Parent");
        parentCategory.setType(categoryType);

        Category childCategory1 = new Category();
        childCategory1.setId(2L);
        childCategory1.setFamilyId(familyId);
        childCategory1.setParentId(1L);
        childCategory1.setName("Child1");
        childCategory1.setType(categoryType);

        Category childCategory2 = new Category();
        childCategory2.setId(3L);
        childCategory2.setFamilyId(familyId);
        childCategory2.setParentId(1L);
        childCategory2.setName("Child2");
        childCategory2.setType(categoryType);

        List<Category> allCategories = new ArrayList<>();
        allCategories.add(parentCategory);
        allCategories.add(childCategory1);
        allCategories.add(childCategory2);

        when(categoryMapper.findByFamilyIdAndType(familyId, categoryType)).thenReturn(allCategories);
        when(categoryMapper.countTransactionsByCategoryId(any())).thenReturn(0);

        // When: getting category tree
        List<CategoryVO> tree = categoryService.getCategoryTree(userId, categoryType);

        // Then: tree should have one root node
        assertEquals(1, tree.size());
        CategoryVO root = tree.get(0);
        assertEquals("Parent", root.getName());
        assertNull(root.getParentId());

        // And: root should have two children
        assertNotNull(root.getChildren());
        assertEquals(2, root.getChildren().size());
        assertTrue(root.getHasChildren());

        // And: children should have correct parent reference
        for (CategoryVO child : root.getChildren()) {
            assertEquals(1L, child.getParentId());
        }
    }

    /**
     * 使用反射设置私有字段
     */
    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }
}


/**
 * 分类删除约束属性测试
 * <p>
 * Feature: family-accounting
 * Property 8: 分类删除约束
 * Validates: Requirements 3.4, 3.5
 */
class CategoryDeletionPropertyTest {

    /**
     * Property 8: 分类删除约束 - 有交易记录的分类不能删除
     * For any category with associated transactions, deletion should be rejected.
     * <p>
     * **Validates: Requirements 3.5**
     */
    @Property(tries = 100)
    void categoryWithTransactionsShouldNotBeDeleted(
            @ForAll @LongRange(min = 1, max = 10000) Long userId,
            @ForAll @LongRange(min = 1, max = 10000) Long familyId,
            @ForAll @LongRange(min = 1, max = 10000) Long categoryId,
            @ForAll @IntRange(min = 1, max = 100) int transactionCount
    ) {
        // Given: mock mappers
        CategoryMapper categoryMapper = Mockito.mock(CategoryMapper.class);
        FamilyMemberMapper familyMemberMapper = Mockito.mock(FamilyMemberMapper.class);

        CategoryService categoryService = new CategoryService();
        setField(categoryService, "categoryMapper", categoryMapper);
        setField(categoryService, "familyMemberMapper", familyMemberMapper);

        // Setup: user belongs to a family
        FamilyMember member = new FamilyMember();
        member.setFamilyId(familyId);
        member.setUserId(userId);
        when(familyMemberMapper.findByUserId(userId)).thenReturn(member);

        // Setup: category exists and belongs to the family
        Category category = new Category();
        category.setId(categoryId);
        category.setFamilyId(familyId);
        category.setName("Test Category");
        category.setType(Category.TYPE_EXPENSE);
        when(categoryMapper.findById(categoryId)).thenReturn(category);

        // Setup: category has transactions
        when(categoryMapper.countTransactionsByCategoryId(categoryId)).thenReturn(transactionCount);

        // When/Then: attempting to delete should throw exception
        com.family.accounting.exception.BusinessException exception =
                assertThrows(com.family.accounting.exception.BusinessException.class,
                        () -> categoryService.deleteCategory(userId, categoryId));

        // And: error message should indicate transactions exist
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("交易") || exception.getMessage().contains("记录"));

        // And: category should NOT be deleted
        verify(categoryMapper, never()).deleteById(any());
    }

    /**
     * Property 8: 分类删除约束 - 没有交易记录的分类可以删除
     * For categories without transactions, deletion should succeed and remove the category.
     * <p>
     * **Validates: Requirements 3.4**
     */
    @Property(tries = 100)
    void categoryWithoutTransactionsShouldBeDeleted(
            @ForAll @LongRange(min = 1, max = 10000) Long userId,
            @ForAll @LongRange(min = 1, max = 10000) Long familyId,
            @ForAll @LongRange(min = 1, max = 10000) Long categoryId
    ) {
        // Given: mock mappers
        CategoryMapper categoryMapper = Mockito.mock(CategoryMapper.class);
        FamilyMemberMapper familyMemberMapper = Mockito.mock(FamilyMemberMapper.class);

        CategoryService categoryService = new CategoryService();
        setField(categoryService, "categoryMapper", categoryMapper);
        setField(categoryService, "familyMemberMapper", familyMemberMapper);

        // Setup: user belongs to a family
        FamilyMember member = new FamilyMember();
        member.setFamilyId(familyId);
        member.setUserId(userId);
        when(familyMemberMapper.findByUserId(userId)).thenReturn(member);

        // Setup: category exists and belongs to the family
        Category category = new Category();
        category.setId(categoryId);
        category.setFamilyId(familyId);
        category.setName("Test Category");
        category.setType(Category.TYPE_EXPENSE);
        when(categoryMapper.findById(categoryId)).thenReturn(category);

        // Setup: category has NO transactions
        when(categoryMapper.countTransactionsByCategoryId(categoryId)).thenReturn(0);

        // Setup: category has NO children
        when(categoryMapper.findByParentId(categoryId)).thenReturn(new ArrayList<>());

        // When: deleting the category
        categoryService.deleteCategory(userId, categoryId);

        // Then: category should be deleted
        verify(categoryMapper).deleteById(categoryId);
    }

    /**
     * Property 8: 分类删除约束 - 有子分类的分类不能删除
     * For any category with child categories, deletion should be rejected.
     * <p>
     * **Validates: Requirements 3.4, 3.5**
     */
    @Property(tries = 100)
    void categoryWithChildrenShouldNotBeDeleted(
            @ForAll @LongRange(min = 1, max = 10000) Long userId,
            @ForAll @LongRange(min = 1, max = 10000) Long familyId,
            @ForAll @LongRange(min = 1, max = 10000) Long categoryId,
            @ForAll @IntRange(min = 1, max = 5) int childCount
    ) {
        // Given: mock mappers
        CategoryMapper categoryMapper = Mockito.mock(CategoryMapper.class);
        FamilyMemberMapper familyMemberMapper = Mockito.mock(FamilyMemberMapper.class);

        CategoryService categoryService = new CategoryService();
        setField(categoryService, "categoryMapper", categoryMapper);
        setField(categoryService, "familyMemberMapper", familyMemberMapper);

        // Setup: user belongs to a family
        FamilyMember member = new FamilyMember();
        member.setFamilyId(familyId);
        member.setUserId(userId);
        when(familyMemberMapper.findByUserId(userId)).thenReturn(member);

        // Setup: category exists and belongs to the family
        Category category = new Category();
        category.setId(categoryId);
        category.setFamilyId(familyId);
        category.setName("Parent Category");
        category.setType(Category.TYPE_EXPENSE);
        when(categoryMapper.findById(categoryId)).thenReturn(category);

        // Setup: category has NO transactions
        when(categoryMapper.countTransactionsByCategoryId(categoryId)).thenReturn(0);

        // Setup: category has children
        List<Category> children = new ArrayList<>();
        for (int i = 0; i < childCount; i++) {
            Category child = new Category();
            child.setId(categoryId + i + 1);
            child.setParentId(categoryId);
            child.setName("Child " + i);
            children.add(child);
        }
        when(categoryMapper.findByParentId(categoryId)).thenReturn(children);

        // When/Then: attempting to delete should throw exception
        com.family.accounting.exception.BusinessException exception =
                assertThrows(com.family.accounting.exception.BusinessException.class,
                        () -> categoryService.deleteCategory(userId, categoryId));

        // And: error message should indicate children exist
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("子分类"));

        // And: category should NOT be deleted
        verify(categoryMapper, never()).deleteById(any());
    }

    /**
     * 使用反射设置私有字段
     */
    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }
}
