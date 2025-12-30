package com.family.accounting.service;

import com.family.accounting.dto.PageVO;
import com.family.accounting.dto.TransactionDTO;
import com.family.accounting.dto.TransactionVO;
import com.family.accounting.entity.*;
import com.family.accounting.exception.BusinessException;
import com.family.accounting.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 交易记录服务类
 */
@Service
public class TransactionService {

    @Autowired
    private TransactionMapper transactionMapper;

    @Autowired
    private AccountBookMapper accountBookMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private FamilyMemberMapper familyMemberMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 创建交易记录
     *
     * @param userId 当前用户ID
     * @param dto    交易信息
     * @return 创建的交易记录
     */
    @Transactional
    public TransactionVO createTransaction(Long userId, TransactionDTO dto) {
        Long familyId = getFamilyIdByUser(userId);

        // 验证账本
        AccountBook accountBook;
        if (dto.getAccountBookId() != null) {
            accountBook = accountBookMapper.findById(dto.getAccountBookId());
            if (accountBook == null) {
                throw new BusinessException("账本不存在");
            }
            if (!accountBook.getFamilyId().equals(familyId)) {
                throw new BusinessException("无权访问此账本");
            }
        } else {
            // 使用默认账本
            accountBook = accountBookMapper.findDefaultByFamilyId(familyId);
            if (accountBook == null) {
                throw new BusinessException("请先创建账本");
            }
        }

        // 验证分类
        Category category = categoryMapper.findById(dto.getCategoryId());
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        if (!category.getFamilyId().equals(familyId)) {
            throw new BusinessException("无权使用此分类");
        }

        // 验证类型
        if (dto.getType() != Transaction.TYPE_EXPENSE && dto.getType() != Transaction.TYPE_INCOME) {
            throw new BusinessException("无效的交易类型");
        }

        // 分类类型必须与交易类型一致
        if (!category.getType().equals(dto.getType())) {
            throw new BusinessException("分类类型与交易类型不匹配");
        }

        // 创建交易记录
        Transaction transaction = new Transaction();
        transaction.setAccountBookId(accountBook.getId());
        transaction.setCategoryId(dto.getCategoryId());
        transaction.setUserId(userId);
        transaction.setType(dto.getType());
        transaction.setAmount(dto.getAmount());
        transaction.setNote(dto.getNote());
        transaction.setTransactionDate(dto.getTransactionDate());

        transactionMapper.insert(transaction);

        return convertToVO(transaction);
    }


    /**
     * 更新交易记录
     *
     * @param userId        当前用户ID
     * @param transactionId 交易ID
     * @param dto           交易信息
     * @return 更新后的交易记录
     */
    @Transactional
    public TransactionVO updateTransaction(Long userId, Long transactionId, TransactionDTO dto) {
        Long familyId = getFamilyIdByUser(userId);

        Transaction transaction = transactionMapper.findById(transactionId);
        if (transaction == null) {
            throw new BusinessException("交易记录不存在");
        }

        // 验证交易记录属于用户的家庭
        AccountBook accountBook = accountBookMapper.findById(transaction.getAccountBookId());
        if (accountBook == null || !accountBook.getFamilyId().equals(familyId)) {
            throw new BusinessException("无权修改此交易记录");
        }

        // 验证分类
        if (dto.getCategoryId() != null) {
            Category category = categoryMapper.findById(dto.getCategoryId());
            if (category == null) {
                throw new BusinessException("分类不存在");
            }
            if (!category.getFamilyId().equals(familyId)) {
                throw new BusinessException("无权使用此分类");
            }
            // 分类类型必须与交易类型一致
            Integer newType = dto.getType() != null ? dto.getType() : transaction.getType();
            if (!category.getType().equals(newType)) {
                throw new BusinessException("分类类型与交易类型不匹配");
            }
            transaction.setCategoryId(dto.getCategoryId());
        }

        // 更新字段
        if (dto.getType() != null) {
            if (dto.getType() != Transaction.TYPE_EXPENSE && dto.getType() != Transaction.TYPE_INCOME) {
                throw new BusinessException("无效的交易类型");
            }
            transaction.setType(dto.getType());
        }
        if (dto.getAmount() != null) {
            transaction.setAmount(dto.getAmount());
        }
        if (dto.getNote() != null) {
            transaction.setNote(dto.getNote());
        }
        if (dto.getTransactionDate() != null) {
            transaction.setTransactionDate(dto.getTransactionDate());
        }

        transactionMapper.update(transaction);

        // 重新查询以获取最新数据
        transaction = transactionMapper.findById(transactionId);
        return convertToVO(transaction);
    }

    /**
     * 删除交易记录
     *
     * @param userId        当前用户ID
     * @param transactionId 交易ID
     */
    @Transactional
    public void deleteTransaction(Long userId, Long transactionId) {
        Long familyId = getFamilyIdByUser(userId);

        Transaction transaction = transactionMapper.findById(transactionId);
        if (transaction == null) {
            throw new BusinessException("交易记录不存在");
        }

        // 验证交易记录属于用户的家庭
        AccountBook accountBook = accountBookMapper.findById(transaction.getAccountBookId());
        if (accountBook == null || !accountBook.getFamilyId().equals(familyId)) {
            throw new BusinessException("无权删除此交易记录");
        }

        transactionMapper.deleteById(transactionId);
    }

    /**
     * 获取交易记录详情
     *
     * @param userId        当前用户ID
     * @param transactionId 交易ID
     * @return 交易记录详情
     */
    public TransactionVO getTransaction(Long userId, Long transactionId) {
        Long familyId = getFamilyIdByUser(userId);

        Transaction transaction = transactionMapper.findById(transactionId);
        if (transaction == null) {
            throw new BusinessException("交易记录不存在");
        }

        // 验证交易记录属于用户的家庭
        AccountBook accountBook = accountBookMapper.findById(transaction.getAccountBookId());
        if (accountBook == null || !accountBook.getFamilyId().equals(familyId)) {
            throw new BusinessException("无权访问此交易记录");
        }

        return convertToVO(transaction);
    }


    /**
     * 分页查询交易记录
     *
     * @param userId        当前用户ID
     * @param accountBookId 账本ID（可选，为空则使用默认账本）
     * @param startDate     开始日期（可选）
     * @param endDate       结束日期（可选）
     * @param type          类型（可选）
     * @param page          页码（从1开始）
     * @param size          每页数量
     * @return 分页交易记录
     */
    public PageVO<TransactionVO> getTransactionList(Long userId, Long accountBookId,
                                                    LocalDate startDate, LocalDate endDate, Integer type, int page, int size) {
        System.out.println("=== TransactionService.getTransactionList ===");
        System.out.println("userId: " + userId);
        System.out.println("accountBookId: " + accountBookId);
        System.out.println("startDate: " + startDate);
        System.out.println("endDate: " + endDate);
        System.out.println("type: " + type);
        System.out.println("page: " + page + ", size: " + size);

        Long familyId = getFamilyIdByUser(userId);
        System.out.println("familyId: " + familyId);

        // 确定账本
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
        System.out.println("accountBook: " + accountBook.getId() + " - " + accountBook.getName());

        // 计算偏移量
        int offset = (page - 1) * size;

        // 查询数据
        List<Transaction> transactions = transactionMapper.findByAccountBookId(
                accountBook.getId(), startDate, endDate, type, offset, size);
        System.out.println("Found transactions: " + transactions.size());

        // 查询总数
        int total = transactionMapper.countByAccountBookId(
                accountBook.getId(), startDate, endDate, type);
        System.out.println("Total transactions: " + total);

        // 转换为VO
        List<TransactionVO> voList = transactions.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        PageVO<TransactionVO> result = PageVO.of(voList, page, size, total);
        System.out.println("Returning page: " + result.getPage() + "/" + result.getTotalPages() +
                ", records: " + result.getList().size());
        return result;
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
     * 将Transaction实体转换为TransactionVO
     */
    private TransactionVO convertToVO(Transaction transaction) {
        TransactionVO vo = new TransactionVO();
        vo.setId(transaction.getId());
        vo.setAccountBookId(transaction.getAccountBookId());
        vo.setCategoryId(transaction.getCategoryId());
        vo.setUserId(transaction.getUserId());
        vo.setType(transaction.getType());
        vo.setTypeText(transaction.getType() == Transaction.TYPE_EXPENSE ? "支出" : "收入");
        vo.setAmount(transaction.getAmount());
        vo.setNote(transaction.getNote());
        vo.setTransactionDate(transaction.getTransactionDate());
        vo.setCreatedAt(transaction.getCreatedAt());
        vo.setUpdatedAt(transaction.getUpdatedAt());

        // 获取账本名称
        AccountBook accountBook = accountBookMapper.findById(transaction.getAccountBookId());
        if (accountBook != null) {
            vo.setAccountBookName(accountBook.getName());
        }

        // 获取分类信息
        Category category = categoryMapper.findById(transaction.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getName());
            vo.setCategoryIcon(category.getIcon());
        }

        // 获取用户昵称
        User user = userMapper.findById(transaction.getUserId());
        if (user != null) {
            vo.setUserNickname(user.getNickname() != null ? user.getNickname() : user.getPhone());
        }

        return vo;
    }
}
