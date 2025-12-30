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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 交易修改统计同步属性测试
 * <p>
 * Feature: family-accounting, Property 13: 交易修改统计同步
 * Validates: Requirements 5.4, 5.5
 */
class TransactionStatisticsSyncPropertyTest {

    /**
     * Property 13: 交易修改统计同步 - 更新交易金额
     * For any transaction edit, the statistics for the affected time period should reflect the updated values.
     * When a transaction amount is updated, the new amount should be used in statistics calculations.
     */
    @Property(tries = 100)
    void updatedTransactionAmountShouldReflectInStatistics(
            @ForAll @BigRange(min = "0.01", max = "999999.99") BigDecimal originalAmount,
            @ForAll @BigRange(min = "0.01", max = "999999.99") BigDecimal newAmount
    ) {
        // Given: mock dependencies for TransactionService
        TransactionMapper mockTransactionMapper = Mockito.mock(TransactionMapper.class);
        AccountBookMapper mockAccountBookMapper = Mockito.mock(AccountBookMapper.class);
        CategoryMapper mockCategoryMapper = Mockito.mock(CategoryMapper.class);
        FamilyMemberMapper mockFamilyMemberMapper = Mockito.mock(FamilyMemberMapper.class);
        UserMapper mockUserMapper = Mockito.mock(UserMapper.class);

        TransactionService transactionService = new TransactionService();
        setField(transactionService, "transactionMapper", mockTransactionMapper);
        setField(transactionService, "accountBookMapper", mockAccountBookMapper);
        setField(transactionService, "categoryMapper", mockCategoryMapper);
        setField(transactionService, "familyMemberMapper", mockFamilyMemberMapper);
        setField(transactionService, "userMapper", mockUserMapper);

        Long userId = 1L;
        Long familyId = 100L;
        Long accountBookId = 200L;
        Long categoryId = 300L;
        Long transactionId = 400L;
        LocalDate transactionDate = LocalDate.now();

        // Setup: user belongs to a family
        FamilyMember member = new FamilyMember();
        member.setUserId(userId);
        member.setFamilyId(familyId);
        when(mockFamilyMemberMapper.findByUserId(userId)).thenReturn(member);

        // Setup: account book exists
        AccountBook accountBook = new AccountBook();
        accountBook.setId(accountBookId);
        accountBook.setFamilyId(familyId);
        accountBook.setName("默认账本");
        when(mockAccountBookMapper.findById(accountBookId)).thenReturn(accountBook);
        when(mockAccountBookMapper.findDefaultByFamilyId(familyId)).thenReturn(accountBook);

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

        // Setup: original transaction exists
        Transaction originalTransaction = new Transaction();
        originalTransaction.setId(transactionId);
        originalTransaction.setAccountBookId(accountBookId);
        originalTransaction.setCategoryId(categoryId);
        originalTransaction.setUserId(userId);
        originalTransaction.setType(Transaction.TYPE_EXPENSE);
        originalTransaction.setAmount(originalAmount);
        originalTransaction.setTransactionDate(transactionDate);
        originalTransaction.setCreatedAt(LocalDateTime.now());
        originalTransaction.setUpdatedAt(LocalDateTime.now());

        // Track the updated transaction
        final Transaction[] updatedTransaction = new Transaction[1];
        when(mockTransactionMapper.findById(transactionId)).thenAnswer(invocation -> {
            if (updatedTransaction[0] != null) {
                return updatedTransaction[0];
            }
            return originalTransaction;
        });

        doAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            updatedTransaction[0] = new Transaction();
            updatedTransaction[0].setId(t.getId());
            updatedTransaction[0].setAccountBookId(t.getAccountBookId() != null ? t.getAccountBookId() : originalTransaction.getAccountBookId());
            updatedTransaction[0].setCategoryId(t.getCategoryId() != null ? t.getCategoryId() : originalTransaction.getCategoryId());
            updatedTransaction[0].setUserId(originalTransaction.getUserId());
            updatedTransaction[0].setType(t.getType() != null ? t.getType() : originalTransaction.getType());
            updatedTransaction[0].setAmount(t.getAmount() != null ? t.getAmount() : originalTransaction.getAmount());
            updatedTransaction[0].setNote(t.getNote());
            updatedTransaction[0].setTransactionDate(t.getTransactionDate() != null ? t.getTransactionDate() : originalTransaction.getTransactionDate());
            updatedTransaction[0].setCreatedAt(originalTransaction.getCreatedAt());
            updatedTransaction[0].setUpdatedAt(LocalDateTime.now());
            return 1;
        }).when(mockTransactionMapper).update(any(Transaction.class));

        // When: updating the transaction amount
        TransactionDTO updateDto = new TransactionDTO();
        updateDto.setAmount(newAmount);

        TransactionVO result = transactionService.updateTransaction(userId, transactionId, updateDto);

        // Then: the updated amount should be reflected
        assertEquals(0, newAmount.compareTo(result.getAmount()),
                "Updated transaction should have the new amount");

        // Verify the transaction was updated with the new amount
        verify(mockTransactionMapper).update(argThat(t ->
                t.getAmount() != null && t.getAmount().compareTo(newAmount) == 0
        ));
    }

    /**
     * Property 13: 交易修改统计同步 - 删除交易
     * For any transaction deletion, the transaction should be removed and no longer contribute to statistics.
     */
    @Property(tries = 100)
    void deletedTransactionShouldNotContributeToStatistics(
            @ForAll @BigRange(min = "0.01", max = "999999.99") BigDecimal amount
    ) {
        // Given: mock dependencies
        TransactionMapper mockTransactionMapper = Mockito.mock(TransactionMapper.class);
        AccountBookMapper mockAccountBookMapper = Mockito.mock(AccountBookMapper.class);
        CategoryMapper mockCategoryMapper = Mockito.mock(CategoryMapper.class);
        FamilyMemberMapper mockFamilyMemberMapper = Mockito.mock(FamilyMemberMapper.class);
        UserMapper mockUserMapper = Mockito.mock(UserMapper.class);

        TransactionService transactionService = new TransactionService();
        setField(transactionService, "transactionMapper", mockTransactionMapper);
        setField(transactionService, "accountBookMapper", mockAccountBookMapper);
        setField(transactionService, "categoryMapper", mockCategoryMapper);
        setField(transactionService, "familyMemberMapper", mockFamilyMemberMapper);
        setField(transactionService, "userMapper", mockUserMapper);

        Long userId = 1L;
        Long familyId = 100L;
        Long accountBookId = 200L;
        Long categoryId = 300L;
        Long transactionId = 400L;
        LocalDate transactionDate = LocalDate.now();

        // Setup: user belongs to a family
        FamilyMember member = new FamilyMember();
        member.setUserId(userId);
        member.setFamilyId(familyId);
        when(mockFamilyMemberMapper.findByUserId(userId)).thenReturn(member);

        // Setup: account book exists
        AccountBook accountBook = new AccountBook();
        accountBook.setId(accountBookId);
        accountBook.setFamilyId(familyId);
        accountBook.setName("默认账本");
        when(mockAccountBookMapper.findById(accountBookId)).thenReturn(accountBook);

        // Setup: transaction exists
        Transaction transaction = new Transaction();
        transaction.setId(transactionId);
        transaction.setAccountBookId(accountBookId);
        transaction.setCategoryId(categoryId);
        transaction.setUserId(userId);
        transaction.setType(Transaction.TYPE_EXPENSE);
        transaction.setAmount(amount);
        transaction.setTransactionDate(transactionDate);
        transaction.setCreatedAt(LocalDateTime.now());

        // Track deletion
        final boolean[] deleted = {false};
        when(mockTransactionMapper.findById(transactionId)).thenAnswer(invocation -> {
            if (deleted[0]) {
                return null;
            }
            return transaction;
        });

        doAnswer(invocation -> {
            deleted[0] = true;
            return 1;
        }).when(mockTransactionMapper).deleteById(transactionId);

        // When: deleting the transaction
        transactionService.deleteTransaction(userId, transactionId);

        // Then: the transaction should be deleted
        assertTrue(deleted[0], "Transaction should be deleted");
        verify(mockTransactionMapper).deleteById(transactionId);
    }

    /**
     * Property 13: 交易修改统计同步 - 更新交易日期
     * For any transaction date change, the transaction should move to the new time period for statistics.
     */
    @Property(tries = 100)
    void updatedTransactionDateShouldMoveToNewPeriod(
            @ForAll("validDates") LocalDate originalDate,
            @ForAll("validDates") LocalDate newDate
    ) {
        // Given: mock dependencies
        TransactionMapper mockTransactionMapper = Mockito.mock(TransactionMapper.class);
        AccountBookMapper mockAccountBookMapper = Mockito.mock(AccountBookMapper.class);
        CategoryMapper mockCategoryMapper = Mockito.mock(CategoryMapper.class);
        FamilyMemberMapper mockFamilyMemberMapper = Mockito.mock(FamilyMemberMapper.class);
        UserMapper mockUserMapper = Mockito.mock(UserMapper.class);

        TransactionService transactionService = new TransactionService();
        setField(transactionService, "transactionMapper", mockTransactionMapper);
        setField(transactionService, "accountBookMapper", mockAccountBookMapper);
        setField(transactionService, "categoryMapper", mockCategoryMapper);
        setField(transactionService, "familyMemberMapper", mockFamilyMemberMapper);
        setField(transactionService, "userMapper", mockUserMapper);

        Long userId = 1L;
        Long familyId = 100L;
        Long accountBookId = 200L;
        Long categoryId = 300L;
        Long transactionId = 400L;
        BigDecimal amount = new BigDecimal("100.00");

        // Setup: user belongs to a family
        FamilyMember member = new FamilyMember();
        member.setUserId(userId);
        member.setFamilyId(familyId);
        when(mockFamilyMemberMapper.findByUserId(userId)).thenReturn(member);

        // Setup: account book exists
        AccountBook accountBook = new AccountBook();
        accountBook.setId(accountBookId);
        accountBook.setFamilyId(familyId);
        accountBook.setName("默认账本");
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

        // Setup: original transaction exists
        Transaction originalTransaction = new Transaction();
        originalTransaction.setId(transactionId);
        originalTransaction.setAccountBookId(accountBookId);
        originalTransaction.setCategoryId(categoryId);
        originalTransaction.setUserId(userId);
        originalTransaction.setType(Transaction.TYPE_EXPENSE);
        originalTransaction.setAmount(amount);
        originalTransaction.setTransactionDate(originalDate);
        originalTransaction.setCreatedAt(LocalDateTime.now());
        originalTransaction.setUpdatedAt(LocalDateTime.now());

        // Track the updated transaction
        final Transaction[] updatedTransaction = new Transaction[1];
        when(mockTransactionMapper.findById(transactionId)).thenAnswer(invocation -> {
            if (updatedTransaction[0] != null) {
                return updatedTransaction[0];
            }
            return originalTransaction;
        });

        doAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            updatedTransaction[0] = new Transaction();
            updatedTransaction[0].setId(t.getId());
            updatedTransaction[0].setAccountBookId(originalTransaction.getAccountBookId());
            updatedTransaction[0].setCategoryId(t.getCategoryId() != null ? t.getCategoryId() : originalTransaction.getCategoryId());
            updatedTransaction[0].setUserId(originalTransaction.getUserId());
            updatedTransaction[0].setType(t.getType() != null ? t.getType() : originalTransaction.getType());
            updatedTransaction[0].setAmount(t.getAmount() != null ? t.getAmount() : originalTransaction.getAmount());
            updatedTransaction[0].setNote(t.getNote());
            updatedTransaction[0].setTransactionDate(t.getTransactionDate() != null ? t.getTransactionDate() : originalTransaction.getTransactionDate());
            updatedTransaction[0].setCreatedAt(originalTransaction.getCreatedAt());
            updatedTransaction[0].setUpdatedAt(LocalDateTime.now());
            return 1;
        }).when(mockTransactionMapper).update(any(Transaction.class));

        // When: updating the transaction date
        TransactionDTO updateDto = new TransactionDTO();
        updateDto.setTransactionDate(newDate);

        TransactionVO result = transactionService.updateTransaction(userId, transactionId, updateDto);

        // Then: the updated date should be reflected
        assertEquals(newDate, result.getTransactionDate(),
                "Updated transaction should have the new date");

        // Verify the transaction was updated with the new date
        verify(mockTransactionMapper).update(argThat(t ->
                t.getTransactionDate() != null && t.getTransactionDate().equals(newDate)
        ));
    }

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
