package com.family.accounting.service;

import com.family.accounting.dto.TransactionDTO;
import com.family.accounting.dto.TransactionVO;
import com.family.accounting.entity.*;
import com.family.accounting.mapper.*;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 交易记录服务属性测试
 * <p>
 * Feature: family-accounting, Property 10: 交易记录持久化完整性
 * Validates: Requirements 5.1, 5.3, 5.6
 */
class TransactionServicePropertyTest {

    /**
     * Property 10: 交易记录持久化完整性
     * For any transaction created with amount, category, date, and optional note,
     * all fields including creator and creation time should be persisted and retrievable.
     */
    @Property(tries = 100)
    void createTransactionShouldPersistAllFields(
            @ForAll @BigRange(min = "0.01", max = "999999.99") BigDecimal amount,
            @ForAll("validTransactionTypes") int type,
            @ForAll("validDates") LocalDate transactionDate,
            @ForAll @StringLength(max = 200) String note
    ) {
        // Given: mock dependencies
        TransactionMapper mockTransactionMapper = Mockito.mock(TransactionMapper.class);
        AccountBookMapper mockAccountBookMapper = Mockito.mock(AccountBookMapper.class);
        CategoryMapper mockCategoryMapper = Mockito.mock(CategoryMapper.class);
        FamilyMemberMapper mockFamilyMemberMapper = Mockito.mock(FamilyMemberMapper.class);
        UserMapper mockUserMapper = Mockito.mock(UserMapper.class);

        TransactionService service = new TransactionService();
        setField(service, "transactionMapper", mockTransactionMapper);
        setField(service, "accountBookMapper", mockAccountBookMapper);
        setField(service, "categoryMapper", mockCategoryMapper);
        setField(service, "familyMemberMapper", mockFamilyMemberMapper);
        setField(service, "userMapper", mockUserMapper);

        Long userId = 1L;
        Long familyId = 100L;
        Long accountBookId = 200L;
        Long categoryId = 300L;

        // Setup: user belongs to a family
        FamilyMember member = new FamilyMember();
        member.setUserId(userId);
        member.setFamilyId(familyId);
        when(mockFamilyMemberMapper.findByUserId(userId)).thenReturn(member);

        // Setup: default account book exists
        AccountBook accountBook = new AccountBook();
        accountBook.setId(accountBookId);
        accountBook.setFamilyId(familyId);
        accountBook.setName("默认账本");
        when(mockAccountBookMapper.findDefaultByFamilyId(familyId)).thenReturn(accountBook);
        when(mockAccountBookMapper.findById(accountBookId)).thenReturn(accountBook);

        // Setup: category exists with matching type
        Category category = new Category();
        category.setId(categoryId);
        category.setFamilyId(familyId);
        category.setType(type);
        category.setName("测试分类");
        when(mockCategoryMapper.findById(categoryId)).thenReturn(category);

        // Setup: user exists
        User user = new User();
        user.setId(userId);
        user.setNickname("测试用户");
        when(mockUserMapper.findById(userId)).thenReturn(user);

        // Capture the inserted transaction
        final Transaction[] capturedTransaction = new Transaction[1];
        doAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId(1L);
            t.setCreatedAt(LocalDateTime.now());
            t.setUpdatedAt(LocalDateTime.now());
            capturedTransaction[0] = t;
            return 1;
        }).when(mockTransactionMapper).insert(any(Transaction.class));

        // When: creating a transaction
        TransactionDTO dto = new TransactionDTO();
        dto.setCategoryId(categoryId);
        dto.setType(type);
        dto.setAmount(amount);
        dto.setNote(note);
        dto.setTransactionDate(transactionDate);

        TransactionVO result = service.createTransaction(userId, dto);

        // Then: all fields should be persisted correctly
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(accountBookId, result.getAccountBookId());
        assertEquals(categoryId, result.getCategoryId());
        assertEquals(userId, result.getUserId());
        assertEquals(type, result.getType());
        assertEquals(0, amount.compareTo(result.getAmount()));
        assertEquals(note, result.getNote());
        assertEquals(transactionDate, result.getTransactionDate());
        assertNotNull(result.getCreatedAt());

        // Verify the transaction was inserted with correct values
        verify(mockTransactionMapper).insert(argThat(t ->
                t.getAccountBookId().equals(accountBookId) &&
                        t.getCategoryId().equals(categoryId) &&
                        t.getUserId().equals(userId) &&
                        t.getType().equals(type) &&
                        t.getAmount().compareTo(amount) == 0 &&
                        (note == null ? t.getNote() == null : note.equals(t.getNote())) &&
                        t.getTransactionDate().equals(transactionDate)
        ));
    }


    /**
     * Property 10: 交易记录持久化完整性 - 创建人记录
     * For any transaction, the creator (userId) should be recorded and retrievable.
     */
    @Property(tries = 100)
    void createTransactionShouldRecordCreator(
            @ForAll @LongRange(min = 1, max = 10000) long userId,
            @ForAll @BigRange(min = "0.01", max = "999999.99") BigDecimal amount
    ) {
        // Given: mock dependencies
        TransactionMapper mockTransactionMapper = Mockito.mock(TransactionMapper.class);
        AccountBookMapper mockAccountBookMapper = Mockito.mock(AccountBookMapper.class);
        CategoryMapper mockCategoryMapper = Mockito.mock(CategoryMapper.class);
        FamilyMemberMapper mockFamilyMemberMapper = Mockito.mock(FamilyMemberMapper.class);
        UserMapper mockUserMapper = Mockito.mock(UserMapper.class);

        TransactionService service = new TransactionService();
        setField(service, "transactionMapper", mockTransactionMapper);
        setField(service, "accountBookMapper", mockAccountBookMapper);
        setField(service, "categoryMapper", mockCategoryMapper);
        setField(service, "familyMemberMapper", mockFamilyMemberMapper);
        setField(service, "userMapper", mockUserMapper);

        Long familyId = 100L;
        Long accountBookId = 200L;
        Long categoryId = 300L;

        // Setup: user belongs to a family
        FamilyMember member = new FamilyMember();
        member.setUserId(userId);
        member.setFamilyId(familyId);
        when(mockFamilyMemberMapper.findByUserId(userId)).thenReturn(member);

        // Setup: default account book exists
        AccountBook accountBook = new AccountBook();
        accountBook.setId(accountBookId);
        accountBook.setFamilyId(familyId);
        accountBook.setName("默认账本");
        when(mockAccountBookMapper.findDefaultByFamilyId(familyId)).thenReturn(accountBook);
        when(mockAccountBookMapper.findById(accountBookId)).thenReturn(accountBook);

        // Setup: category exists
        Category category = new Category();
        category.setId(categoryId);
        category.setFamilyId(familyId);
        category.setType(Transaction.TYPE_EXPENSE);
        category.setName("测试分类");
        when(mockCategoryMapper.findById(categoryId)).thenReturn(category);

        // Setup: user exists
        User user = new User();
        user.setId(userId);
        user.setNickname("用户" + userId);
        when(mockUserMapper.findById(userId)).thenReturn(user);

        // Capture the inserted transaction
        doAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId(1L);
            t.setCreatedAt(LocalDateTime.now());
            t.setUpdatedAt(LocalDateTime.now());
            return 1;
        }).when(mockTransactionMapper).insert(any(Transaction.class));

        // When: creating a transaction
        TransactionDTO dto = new TransactionDTO();
        dto.setCategoryId(categoryId);
        dto.setType(Transaction.TYPE_EXPENSE);
        dto.setAmount(amount);
        dto.setTransactionDate(LocalDate.now());

        TransactionVO result = service.createTransaction(userId, dto);

        // Then: the creator should be recorded
        assertEquals(userId, result.getUserId());
        assertEquals("用户" + userId, result.getUserNickname());

        // Verify the transaction was inserted with correct userId
        verify(mockTransactionMapper).insert(argThat(t -> t.getUserId().equals(userId)));
    }

    /**
     * 生成有效的交易类型
     */
    @Provide
    Arbitrary<Integer> validTransactionTypes() {
        return Arbitraries.of(Transaction.TYPE_EXPENSE, Transaction.TYPE_INCOME);
    }

    /**
     * 生成有效的日期（过去一年内）
     */
    @Provide
    Arbitrary<LocalDate> validDates() {
        LocalDate now = LocalDate.now();
        return Arbitraries.integers()
                .between(0, 365)
                .map(days -> now.minusDays(days));
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
