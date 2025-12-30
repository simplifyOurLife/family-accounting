package com.family.accounting.mapper;

import com.family.accounting.entity.AccountBook;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 账本数据访问接口
 */
@Mapper
public interface AccountBookMapper {

    /**
     * 根据ID查询账本
     *
     * @param id 账本ID
     * @return 账本实体
     */
    AccountBook findById(@Param("id") Long id);

    /**
     * 根据家庭ID查询所有账本
     *
     * @param familyId 家庭ID
     * @return 账本列表
     */
    List<AccountBook> findByFamilyId(@Param("familyId") Long familyId);

    /**
     * 根据家庭ID查询默认账本
     *
     * @param familyId 家庭ID
     * @return 默认账本
     */
    AccountBook findDefaultByFamilyId(@Param("familyId") Long familyId);

    /**
     * 插入新账本
     *
     * @param accountBook 账本实体
     * @return 影响行数
     */
    int insert(AccountBook accountBook);

    /**
     * 更新账本
     *
     * @param accountBook 账本实体
     * @return 影响行数
     */
    int update(AccountBook accountBook);

    /**
     * 删除账本
     *
     * @param id 账本ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 设置默认账本（先清除其他默认）
     *
     * @param familyId 家庭ID
     * @param id       账本ID
     * @return 影响行数
     */
    int setDefault(@Param("familyId") Long familyId, @Param("id") Long id);

    /**
     * 清除家庭的默认账本标记
     *
     * @param familyId 家庭ID
     * @return 影响行数
     */
    int clearDefault(@Param("familyId") Long familyId);

    /**
     * 统计家庭账本数量
     *
     * @param familyId 家庭ID
     * @return 账本数量
     */
    int countByFamilyId(@Param("familyId") Long familyId);

    /**
     * 统计账本下的交易数量
     *
     * @param accountBookId 账本ID
     * @return 交易数量
     */
    int countTransactionsByAccountBookId(@Param("accountBookId") Long accountBookId);
}
