package com.family.accounting.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 家庭成员实体类
 * 对应数据库表 t_family_member
 */
@Data
public class FamilyMember {

    /**
     * 成员记录ID
     */
    private Long id;

    /**
     * 家庭ID
     */
    private Long familyId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 在家庭中的昵称
     */
    private String nickname;

    /**
     * 加入时间
     */
    private LocalDateTime joinedAt;
}
