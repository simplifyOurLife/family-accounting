package com.family.accounting.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 邀请信息视图对象
 */
@Data
public class InvitationVO {

    /**
     * 邀请ID
     */
    private Long id;

    /**
     * 家庭ID
     */
    private Long familyId;

    /**
     * 家庭名称
     */
    private String familyName;

    /**
     * 邀请人ID
     */
    private Long inviterId;

    /**
     * 邀请人昵称
     */
    private String inviterNickname;

    /**
     * 被邀请人ID
     */
    private Long inviteeId;

    /**
     * 被邀请人昵称
     */
    private String inviteeNickname;

    /**
     * 被邀请人手机号
     */
    private String inviteePhone;

    /**
     * 状态: 0-待处理 1-已接受 2-已拒绝 3-已过期
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String statusText;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 过期时间
     */
    private LocalDateTime expiredAt;
}
