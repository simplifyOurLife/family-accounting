package com.family.accounting.mapper;

import com.family.accounting.entity.FamilyMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 家庭成员数据访问接口
 */
@Mapper
public interface FamilyMemberMapper {

    /**
     * 根据ID查询成员
     *
     * @param id 成员记录ID
     * @return 成员实体
     */
    FamilyMember findById(@Param("id") Long id);

    /**
     * 根据家庭ID和用户ID查询成员
     *
     * @param familyId 家庭ID
     * @param userId   用户ID
     * @return 成员实体
     */
    FamilyMember findByFamilyIdAndUserId(@Param("familyId") Long familyId, @Param("userId") Long userId);

    /**
     * 根据用户ID查询成员记录（判断用户是否已有家庭）
     *
     * @param userId 用户ID
     * @return 成员实体
     */
    FamilyMember findByUserId(@Param("userId") Long userId);

    /**
     * 根据家庭ID查询所有成员
     *
     * @param familyId 家庭ID
     * @return 成员列表
     */
    List<FamilyMember> findByFamilyId(@Param("familyId") Long familyId);

    /**
     * 插入新成员
     *
     * @param member 成员实体
     * @return 影响行数
     */
    int insert(FamilyMember member);

    /**
     * 更新成员昵称
     *
     * @param id       成员记录ID
     * @param nickname 新昵称
     * @return 影响行数
     */
    int updateNickname(@Param("id") Long id, @Param("nickname") String nickname);

    /**
     * 删除成员
     *
     * @param id 成员记录ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据家庭ID和用户ID删除成员
     *
     * @param familyId 家庭ID
     * @param userId   用户ID
     * @return 影响行数
     */
    int deleteByFamilyIdAndUserId(@Param("familyId") Long familyId, @Param("userId") Long userId);

    /**
     * 统计家庭成员数量
     *
     * @param familyId 家庭ID
     * @return 成员数量
     */
    int countByFamilyId(@Param("familyId") Long familyId);
}
