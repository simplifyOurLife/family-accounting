package com.family.accounting.mapper;

import com.family.accounting.entity.Transaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 交易记录数据访问接口
 */
@Mapper
public interface TransactionMapper {

    /**
     * 根据ID查询交易记录
     *
     * @param id 交易ID
     * @return 交易记录实体
     */
    Transaction findById(@Param("id") Long id);

    /**
     * 分页查询账本的交易记录
     *
     * @param accountBookId 账本ID
     * @param startDate     开始日期（可选）
     * @param endDate       结束日期（可选）
     * @param type          类型（可选）
     * @param offset        偏移量
     * @param limit         每页数量
     * @return 交易记录列表
     */
    List<Transaction> findByAccountBookId(
            @Param("accountBookId") Long accountBookId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("type") Integer type,
            @Param("offset") int offset,
            @Param("limit") int limit);

    /**
     * 统计账本的交易记录数量
     *
     * @param accountBookId 账本ID
     * @param startDate     开始日期（可选）
     * @param endDate       结束日期（可选）
     * @param type          类型（可选）
     * @return 记录数量
     */
    int countByAccountBookId(
            @Param("accountBookId") Long accountBookId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("type") Integer type);

    /**
     * 根据用户ID查询交易记录
     *
     * @param userId 用户ID
     * @param offset 偏移量
     * @param limit  每页数量
     * @return 交易记录列表
     */
    List<Transaction> findByUserId(
            @Param("userId") Long userId,
            @Param("offset") int offset,
            @Param("limit") int limit);

    /**
     * 根据分类ID查询交易记录数量
     *
     * @param categoryId 分类ID
     * @return 记录数量
     */
    int countByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 根据账本ID查询交易记录数量
     *
     * @param accountBookId 账本ID
     * @return 记录数量
     */
    int countByAccountBook(@Param("accountBookId") Long accountBookId);

    /**
     * 插入新交易记录
     *
     * @param transaction 交易记录实体
     * @return 影响行数
     */
    int insert(Transaction transaction);

    /**
     * 更新交易记录
     *
     * @param transaction 交易记录实体
     * @return 影响行数
     */
    int update(Transaction transaction);

    /**
     * 删除交易记录
     *
     * @param id 交易ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据账本ID删除所有交易记录
     *
     * @param accountBookId 账本ID
     * @return 影响行数
     */
    int deleteByAccountBookId(@Param("accountBookId") Long accountBookId);
}
