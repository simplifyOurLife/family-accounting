package com.family.accounting.service;

import com.family.accounting.dto.CategoryStatVO;
import com.family.accounting.dto.StatisticsVO;
import com.family.accounting.entity.AccountBook;
import com.family.accounting.entity.FamilyMember;
import com.family.accounting.entity.Transaction;
import com.family.accounting.exception.BusinessException;
import com.family.accounting.mapper.AccountBookMapper;
import com.family.accounting.mapper.FamilyMemberMapper;
import com.family.accounting.mapper.StatisticsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 统计服务类
 */
@Service
public class StatisticsService {

    @Autowired
    private StatisticsMapper statisticsMapper;

    @Autowired
    private AccountBookMapper accountBookMapper;

    @Autowired
    private FamilyMemberMapper familyMemberMapper;

    /**
     * 获取日统计
     *
     * @param userId        当前用户ID
     * @param accountBookId 账本ID（可选）
     * @param date          日期
     * @return 统计数据
     */
    public StatisticsVO getDailyStatistics(Long userId, Long accountBookId, LocalDate date) {
        AccountBook accountBook = getAccountBook(userId, accountBookId);
        return buildStatistics(accountBook.getId(), "daily", date, date);
    }

    /**
     * 获取周统计
     *
     * @param userId        当前用户ID
     * @param accountBookId 账本ID（可选）
     * @param date          周内任意日期
     * @return 统计数据
     */
    public StatisticsVO getWeeklyStatistics(Long userId, Long accountBookId, LocalDate date) {
        AccountBook accountBook = getAccountBook(userId, accountBookId);

        // 计算周的开始和结束日期（周一到周日）
        LocalDate startDate = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endDate = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        return buildStatistics(accountBook.getId(), "weekly", startDate, endDate);
    }

    /**
     * 获取月统计
     *
     * @param userId        当前用户ID
     * @param accountBookId 账本ID（可选）
     * @param year          年份
     * @param month         月份
     * @return 统计数据
     */
    public StatisticsVO getMonthlyStatistics(Long userId, Long accountBookId, int year, int month) {
        AccountBook accountBook = getAccountBook(userId, accountBookId);

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());

        return buildStatistics(accountBook.getId(), "monthly", startDate, endDate);
    }

    /**
     * 获取年统计
     *
     * @param userId        当前用户ID
     * @param accountBookId 账本ID（可选）
     * @param year          年份
     * @return 统计数据
     */
    public StatisticsVO getYearlyStatistics(Long userId, Long accountBookId, int year) {
        AccountBook accountBook = getAccountBook(userId, accountBookId);

        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        return buildStatistics(accountBook.getId(), "yearly", startDate, endDate);
    }

    /**
     * 获取分类统计
     *
     * @param userId        当前用户ID
     * @param accountBookId 账本ID（可选）
     * @param type          类型: 1-支出 2-收入
     * @param startDate     开始日期
     * @param endDate       结束日期
     * @return 分类统计列表
     */
    public List<CategoryStatVO> getCategoryStatistics(Long userId, Long accountBookId,
                                                      Integer type, LocalDate startDate, LocalDate endDate) {
        AccountBook accountBook = getAccountBook(userId, accountBookId);
        return buildCategoryStats(accountBook.getId(), type, startDate, endDate);
    }

    /**
     * 获取账本（验证权限）
     */
    private AccountBook getAccountBook(Long userId, Long accountBookId) {
        Long familyId = getFamilyIdByUser(userId);

        AccountBook accountBook;
        if (accountBookId != null) {
            accountBook = accountBookMapper.findById(accountBookId);
            if (accountBook == null) {
                throw new BusinessException("账本不存在");
            }
            if (!accountBook.getFamilyId().equals(familyId)) {
                throw new BusinessException("无权访问此账本");
            }
        } else {
            accountBook = accountBookMapper.findDefaultByFamilyId(familyId);
            if (accountBook == null) {
                throw new BusinessException("请先创建账本");
            }
        }
        return accountBook;
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
     * 构建统计数据
     */
    private StatisticsVO buildStatistics(Long accountBookId, String periodType,
                                         LocalDate startDate, LocalDate endDate) {
        StatisticsVO vo = new StatisticsVO();
        vo.setPeriodType(periodType);
        vo.setStartDate(startDate);
        vo.setEndDate(endDate);

        // 计算收入和支出总额
        BigDecimal totalIncome = statisticsMapper.sumIncomeByDateRange(accountBookId, startDate, endDate);
        BigDecimal totalExpense = statisticsMapper.sumExpenseByDateRange(accountBookId, startDate, endDate);

        vo.setTotalIncome(totalIncome != null ? totalIncome : BigDecimal.ZERO);
        vo.setTotalExpense(totalExpense != null ? totalExpense : BigDecimal.ZERO);
        vo.setBalance(vo.getTotalIncome().subtract(vo.getTotalExpense()));

        // 获取分类统计（合并收入和支出）
        List<CategoryStatVO> categoryStats = new ArrayList<>();
        categoryStats.addAll(buildCategoryStats(accountBookId, Transaction.TYPE_EXPENSE, startDate, endDate));
        categoryStats.addAll(buildCategoryStats(accountBookId, Transaction.TYPE_INCOME, startDate, endDate));
        vo.setCategoryStats(categoryStats);

        return vo;
    }

    /**
     * 构建分类统计
     */
    private List<CategoryStatVO> buildCategoryStats(Long accountBookId, Integer type,
                                                    LocalDate startDate, LocalDate endDate) {
        List<Map<String, Object>> rawStats = statisticsMapper.sumByCategory(accountBookId, type, startDate, endDate);

        // 计算总额用于百分比
        BigDecimal total = rawStats.stream()
                .map(m -> (BigDecimal) m.get("amount"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<CategoryStatVO> result = new ArrayList<>();
        for (Map<String, Object> raw : rawStats) {
            CategoryStatVO stat = new CategoryStatVO();
            stat.setCategoryId(((Number) raw.get("categoryId")).longValue());
            stat.setCategoryName((String) raw.get("categoryName"));
            stat.setCategoryIcon((String) raw.get("categoryIcon"));
            stat.setAmount((BigDecimal) raw.get("amount"));
            stat.setCount(((Number) raw.get("count")).intValue());
            stat.setType(type); // 设置类型

            // 计算百分比
            if (total.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal percentage = stat.getAmount()
                        .multiply(new BigDecimal("100"))
                        .divide(total, 2, RoundingMode.HALF_UP);
                stat.setPercentage(percentage);
            } else {
                stat.setPercentage(BigDecimal.ZERO);
            }

            result.add(stat);
        }

        return result;
    }
}
