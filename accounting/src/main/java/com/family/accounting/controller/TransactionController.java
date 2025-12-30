package com.family.accounting.controller;

import com.family.accounting.dto.PageVO;
import com.family.accounting.dto.Result;
import com.family.accounting.dto.TransactionDTO;
import com.family.accounting.dto.TransactionVO;
import com.family.accounting.security.SecurityUtils;
import com.family.accounting.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;

/**
 * 交易记录控制器
 */
@RestController
@RequestMapping("/api/transaction")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    /**
     * 获取交易记录列表（分页）
     *
     * @param accountBookId 账本ID（可选，为空则使用默认账本）
     * @param startDate     开始日期（可选）
     * @param endDate       结束日期（可选）
     * @param type          类型（可选）: 1-支出 2-收入
     * @param page          页码（从1开始，默认1）
     * @param size          每页数量（默认20）
     * @return 分页交易记录
     */
    @GetMapping
    public Result<PageVO<TransactionVO>> getTransactionList(
            @RequestParam(required = false) Long accountBookId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Integer type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        PageVO<TransactionVO> transactions = transactionService.getTransactionList(
                userId, accountBookId, startDate, endDate, type, page, size);
        return Result.success(transactions);
    }

    /**
     * 获取交易记录详情
     *
     * @param id 交易ID
     * @return 交易记录详情
     */
    @GetMapping("/{id}")
    public Result<TransactionVO> getTransaction(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        TransactionVO transaction = transactionService.getTransaction(userId, id);
        return Result.success(transaction);
    }

    /**
     * 创建交易记录
     *
     * @param dto 交易信息
     * @return 创建的交易记录
     */
    @PostMapping
    public Result<TransactionVO> createTransaction(@Valid @RequestBody TransactionDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        TransactionVO transaction = transactionService.createTransaction(userId, dto);
        return Result.success("记账成功", transaction);
    }

    /**
     * 更新交易记录
     *
     * @param id  交易ID
     * @param dto 交易信息
     * @return 更新后的交易记录
     */
    @PutMapping("/{id}")
    public Result<TransactionVO> updateTransaction(@PathVariable Long id, @Valid @RequestBody TransactionDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        TransactionVO transaction = transactionService.updateTransaction(userId, id, dto);
        return Result.success("更新成功", transaction);
    }

    /**
     * 删除交易记录
     *
     * @param id 交易ID
     * @return 成功响应
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteTransaction(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        transactionService.deleteTransaction(userId, id);
        return Result.success("删除成功", null);
    }
}
