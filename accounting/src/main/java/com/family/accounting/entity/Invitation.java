package com.family.accounting.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 邀请实体类
 * 对应数据库表 t_invitation
 */
@Data
public class Invitation {

    /**
     * 邀请ID
     */
    private Long id;

    /**
     * 家庭ID
     */
    private Long familyId;

    /**
     * 邀请人ID
     */
    private Long inviterId;

    /**
     * 被邀请人ID
     */
    private Long inviteeId;

    /**
     * 状态: 0-待处理 1-已接受 2-已拒绝 3-已过期
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 过期时间
     */
    private LocalDateTime expiredAt;

    // 邀请状态常量
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_ACCEPTED = 1;
    public static final int STATUS_DECLINED = 2;
    public static final int STATUS_EXPIRED = 3;
}
