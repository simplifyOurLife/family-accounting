package com.family.accounting.controller;

import com.family.accounting.dto.AccountBookDTO;
import com.family.accounting.dto.AccountBookVO;
import com.family.accounting.dto.Result;
import com.family.accounting.security.SecurityUtils;
import com.family.accounting.service.AccountBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 账本管理控制器
 */
@RestController
@RequestMapping("/api/account-book")
public class AccountBookController {

    @Autowired
    private AccountBookService accountBookService;

    /**
     * 获取账本列表
     *
     * @return 账本列表
     */
    @GetMapping
    public Result<List<AccountBookVO>> getAccountBooks() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<AccountBookVO> accountBooks = accountBookService.getAccountBooks(userId);
        return Result.success(accountBooks);
    }

    /**
     * 根据ID获取账本详情
     *
     * @param id 账本ID
     * @return 账本信息
     */
    @GetMapping("/{id}")
    public Result<AccountBookVO> getAccountBookById(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        AccountBookVO accountBook = accountBookService.getAccountBookById(userId, id);
        return Result.success(accountBook);
    }

    /**
     * 获取默认账本
     *
     * @return 默认账本
     */
    @GetMapping("/default")
    public Result<AccountBookVO> getDefaultAccountBook() {
        Long userId = SecurityUtils.getCurrentUserId();
        AccountBookVO accountBook = accountBookService.getDefaultAccountBook(userId);
        return Result.success(accountBook);
    }

    /**
     * 创建账本
     *
     * @param dto 账本信息
     * @return 创建的账本
     */
    @PostMapping
    public Result<AccountBookVO> createAccountBook(@Valid @RequestBody AccountBookDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        AccountBookVO accountBook = accountBookService.createAccountBook(userId, dto);
        return Result.success("创建成功", accountBook);
    }

    /**
     * 更新账本
     *
     * @param id  账本ID
     * @param dto 账本信息
     * @return 更新后的账本
     */
    @PutMapping("/{id}")
    public Result<AccountBookVO> updateAccountBook(@PathVariable Long id, @Valid @RequestBody AccountBookDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        AccountBookVO accountBook = accountBookService.updateAccountBook(userId, id, dto);
        return Result.success("更新成功", accountBook);
    }

    /**
     * 删除账本
     *
     * @param id    账本ID
     * @param force 是否强制删除（有交易时）
     * @return 成功响应
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteAccountBook(@PathVariable Long id,
                                          @RequestParam(required = false, defaultValue = "false") boolean force) {
        Long userId = SecurityUtils.getCurrentUserId();
        accountBookService.deleteAccountBook(userId, id, force);
        return Result.success("删除成功", null);
    }

    /**
     * 设为默认账本
     *
     * @param id 账本ID
     * @return 成功响应
     */
    @PutMapping("/{id}/default")
    public Result<Void> setDefaultAccountBook(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        accountBookService.setDefaultAccountBook(userId, id);
        return Result.success("设置成功", null);
    }

    /**
     * 检查账本是否可删除
     *
     * @param id 账本ID
     * @return 是否可删除
     */
    @GetMapping("/{id}/can-delete")
    public Result<Boolean> canDeleteAccountBook(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        boolean canDelete = accountBookService.canDeleteAccountBook(userId, id);
        return Result.success(canDelete);
    }
}
