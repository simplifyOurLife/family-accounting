package com.family.accounting.controller;

import com.family.accounting.dto.*;
import com.family.accounting.security.SecurityUtils;
import com.family.accounting.service.FamilyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 家庭管理控制器
 */
@RestController
@RequestMapping("/api/family")
public class FamilyController {

    @Autowired
    private FamilyService familyService;

    /**
     * 创建家庭
     *
     * @param dto 家庭信息
     * @return 家庭信息
     */
    @PostMapping
    public Result<FamilyVO> createFamily(@Valid @RequestBody FamilyDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        System.out.println("=== Creating family ===");
        System.out.println("userId: " + userId);
        System.out.println("familyName: " + dto.getName());
        try {
            FamilyVO familyVO = familyService.createFamily(userId, dto);
            System.out.println("Family created successfully: " + familyVO.getId());
            return Result.success("创建成功", familyVO);
        } catch (Exception e) {
            System.out.println("Failed to create family: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 获取当前家庭信息
     *
     * @return 家庭信息
     */
    @GetMapping
    public Result<FamilyVO> getCurrentFamily() {
        Long userId = SecurityUtils.getCurrentUserId();
        FamilyVO familyVO = familyService.getCurrentFamily(userId);
        return Result.success(familyVO);
    }

    /**
     * 更新家庭信息
     *
     * @param dto 家庭信息
     * @return 更新后的家庭信息
     */
    @PutMapping
    public Result<FamilyVO> updateFamily(@Valid @RequestBody FamilyDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        FamilyVO familyVO = familyService.updateFamily(userId, dto);
        return Result.success("更新成功", familyVO);
    }

    /**
     * 邀请成员
     *
     * @param dto 邀请信息
     * @return 成功响应
     */
    @PostMapping("/invite")
    public Result<Void> inviteMember(@Valid @RequestBody InviteDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        familyService.inviteMember(userId, dto);
        return Result.success("邀请已发送", null);
    }

    /**
     * 获取用户收到的待处理邀请列表
     *
     * @return 邀请列表
     */
    @GetMapping("/invitations")
    public Result<List<InvitationVO>> getPendingInvitations() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<InvitationVO> invitations = familyService.getPendingInvitations(userId);
        return Result.success(invitations);
    }

    /**
     * 接受邀请
     *
     * @param id 邀请ID
     * @return 成功响应
     */
    @PostMapping("/invitation/{id}/accept")
    public Result<Void> acceptInvitation(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        familyService.acceptInvitation(userId, id);
        return Result.success("已加入家庭", null);
    }

    /**
     * 拒绝邀请
     *
     * @param id 邀请ID
     * @return 成功响应
     */
    @PostMapping("/invitation/{id}/decline")
    public Result<Void> declineInvitation(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        familyService.declineInvitation(userId, id);
        return Result.success("已拒绝邀请", null);
    }

    /**
     * 获取家庭成员列表
     *
     * @return 成员列表
     */
    @GetMapping("/members")
    public Result<List<MemberVO>> getMembers() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<MemberVO> members = familyService.getMembers(userId);
        return Result.success(members);
    }

    /**
     * 设置成员昵称
     *
     * @param id  成员记录ID
     * @param dto 昵称信息
     * @return 成功响应
     */
    @PutMapping("/member/{id}/nickname")
    public Result<Void> setMemberNickname(@PathVariable Long id, @Valid @RequestBody NicknameDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        familyService.setMemberNickname(userId, id, dto.getNickname());
        return Result.success("昵称已更新", null);
    }

    /**
     * 移除成员
     *
     * @param id 成员记录ID
     * @return 成功响应
     */
    @DeleteMapping("/member/{id}")
    public Result<Void> removeMember(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        familyService.removeMember(userId, id);
        return Result.success("成员已移除", null);
    }
}
