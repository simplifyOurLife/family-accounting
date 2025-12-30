package com.family.accounting.service;

import com.family.accounting.dto.AccountBookDTO;
import com.family.accounting.dto.AccountBookVO;
import com.family.accounting.entity.AccountBook;
import com.family.accounting.entity.Family;
import com.family.accounting.entity.FamilyMember;
import com.family.accounting.exception.BusinessException;
import com.family.accounting.mapper.AccountBookMapper;
import com.family.accounting.mapper.FamilyMapper;
import com.family.accounting.mapper.FamilyMemberMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 账本服务类
 */
@Service
public class AccountBookService {

    @Autowired
    private AccountBookMapper accountBookMapper;

    @Autowired
    private FamilyMemberMapper familyMemberMapper;

    @Autowired
    private FamilyMapper familyMapper;

    /**
     * 获取账本列表
     *
     * @param userId 用户ID
     * @return 账本列表
     */
    public List<AccountBookVO> getAccountBooks(Long userId) {
        Long familyId = getFamilyIdByUser(userId);
        List<AccountBook> accountBooks = accountBookMapper.findByFamilyId(familyId);
        return accountBooks.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取账本
     *
     * @param userId        用户ID
     * @param accountBookId 账本ID
     * @return 账本信息
     */
    public AccountBookVO getAccountBookById(Long userId, Long accountBookId) {
        Long familyId = getFamilyIdByUser(userId);

        AccountBook accountBook = accountBookMapper.findById(accountBookId);
        if (accountBook == null) {
            throw new BusinessException("账本不存在");
        }

        // 验证账本属于用户的家庭
        if (!accountBook.getFamilyId().equals(familyId)) {
            throw new BusinessException("无权访问此账本");
        }

        return convertToVO(accountBook);
    }

    /**
     * 获取默认账本
     *
     * @param userId 用户ID
     * @return 默认账本
     */
    public AccountBookVO getDefaultAccountBook(Long userId) {
        Long familyId = getFamilyIdByUser(userId);
        AccountBook accountBook = accountBookMapper.findDefaultByFamilyId(familyId);
        if (accountBook == null) {
            throw new BusinessException("未找到默认账本");
        }
        return convertToVO(accountBook);
    }

    /**
     * 创建账本
     *
     * @param userId 用户ID
     * @param dto    账本信息
     * @return 创建的账本
     */
    @Transactional
    public AccountBookVO createAccountBook(Long userId, AccountBookDTO dto) {
        Long familyId = getFamilyIdByUser(userId);

        // 验证用户是否为家庭管理员
        checkFamilyAdmin(userId, familyId);

        AccountBook accountBook = new AccountBook();
        accountBook.setFamilyId(familyId);
        accountBook.setName(dto.getName());
        accountBook.setIsDefault(0); // 新创建的账本默认不是默认账本

        accountBookMapper.insert(accountBook);

        return convertToVO(accountBook);
    }

    /**
     * 更新账本
     *
     * @param userId        用户ID
     * @param accountBookId 账本ID
     * @param dto           账本信息
     * @return 更新后的账本
     */
    @Transactional
    public AccountBookVO updateAccountBook(Long userId, Long accountBookId, AccountBookDTO dto) {
        Long familyId = getFamilyIdByUser(userId);

        // 验证用户是否为家庭管理员
        checkFamilyAdmin(userId, familyId);

        AccountBook accountBook = accountBookMapper.findById(accountBookId);
        if (accountBook == null) {
            throw new BusinessException("账本不存在");
        }

        // 验证账本属于用户的家庭
        if (!accountBook.getFamilyId().equals(familyId)) {
            throw new BusinessException("无权修改此账本");
        }

        // 更新名称
        accountBook.setName(dto.getName());
        accountBookMapper.update(accountBook);

        return convertToVO(accountBook);
    }

    /**
     * 删除账本
     *
     * @param userId        用户ID
     * @param accountBookId 账本ID
     * @param force         是否强制删除（有交易时）
     */
    @Transactional
    public void deleteAccountBook(Long userId, Long accountBookId, boolean force) {
        Long familyId = getFamilyIdByUser(userId);

        // 验证用户是否为家庭管理员
        checkFamilyAdmin(userId, familyId);

        AccountBook accountBook = accountBookMapper.findById(accountBookId);
        if (accountBook == null) {
            throw new BusinessException("账本不存在");
        }

        // 验证账本属于用户的家庭
        if (!accountBook.getFamilyId().equals(familyId)) {
            throw new BusinessException("无权删除此账本");
        }

        // 检查是否为默认账本
        if (accountBook.getIsDefault() == 1) {
            throw new BusinessException("默认账本不能删除，请先设置其他账本为默认账本");
        }

        // 检查是否有关联交易
        int transactionCount = accountBookMapper.countTransactionsByAccountBookId(accountBookId);
        if (transactionCount > 0 && !force) {
            throw new BusinessException("该账本下有" + transactionCount + "笔交易记录，确认删除请使用强制删除");
        }

        accountBookMapper.deleteById(accountBookId);
    }

    /**
     * 设置默认账本
     *
     * @param userId        用户ID
     * @param accountBookId 账本ID
     */
    @Transactional
    public void setDefaultAccountBook(Long userId, Long accountBookId) {
        Long familyId = getFamilyIdByUser(userId);

        // 验证用户是否为家庭管理员
        checkFamilyAdmin(userId, familyId);

        AccountBook accountBook = accountBookMapper.findById(accountBookId);
        if (accountBook == null) {
            throw new BusinessException("账本不存在");
        }

        // 验证账本属于用户的家庭
        if (!accountBook.getFamilyId().equals(familyId)) {
            throw new BusinessException("无权设置此账本为默认");
        }

        // 先清除当前默认账本
        accountBookMapper.clearDefault(familyId);

        // 设置新的默认账本
        accountBookMapper.setDefault(familyId, accountBookId);
    }

    /**
     * 检查账本是否可删除
     *
     * @param userId        用户ID
     * @param accountBookId 账本ID
     * @return true-可删除，false-不可删除
     */
    public boolean canDeleteAccountBook(Long userId, Long accountBookId) {
        Long familyId = getFamilyIdByUser(userId);

        AccountBook accountBook = accountBookMapper.findById(accountBookId);
        if (accountBook == null || !accountBook.getFamilyId().equals(familyId)) {
            return false;
        }

        // 默认账本不能删除
        if (accountBook.getIsDefault() == 1) {
            return false;
        }

        // 检查是否有关联交易
        int transactionCount = accountBookMapper.countTransactionsByAccountBookId(accountBookId);
        return transactionCount == 0;
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
     * 检查用户是否为家庭管理员
     */
    private void checkFamilyAdmin(Long userId, Long familyId) {
        Family family = familyMapper.findById(familyId);
        if (family == null) {
            throw new BusinessException("家庭不存在");
        }
        if (!family.getAdminId().equals(userId)) {
            throw new BusinessException("只有家庭管理员才能执行此操作");
        }
    }

    /**
     * 将AccountBook实体转换为AccountBookVO
     */
    private AccountBookVO convertToVO(AccountBook accountBook) {
        AccountBookVO vo = new AccountBookVO();
        vo.setId(accountBook.getId());
        vo.setFamilyId(accountBook.getFamilyId());
        vo.setName(accountBook.getName());
        vo.setIsDefault(accountBook.getIsDefault() == 1);
        vo.setCreatedAt(accountBook.getCreatedAt());
        vo.setUpdatedAt(accountBook.getUpdatedAt());

        // 获取关联交易数量
        int transactionCount = accountBookMapper.countTransactionsByAccountBookId(accountBook.getId());
        vo.setTransactionCount(transactionCount);

        return vo;
    }
}
