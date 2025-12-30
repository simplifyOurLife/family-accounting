package com.family.accounting.service;

import com.family.accounting.dto.TransactionDTO;
import com.family.accounting.dto.TransactionVO;
import com.family.accounting.entity.*;
import com.family.accounting.mapper.*;
import net.jqwik.api.*;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 交易金额精度属性测试
 * <p>
 * Feature: family-accounting, Property 11: 交易金额精度保持
 * Validates: Requirements 9.3
 */
class TransactionAmountPropertyTest {

    /**
     * Property 11: 交易金额精度保持
     * For any transaction amount (including decimal values), storing and retrieving
     * should preserve the exact decimal value without floating point errors.
     */
    @Property(tries = 100)
    void amountPrecisionShouldBePreserved(
            @ForAll("decimalAmounts") BigDecimal amount
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
        user.setNickname("测试用户");
        when(mockUserMapper.findById(userId)).thenReturn(user);

        // Capture the inserted transaction and simulate retrieval
        final Transaction[] capturedTransaction = new Transaction[1];
        doAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId(1L);
            t.setCreatedAt(LocalDateTime.now());
            t.setUpdatedAt(LocalDateTime.now());
            capturedTransaction[0] = t;
            return 1;
        }).when(mockTransactionMapper).insert(any(Transaction.class));

        // When: creating a transaction with specific decimal amount
        TransactionDTO dto = new TransactionDTO();
        dto.setCategoryId(categoryId);
        dto.setType(Transaction.TYPE_EXPENSE);
        dto.setAmount(amount);
        dto.setTransactionDate(LocalDate.now());

        TransactionVO result = service.createTransaction(userId, dto);

        // Then: the amount should be exactly preserved (no floating point errors)
        assertNotNull(result);
        assertNotNull(result.getAmount());

        // Compare using BigDecimal comparison (not double comparison)
        assertEquals(0, amount.compareTo(result.getAmount()),
                "Amount precision lost: expected " + amount + " but got " + result.getAmount());

        // Verify the stored amount is exactly the same
        assertNotNull(capturedTransaction[0]);
        assertEquals(0, amount.compareTo(capturedTransaction[0].getAmount()),
                "Stored amount precision lost: expected " + amount + " but got " + capturedTransaction[0].getAmount());
    }

    /**
     * Property 11: 交易金额精度保持 - 边界值测试
     * For edge case amounts (very small decimals, large numbers), precision should be maintained.
     */
    @Property(tries = 100)
    void edgeCaseAmountsShouldPreservePrecision(
            @ForAll("edgeCaseAmounts") BigDecimal amount
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

        // Setup: category exists
        Category category = new Category();
        category.setId(categoryId);
        category.setFamilyId(familyId);
        category.setType(Transaction.TYPE_INCOME);
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
        dto.setType(Transaction.TYPE_INCOME);
        dto.setAmount(amount);
        dto.setTransactionDate(LocalDate.now());

        TransactionVO result = service.createTransaction(userId, dto);

        // Then: the amount should be exactly preserved
        assertNotNull(result);
        assertEquals(0, amount.compareTo(result.getAmount()),
                "Edge case amount precision lost: expected " + amount + " but got " + result.getAmount());
    }

    /**
     * 生成各种小数金额（包括常见的货币金额）
     */
    @Provide
    Arbitrary<BigDecimal> decimalAmounts() {
        return Arbitraries.bigDecimals()
                .between(new BigDecimal("0.01"), new BigDecimal("9999999999.99"))
                .ofScale(2);
    }

    /**
     * 生成边界值金额
     */
    @Provide
    Arbitrary<BigDecimal> edgeCaseAmounts() {
        return Arbitraries.of(
                new BigDecimal("0.01"),           // 最小金额
                new BigDecimal("0.10"),           // 一角
                new BigDecimal("0.99"),           // 接近1元
                new BigDecimal("1.00"),           // 整数
                new BigDecimal("9.99"),           // 常见价格
                new BigDecimal("10.00"),          // 整十
                new BigDecimal("99.99"),          // 接近100
                new BigDecimal("100.00"),         // 整百
                new BigDecimal("123.45"),         // 常见金额
                new BigDecimal("999.99"),         // 接近1000
                new BigDecimal("1000.00"),        // 整千
                new BigDecimal("9999.99"),        // 接近万
                new BigDecimal("10000.00"),       // 整万
                new BigDecimal("99999.99"),       // 接近十万
                new BigDecimal("100000.00"),      // 十万
                new BigDecimal("999999.99"),      // 接近百万
                new BigDecimal("1000000.00"),     // 百万
                new BigDecimal("9999999999.99")   // 最大金额（接近数据库DECIMAL(12,2)限制）
        );
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
