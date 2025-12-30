package com.family.accounting.service;

import com.family.accounting.dto.CategoryStatVO;
import com.family.accounting.dto.StatisticsVO;
import com.family.accounting.entity.AccountBook;
import com.family.accounting.entity.FamilyMember;
import com.family.accounting.entity.Transaction;
import com.family.accounting.mapper.AccountBookMapper;
import com.family.accounting.mapper.FamilyMemberMapper;
import com.family.accounting.mapper.StatisticsMapper;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 统计服务属性测试
 * <p>
 * Feature: family-accounting, Property 12: 统计计算正确性
 * Validates: Requirements 6.1, 6.2, 6.3, 6.4, 6.5
 */
class StatisticsServicePropertyTest {

    /**
     * Property 12: 统计计算正确性 - 结余计算
     * For any time period, balance should equal totalIncome - totalExpense.
     */
    @Property(tries = 100)
    void balanceShouldEqualIncomeMinusExpense(
            @ForAll @BigRange(min = "0", max = "999999.99") BigDecimal income,
            @ForAll @BigRange(min = "0", max = "999999.99") BigDecimal expense
    ) {
        // Given: mock dependencies
        StatisticsMapper mockStatisticsMapper = Mockito.mock(StatisticsMapper.class);
        AccountBookMapper mockAccountBookMapper = Mockito.mock(AccountBookMapper.class);
        FamilyMemberMapper mockFamilyMemberMapper = Mockito.mock(FamilyMemberMapper.class);

        StatisticsService service = new StatisticsService();
        setField(service, "statisticsMapper", mockStatisticsMapper);
        setField(service, "accountBookMapper", mockAccountBookMapper);
        setField(service, "familyMemberMapper", mockFamilyMemberMapper);

        Long userId = 1L;
        Long familyId = 100L;
        Long accountBookId = 200L;
        LocalDate date = LocalDate.now();

        // Setup: user belongs to a family
        FamilyMember member = new FamilyMember();
        member.setUserId(userId);
        member.setFamilyId(familyId);
        when(mockFamilyMemberMapper.findByUserId(userId)).thenReturn(member);

        // Setup: default account book exists
        AccountBook accountBook = new AccountBook();
        accountBook.setId(accountBookId);
        accountBook.setFamilyId(familyId);
        when(mockAccountBookMapper.findDefaultByFamilyId(familyId)).thenReturn(accountBook);

        // Setup: statistics data
        when(mockStatisticsMapper.sumIncomeByDateRange(eq(accountBookId), any(), any())).thenReturn(income);
        when(mockStatisticsMapper.sumExpenseByDateRange(eq(accountBookId), any(), any())).thenReturn(expense);
        when(mockStatisticsMapper.sumByCategory(eq(accountBookId), anyInt(), any(), any())).thenReturn(new ArrayList<>());

        // When: getting daily statistics
        StatisticsVO result = service.getDailyStatistics(userId, null, date);

        // Then: balance should equal income - expense
        BigDecimal expectedBalance = income.subtract(expense);
        assertEquals(0, expectedBalance.compareTo(result.getBalance()),
                "Balance should equal totalIncome - totalExpense");
        assertEquals(0, income.compareTo(result.getTotalIncome()));
        assertEquals(0, expense.compareTo(result.getTotalExpense()));
    }

    /**
     * Property 12: 统计计算正确性 - 分类统计总和
     * For any time period, category-wise breakdown should sum to the total.
     */
    @Property(tries = 100)
    void categoryBreakdownShouldSumToTotal(
            @ForAll("categoryAmounts") List<BigDecimal> amounts
    ) {
        // Given: mock dependencies
        StatisticsMapper mockStatisticsMapper = Mockito.mock(StatisticsMapper.class);
        AccountBookMapper mockAccountBookMapper = Mockito.mock(AccountBookMapper.class);
        FamilyMemberMapper mockFamilyMemberMapper = Mockito.mock(FamilyMemberMapper.class);

        StatisticsService service = new StatisticsService();
        setField(service, "statisticsMapper", mockStatisticsMapper);
        setField(service, "accountBookMapper", mockAccountBookMapper);
        setField(service, "familyMemberMapper", mockFamilyMemberMapper);

        Long userId = 1L;
        Long familyId = 100L;
        Long accountBookId = 200L;
        LocalDate date = LocalDate.now();

        // Setup: user belongs to a family
        FamilyMember member = new FamilyMember();
        member.setUserId(userId);
        member.setFamilyId(familyId);
        when(mockFamilyMemberMapper.findByUserId(userId)).thenReturn(member);

        // Setup: default account book exists
        AccountBook accountBook = new AccountBook();
        accountBook.setId(accountBookId);
        accountBook.setFamilyId(familyId);
        when(mockAccountBookMapper.findDefaultByFamilyId(familyId)).thenReturn(accountBook);

        // Calculate expected total expense
        BigDecimal expectedTotal = amounts.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Setup: category statistics data
        List<Map<String, Object>> categoryStats = new ArrayList<>();
        for (int i = 0; i < amounts.size(); i++) {
            Map<String, Object> stat = new HashMap<>();
            stat.put("categoryId", (long) (i + 1));
            stat.put("categoryName", "分类" + (i + 1));
            stat.put("categoryIcon", "icon" + (i + 1));
            stat.put("amount", amounts.get(i));
            stat.put("count", 1);
            categoryStats.add(stat);
        }

        when(mockStatisticsMapper.sumIncomeByDateRange(eq(accountBookId), any(), any())).thenReturn(BigDecimal.ZERO);
        when(mockStatisticsMapper.sumExpenseByDateRange(eq(accountBookId), any(), any())).thenReturn(expectedTotal);
        when(mockStatisticsMapper.sumByCategory(eq(accountBookId), eq(Transaction.TYPE_EXPENSE), any(), any()))
                .thenReturn(categoryStats);
        when(mockStatisticsMapper.sumByCategory(eq(accountBookId), eq(Transaction.TYPE_INCOME), any(), any()))
                .thenReturn(new ArrayList<>());

        // When: getting daily statistics
        StatisticsVO result = service.getDailyStatistics(userId, null, date);

        // Then: category breakdown should sum to total expense
        BigDecimal categorySum = result.getCategoryStats().stream()
                .filter(stat -> stat.getType().equals(Transaction.TYPE_EXPENSE))
                .map(CategoryStatVO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertEquals(0, expectedTotal.compareTo(categorySum),
                "Category breakdown should sum to total expense");
    }

    @Provide
    Arbitrary<List<BigDecimal>> categoryAmounts() {
        return Arbitraries.bigDecimals()
                .between(new BigDecimal("0.01"), new BigDecimal("9999.99"))
                .ofScale(2)
                .list()
                .ofMinSize(1)
                .ofMaxSize(10);
    }

    /**
     * Property 12: 统计计算正确性 - 百分比总和
     * For any category breakdown, percentages should sum to approximately 100%.
     */
    @Property(tries = 100)
    void categoryPercentagesShouldSumToHundred(
            @ForAll("nonEmptyCategoryAmounts") List<BigDecimal> amounts
    ) {
        // Given: mock dependencies
        StatisticsMapper mockStatisticsMapper = Mockito.mock(StatisticsMapper.class);
        AccountBookMapper mockAccountBookMapper = Mockito.mock(AccountBookMapper.class);
        FamilyMemberMapper mockFamilyMemberMapper = Mockito.mock(FamilyMemberMapper.class);

        StatisticsService service = new StatisticsService();
        setField(service, "statisticsMapper", mockStatisticsMapper);
        setField(service, "accountBookMapper", mockAccountBookMapper);
        setField(service, "familyMemberMapper", mockFamilyMemberMapper);

        Long userId = 1L;
        Long familyId = 100L;
        Long accountBookId = 200L;
        LocalDate date = LocalDate.now();

        // Setup: user belongs to a family
        FamilyMember member = new FamilyMember();
        member.setUserId(userId);
        member.setFamilyId(familyId);
        when(mockFamilyMemberMapper.findByUserId(userId)).thenReturn(member);

        // Setup: default account book exists
        AccountBook accountBook = new AccountBook();
        accountBook.setId(accountBookId);
        accountBook.setFamilyId(familyId);
        when(mockAccountBookMapper.findDefaultByFamilyId(familyId)).thenReturn(accountBook);

        // Calculate expected total
        BigDecimal expectedTotal = amounts.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Setup: category statistics data
        List<Map<String, Object>> categoryStats = new ArrayList<>();
        for (int i = 0; i < amounts.size(); i++) {
            Map<String, Object> stat = new HashMap<>();
            stat.put("categoryId", (long) (i + 1));
            stat.put("categoryName", "分类" + (i + 1));
            stat.put("categoryIcon", "icon" + (i + 1));
            stat.put("amount", amounts.get(i));
            stat.put("count", 1);
            categoryStats.add(stat);
        }

        when(mockStatisticsMapper.sumIncomeByDateRange(eq(accountBookId), any(), any())).thenReturn(BigDecimal.ZERO);
        when(mockStatisticsMapper.sumExpenseByDateRange(eq(accountBookId), any(), any())).thenReturn(expectedTotal);
        when(mockStatisticsMapper.sumByCategory(eq(accountBookId), eq(Transaction.TYPE_EXPENSE), any(), any()))
                .thenReturn(categoryStats);
        when(mockStatisticsMapper.sumByCategory(eq(accountBookId), eq(Transaction.TYPE_INCOME), any(), any()))
                .thenReturn(new ArrayList<>());

        // When: getting daily statistics
        StatisticsVO result = service.getDailyStatistics(userId, null, date);

        // Then: percentages should sum to approximately 100% (allowing for rounding)
        BigDecimal percentageSum = result.getCategoryStats().stream()
                .filter(stat -> stat.getType().equals(Transaction.TYPE_EXPENSE))
                .map(CategoryStatVO::getPercentage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Allow for rounding errors (should be between 99 and 101)
        assertTrue(percentageSum.compareTo(new BigDecimal("99")) >= 0 &&
                        percentageSum.compareTo(new BigDecimal("101")) <= 0,
                "Percentages should sum to approximately 100%, got: " + percentageSum);
    }

    @Provide
    Arbitrary<List<BigDecimal>> nonEmptyCategoryAmounts() {
        return Arbitraries.bigDecimals()
                .between(new BigDecimal("0.01"), new BigDecimal("9999.99"))
                .ofScale(2)
                .list()
                .ofMinSize(1)
                .ofMaxSize(10);
    }

    /**
     * Property 12: 统计计算正确性 - 周期类型正确
     * For any statistics request, the period type should be correctly set.
     */
    @Property(tries = 100)
    void periodTypeShouldBeCorrectlySet(
            @ForAll("periodTypes") String periodType,
            @ForAll("validDates") LocalDate date
    ) {
        // Given: mock dependencies
        StatisticsMapper mockStatisticsMapper = Mockito.mock(StatisticsMapper.class);
        AccountBookMapper mockAccountBookMapper = Mockito.mock(AccountBookMapper.class);
        FamilyMemberMapper mockFamilyMemberMapper = Mockito.mock(FamilyMemberMapper.class);

        StatisticsService service = new StatisticsService();
        setField(service, "statisticsMapper", mockStatisticsMapper);
        setField(service, "accountBookMapper", mockAccountBookMapper);
        setField(service, "familyMemberMapper", mockFamilyMemberMapper);

        Long userId = 1L;
        Long familyId = 100L;
        Long accountBookId = 200L;

        // Setup: user belongs to a family
        FamilyMember member = new FamilyMember();
        member.setUserId(userId);
        member.setFamilyId(familyId);
        when(mockFamilyMemberMapper.findByUserId(userId)).thenReturn(member);

        // Setup: default account book exists
        AccountBook accountBook = new AccountBook();
        accountBook.setId(accountBookId);
        accountBook.setFamilyId(familyId);
        when(mockAccountBookMapper.findDefaultByFamilyId(familyId)).thenReturn(accountBook);

        // Setup: statistics data
        when(mockStatisticsMapper.sumIncomeByDateRange(eq(accountBookId), any(), any())).thenReturn(BigDecimal.ZERO);
        when(mockStatisticsMapper.sumExpenseByDateRange(eq(accountBookId), any(), any())).thenReturn(BigDecimal.ZERO);
        when(mockStatisticsMapper.sumByCategory(eq(accountBookId), anyInt(), any(), any())).thenReturn(new ArrayList<>());

        // When: getting statistics based on period type
        StatisticsVO result;
        switch (periodType) {
            case "daily":
                result = service.getDailyStatistics(userId, null, date);
                break;
            case "weekly":
                result = service.getWeeklyStatistics(userId, null, date);
                break;
            case "monthly":
                result = service.getMonthlyStatistics(userId, null, date.getYear(), date.getMonthValue());
                break;
            case "yearly":
                result = service.getYearlyStatistics(userId, null, date.getYear());
                break;
            default:
                throw new IllegalArgumentException("Unknown period type: " + periodType);
        }

        // Then: period type should be correctly set
        assertEquals(periodType, result.getPeriodType());
    }

    @Provide
    Arbitrary<String> periodTypes() {
        return Arbitraries.of("daily", "weekly", "monthly", "yearly");
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
