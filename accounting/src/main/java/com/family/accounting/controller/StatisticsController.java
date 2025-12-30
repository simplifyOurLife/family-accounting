package com.family.accounting.controller;

import com.family.accounting.dto.CategoryStatVO;
import com.family.accounting.dto.Result;
import com.family.accounting.dto.StatisticsVO;
import com.family.accounting.security.SecurityUtils;
import com.family.accounting.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 统计控制器
 */
@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    /**
     * 获取日统计
     *
     * @param accountBookId 账本ID（可选，为空则使用默认账本）
     * @param date          日期（默认今天）
     * @return 统计数据
     */
    @GetMapping("/daily")
    public Result<StatisticsVO> getDailyStatistics(
            @RequestParam(required = false) Long accountBookId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (date == null) {
            date = LocalDate.now();
        }
        StatisticsVO statistics = statisticsService.getDailyStatistics(userId, accountBookId, date);
        return Result.success(statistics);
    }

    /**
     * 获取周统计
     *
     * @param accountBookId 账本ID（可选，为空则使用默认账本）
     * @param date          周内任意日期（默认今天）
     * @return 统计数据
     */
    @GetMapping("/weekly")
    public Result<StatisticsVO> getWeeklyStatistics(
            @RequestParam(required = false) Long accountBookId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (date == null) {
            date = LocalDate.now();
        }
        StatisticsVO statistics = statisticsService.getWeeklyStatistics(userId, accountBookId, date);
        return Result.success(statistics);
    }

    /**
     * 获取月统计
     *
     * @param accountBookId 账本ID（可选，为空则使用默认账本）
     * @param year          年份（默认当前年）
     * @param month         月份（默认当前月）
     * @return 统计数据
     */
    @GetMapping("/monthly")
    public Result<StatisticsVO> getMonthlyStatistics(
            @RequestParam(required = false) Long accountBookId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        Long userId = SecurityUtils.getCurrentUserId();
        LocalDate now = LocalDate.now();
        if (year == null) {
            year = now.getYear();
        }
        if (month == null) {
            month = now.getMonthValue();
        }
        StatisticsVO statistics = statisticsService.getMonthlyStatistics(userId, accountBookId, year, month);
        return Result.success(statistics);
    }

    /**
     * 获取年统计
     *
     * @param accountBookId 账本ID（可选，为空则使用默认账本）
     * @param year          年份（默认当前年）
     * @return 统计数据
     */
    @GetMapping("/yearly")
    public Result<StatisticsVO> getYearlyStatistics(
            @RequestParam(required = false) Long accountBookId,
            @RequestParam(required = false) Integer year) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (year == null) {
            year = LocalDate.now().getYear();
        }
        StatisticsVO statistics = statisticsService.getYearlyStatistics(userId, accountBookId, year);
        return Result.success(statistics);
    }

    /**
     * 获取分类统计
     *
     * @param accountBookId 账本ID（可选，为空则使用默认账本）
     * @param type          类型: 1-支出 2-收入
     * @param startDate     开始日期
     * @param endDate       结束日期
     * @return 分类统计列表
     */
    @GetMapping("/category")
    public Result<List<CategoryStatVO>> getCategoryStatistics(
            @RequestParam(required = false) Long accountBookId,
            @RequestParam Integer type,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<CategoryStatVO> statistics = statisticsService.getCategoryStatistics(
                userId, accountBookId, type, startDate, endDate);
        return Result.success(statistics);
    }
}
