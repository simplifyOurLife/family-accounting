package com.family.accounting.mapper;

import com.family.accounting.entity.Invitation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 邀请数据访问接口
 */
@Mapper
public interface InvitationMapper {

    /**
     * 根据ID查询邀请
     *
     * @param id 邀请ID
     * @return 邀请实体
     */
    Invitation findById(@Param("id") Long id);

    /**
     * 根据被邀请人ID查询待处理邀请列表
     *
     * @param inviteeId 被邀请人ID
     * @return 邀请列表
     */
    List<Invitation> findPendingByInviteeId(@Param("inviteeId") Long inviteeId);

    /**
     * 根据家庭ID查询邀请列表
     *
     * @param familyId 家庭ID
     * @return 邀请列表
     */
    List<Invitation> findByFamilyId(@Param("familyId") Long familyId);

    /**
     * 检查是否存在待处理的邀请
     *
     * @param familyId  家庭ID
     * @param inviteeId 被邀请人ID
     * @return 存在返回1，不存在返回0
     */
    int existsPendingInvitation(@Param("familyId") Long familyId, @Param("inviteeId") Long inviteeId);

    /**
     * 插入新邀请
     *
     * @param invitation 邀请实体
     * @return 影响行数
     */
    int insert(Invitation invitation);

    /**
     * 更新邀请状态
     *
     * @param id     邀请ID
     * @param status 新状态
     * @return 影响行数
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 删除邀请
     *
     * @param id 邀请ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);
}
