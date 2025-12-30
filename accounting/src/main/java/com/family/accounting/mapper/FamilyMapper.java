package com.family.accounting.mapper;

import com.family.accounting.entity.Family;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 家庭数据访问接口
 */
@Mapper
public interface FamilyMapper {

    /**
     * 根据ID查询家庭
     *
     * @param id 家庭ID
     * @return 家庭实体
     */
    Family findById(@Param("id") Long id);

    /**
     * 根据管理员ID查询家庭
     *
     * @param adminId 管理员用户ID
     * @return 家庭实体
     */
    Family findByAdminId(@Param("adminId") Long adminId);

    /**
     * 插入新家庭
     *
     * @param family 家庭实体
     * @return 影响行数
     */
    int insert(Family family);

    /**
     * 更新家庭信息
     *
     * @param family 家庭实体
     * @return 影响行数
     */
    int update(Family family);

    /**
     * 删除家庭
     *
     * @param id 家庭ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);
}
