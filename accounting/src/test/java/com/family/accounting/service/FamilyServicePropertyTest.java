package com.family.accounting.service;

import com.family.accounting.config.DefaultCategoryConfig;
import com.family.accounting.dto.FamilyDTO;
import com.family.accounting.dto.FamilyVO;
import com.family.accounting.entity.*;
import com.family.accounting.exception.BusinessException;
import com.family.accounting.mapper.*;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 家庭服务属性测试
 * <p>
 * Feature: family-accounting
 * Property 3: 家庭创建者成为管理员
 * Property 5: 家庭成员唯一性约束
 * Property 6: 成员移除完整性
 * Property 9: 新家庭默认数据初始化
 * Validates: Requirements 2.1, 2.6, 2.7, 3.6, 4.5
 */
class FamilyServicePropertyTest {

    /**
     * Property 3: 家庭创建者成为管理员
     * For any user creating a family with a valid name, the creator should automatically
     * become the family admin and be added as a family member.
     * <p>
     * **Validates: Requirements 2.1**
     */
    @Property(tries = 100)
    void createFamilyShouldMakeCreatorAdminAndMember(
            @ForAll @LongRange(min = 1, max = 10000) Long userId,
            @ForAll @StringLength(min = 1, max = 50) String familyName
    ) {
        // Given: mock mappers
        FamilyMapper familyMapper = Mockito.mock(FamilyMapper.class);
        FamilyMemberMapper familyMemberMapper = Mockito.mock(FamilyMemberMapper.class);
        CategoryMapper categoryMapper = Mockito.mock(CategoryMapper.class);
        AccountBookMapper accountBookMapper = Mockito.mock(AccountBookMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);

        FamilyService familyService = new FamilyService();
        setField(familyService, "familyMapper", familyMapper);
        setField(familyService, "familyMemberMapper", familyMemberMapper);
        setField(familyService, "categoryMapper", categoryMapper);
        setField(familyService, "accountBookMapper", accountBookMapper);
        setField(familyService, "userMapper", userMapper);

        // Setup: user has no existing family
        when(familyMemberMapper.findByUserId(userId)).thenReturn(null);
        when(familyMemberMapper.countByFamilyId(any())).thenReturn(1);

        // Setup: user exists
        User user = new User();
        user.setId(userId);
        user.setPhone("13800138000");
        user.setNickname("TestUser");
        when(userMapper.findById(userId)).thenReturn(user);

        // Capture family and member inserts
        AtomicLong familyIdCounter = new AtomicLong(1);
        doAnswer(invocation -> {
            Family family = invocation.getArgument(0);
            family.setId(familyIdCounter.getAndIncrement());
            return 1;
        }).when(familyMapper).insert(any(Family.class));

        doAnswer(invocation -> {
            FamilyMember member = invocation.getArgument(0);
            member.setId(1L);
            return 1;
        }).when(familyMemberMapper).insert(any(FamilyMember.class));

        doAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            category.setId(System.nanoTime());
            return 1;
        }).when(categoryMapper).insert(any(Category.class));

        doAnswer(invocation -> {
            AccountBook book = invocation.getArgument(0);
            book.setId(1L);
            return 1;
        }).when(accountBookMapper).insert(any(AccountBook.class));

        // When: creating a family
        FamilyDTO dto = new FamilyDTO();
        dto.setName(familyName);

        FamilyVO result = familyService.createFamily(userId, dto);

        // Then: the family should be created with the user as admin
        assertNotNull(result);
        assertEquals(familyName, result.getName());
        assertEquals(userId, result.getAdminId());

        // Verify family was created with correct admin
        ArgumentCaptor<Family> familyCaptor = ArgumentCaptor.forClass(Family.class);
        verify(familyMapper).insert(familyCaptor.capture());
        assertEquals(userId, familyCaptor.getValue().getAdminId());
        assertEquals(familyName, familyCaptor.getValue().getName());

        // Verify member was added with correct user and family
        ArgumentCaptor<FamilyMember> memberCaptor = ArgumentCaptor.forClass(FamilyMember.class);
        verify(familyMemberMapper).insert(memberCaptor.capture());
        assertEquals(userId, memberCaptor.getValue().getUserId());
        assertNotNull(memberCaptor.getValue().getFamilyId());
    }

    /**
     * Property 9: 新家庭默认数据初始化
     * For any newly created family, the system should automatically create default
     * categories (income and expense types) and a default account book.
     * <p>
     * **Validates: Requirements 3.6, 4.5**
     */
    @Property(tries = 100)
    void createFamilyShouldInitializeDefaultData(
            @ForAll @LongRange(min = 1, max = 10000) Long userId,
            @ForAll @StringLength(min = 1, max = 50) String familyName
    ) {
        // Given: mock mappers
        FamilyMapper familyMapper = Mockito.mock(FamilyMapper.class);
        FamilyMemberMapper familyMemberMapper = Mockito.mock(FamilyMemberMapper.class);
        CategoryMapper categoryMapper = Mockito.mock(CategoryMapper.class);
        AccountBookMapper accountBookMapper = Mockito.mock(AccountBookMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);

        FamilyService familyService = new FamilyService();
        setField(familyService, "familyMapper", familyMapper);
        setField(familyService, "familyMemberMapper", familyMemberMapper);
        setField(familyService, "categoryMapper", categoryMapper);
        setField(familyService, "accountBookMapper", accountBookMapper);
        setField(familyService, "userMapper", userMapper);

        // Setup: user has no existing family
        when(familyMemberMapper.findByUserId(userId)).thenReturn(null);
        when(familyMemberMapper.countByFamilyId(any())).thenReturn(1);

        // Setup: user exists
        User user = new User();
        user.setId(userId);
        user.setPhone("13800138000");
        when(userMapper.findById(userId)).thenReturn(user);

        // Capture all inserts
        AtomicLong idCounter = new AtomicLong(1);
        doAnswer(invocation -> {
            Family family = invocation.getArgument(0);
            family.setId(idCounter.getAndIncrement());
            return 1;
        }).when(familyMapper).insert(any(Family.class));

        doAnswer(invocation -> {
            FamilyMember member = invocation.getArgument(0);
            member.setId(idCounter.getAndIncrement());
            return 1;
        }).when(familyMemberMapper).insert(any(FamilyMember.class));

        List<Category> insertedCategories = new ArrayList<>();
        doAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            category.setId(idCounter.getAndIncrement());
            insertedCategories.add(category);
            return 1;
        }).when(categoryMapper).insert(any(Category.class));

        List<AccountBook> insertedBooks = new ArrayList<>();
        doAnswer(invocation -> {
            AccountBook book = invocation.getArgument(0);
            book.setId(idCounter.getAndIncrement());
            insertedBooks.add(book);
            return 1;
        }).when(accountBookMapper).insert(any(AccountBook.class));

        // When: creating a family
        FamilyDTO dto = new FamilyDTO();
        dto.setName(familyName);

        familyService.createFamily(userId, dto);

        // Then: default categories should be created
        assertFalse(insertedCategories.isEmpty(), "Default categories should be created");

        // Verify expense categories exist
        long expenseCount = insertedCategories.stream()
                .filter(c -> c.getType() == Category.TYPE_EXPENSE)
                .count();
        assertTrue(expenseCount > 0, "Expense categories should be created");

        // Verify income categories exist
        long incomeCount = insertedCategories.stream()
                .filter(c -> c.getType() == Category.TYPE_INCOME)
                .count();
        assertTrue(incomeCount > 0, "Income categories should be created");

        // Then: default account book should be created
        assertEquals(1, insertedBooks.size(), "One default account book should be created");
        AccountBook defaultBook = insertedBooks.get(0);
        assertEquals(1, defaultBook.getIsDefault().intValue(), "Account book should be marked as default");
        assertNotNull(defaultBook.getName(), "Account book should have a name");
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

    /**
     * Property 5: 家庭成员唯一性约束
     * For any user who is already a member of a family, attempting to create a new family
     * should be rejected.
     * <p>
     * **Validates: Requirements 2.6**
     */
    @Property(tries = 100)
    void createFamilyShouldRejectUserAlreadyInFamily(
            @ForAll @LongRange(min = 1, max = 10000) Long userId,
            @ForAll @LongRange(min = 1, max = 10000) Long existingFamilyId,
            @ForAll @StringLength(min = 1, max = 50) String newFamilyName
    ) {
        // Given: mock mappers
        FamilyMapper familyMapper = Mockito.mock(FamilyMapper.class);
        FamilyMemberMapper familyMemberMapper = Mockito.mock(FamilyMemberMapper.class);
        CategoryMapper categoryMapper = Mockito.mock(CategoryMapper.class);
        AccountBookMapper accountBookMapper = Mockito.mock(AccountBookMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);

        FamilyService familyService = new FamilyService();
        setField(familyService, "familyMapper", familyMapper);
        setField(familyService, "familyMemberMapper", familyMemberMapper);
        setField(familyService, "categoryMapper", categoryMapper);
        setField(familyService, "accountBookMapper", accountBookMapper);
        setField(familyService, "userMapper", userMapper);

        // Setup: user already has a family
        FamilyMember existingMember = new FamilyMember();
        existingMember.setId(1L);
        existingMember.setFamilyId(existingFamilyId);
        existingMember.setUserId(userId);
        when(familyMemberMapper.findByUserId(userId)).thenReturn(existingMember);

        // When: attempting to create a new family
        FamilyDTO dto = new FamilyDTO();
        dto.setName(newFamilyName);

        // Then: should throw BusinessException
        BusinessException exception = assertThrows(BusinessException.class,
                () -> familyService.createFamily(userId, dto));

        // And: the error message should indicate the user already has a family
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("已经是") || exception.getMessage().contains("成员"));

        // And: no family should be created
        verify(familyMapper, never()).insert(any(Family.class));

        // And: no member should be added
        verify(familyMemberMapper, never()).insert(any(FamilyMember.class));
    }

    /**
     * Property 6: 成员移除完整性
     * For any family member removed by admin, the member should no longer appear
     * in the family members list.
     * <p>
     * **Validates: Requirements 2.7**
     */
    @Property(tries = 100)
    void removeMemberShouldDeleteFromFamily(
            @ForAll @LongRange(min = 1, max = 10000) Long adminId,
            @ForAll @LongRange(min = 1, max = 10000) Long memberId,
            @ForAll @LongRange(min = 1, max = 10000) Long memberUserId,
            @ForAll @LongRange(min = 1, max = 10000) Long familyId
    ) {
        // Ensure admin and member are different users
        Assume.that(!adminId.equals(memberUserId));

        // Given: mock mappers
        FamilyMapper familyMapper = Mockito.mock(FamilyMapper.class);
        FamilyMemberMapper familyMemberMapper = Mockito.mock(FamilyMemberMapper.class);
        CategoryMapper categoryMapper = Mockito.mock(CategoryMapper.class);
        AccountBookMapper accountBookMapper = Mockito.mock(AccountBookMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        InvitationMapper invitationMapper = Mockito.mock(InvitationMapper.class);

        FamilyService familyService = new FamilyService();
        setField(familyService, "familyMapper", familyMapper);
        setField(familyService, "familyMemberMapper", familyMemberMapper);
        setField(familyService, "categoryMapper", categoryMapper);
        setField(familyService, "accountBookMapper", accountBookMapper);
        setField(familyService, "userMapper", userMapper);
        setField(familyService, "invitationMapper", invitationMapper);

        // Setup: admin is the family admin
        Family family = new Family();
        family.setId(familyId);
        family.setAdminId(adminId);
        when(familyMapper.findByAdminId(adminId)).thenReturn(family);

        // Setup: member exists in the family
        FamilyMember member = new FamilyMember();
        member.setId(memberId);
        member.setFamilyId(familyId);
        member.setUserId(memberUserId);
        when(familyMemberMapper.findById(memberId)).thenReturn(member);

        // When: removing the member
        familyService.removeMember(adminId, memberId);

        // Then: member should be deleted
        verify(familyMemberMapper).deleteById(memberId);
    }

    /**
     * Property 6: 成员移除完整性 - 管理员不能移除自己
     * For any family admin, attempting to remove themselves should be rejected.
     * <p>
     * **Validates: Requirements 2.7**
     */
    @Property(tries = 100)
    void removeMemberShouldNotAllowRemovingAdmin(
            @ForAll @LongRange(min = 1, max = 10000) Long adminId,
            @ForAll @LongRange(min = 1, max = 10000) Long memberId,
            @ForAll @LongRange(min = 1, max = 10000) Long familyId
    ) {
        // Given: mock mappers
        FamilyMapper familyMapper = Mockito.mock(FamilyMapper.class);
        FamilyMemberMapper familyMemberMapper = Mockito.mock(FamilyMemberMapper.class);
        CategoryMapper categoryMapper = Mockito.mock(CategoryMapper.class);
        AccountBookMapper accountBookMapper = Mockito.mock(AccountBookMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        InvitationMapper invitationMapper = Mockito.mock(InvitationMapper.class);

        FamilyService familyService = new FamilyService();
        setField(familyService, "familyMapper", familyMapper);
        setField(familyService, "familyMemberMapper", familyMemberMapper);
        setField(familyService, "categoryMapper", categoryMapper);
        setField(familyService, "accountBookMapper", accountBookMapper);
        setField(familyService, "userMapper", userMapper);
        setField(familyService, "invitationMapper", invitationMapper);

        // Setup: admin is the family admin
        Family family = new Family();
        family.setId(familyId);
        family.setAdminId(adminId);
        when(familyMapper.findByAdminId(adminId)).thenReturn(family);

        // Setup: member is the admin themselves
        FamilyMember member = new FamilyMember();
        member.setId(memberId);
        member.setFamilyId(familyId);
        member.setUserId(adminId); // Same as admin
        when(familyMemberMapper.findById(memberId)).thenReturn(member);

        // When/Then: attempting to remove admin should throw exception
        BusinessException exception = assertThrows(BusinessException.class,
                () -> familyService.removeMember(adminId, memberId));

        // And: error message should indicate admin cannot be removed
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("管理员"));

        // And: member should NOT be deleted
        verify(familyMemberMapper, never()).deleteById(any());
    }
}
