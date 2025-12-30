package com.family.accounting.service;

import com.family.accounting.config.DefaultCategoryConfig;
import com.family.accounting.dto.*;
import com.family.accounting.entity.*;
import com.family.accounting.exception.BusinessException;
import com.family.accounting.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 家庭服务类
 */
@Service
public class FamilyService {

    @Autowired
    private FamilyMapper familyMapper;

    @Autowired
    private FamilyMemberMapper familyMemberMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private AccountBookMapper accountBookMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private InvitationMapper invitationMapper;

    /**
     * 创建家庭
     *
     * @param userId 创建者用户ID
     * @param dto    家庭信息
     * @return 家庭信息
     */
    @Transactional
    public FamilyVO createFamily(Long userId, FamilyDTO dto) {
        // 检查用户是否已有家庭
        FamilyMember existingMember = familyMemberMapper.findByUserId(userId);
        if (existingMember != null) {
            throw new BusinessException("您已经是一个家庭的成员，不能创建新家庭");
        }

        // 创建家庭
        Family family = new Family();
        family.setName(dto.getName());
        family.setAdminId(userId);
        familyMapper.insert(family);

        // 将创建者添加为家庭成员
        FamilyMember member = new FamilyMember();
        member.setFamilyId(family.getId());
        member.setUserId(userId);
        familyMemberMapper.insert(member);

        // 创建默认分类
        createDefaultCategories(family.getId());

        // 创建默认账本
        createDefaultAccountBook(family.getId());

        // 返回家庭信息
        return convertToVO(family, userId);
    }

    /**
     * 获取当前用户的家庭信息
     *
     * @param userId 用户ID
     * @return 家庭信息
     */
    public FamilyVO getCurrentFamily(Long userId) {
        FamilyMember member = familyMemberMapper.findByUserId(userId);
        if (member == null) {
            return null;
        }

        Family family = familyMapper.findById(member.getFamilyId());
        if (family == null) {
            return null;
        }

        return convertToVO(family, userId);
    }

    /**
     * 更新家庭信息
     *
     * @param userId 用户ID
     * @param dto    家庭信息
     * @return 更新后的家庭信息
     */
    @Transactional
    public FamilyVO updateFamily(Long userId, FamilyDTO dto) {
        Family family = getFamilyByAdmin(userId);

        family.setName(dto.getName());
        familyMapper.update(family);

        return convertToVO(family, userId);
    }

    /**
     * 获取家庭成员列表
     *
     * @param userId 用户ID
     * @return 成员列表
     */
    public List<MemberVO> getMembers(Long userId) {
        FamilyMember currentMember = familyMemberMapper.findByUserId(userId);
        if (currentMember == null) {
            throw new BusinessException("您还没有加入任何家庭");
        }

        Family family = familyMapper.findById(currentMember.getFamilyId());
        List<FamilyMember> members = familyMemberMapper.findByFamilyId(currentMember.getFamilyId());

        return members.stream()
                .map(m -> convertToMemberVO(m, family.getAdminId()))
                .collect(Collectors.toList());
    }

    /**
     * 邀请成员加入家庭
     *
     * @param userId 邀请人ID（必须是管理员）
     * @param dto    邀请信息
     */
    @Transactional
    public void inviteMember(Long userId, InviteDTO dto) {
        // 验证邀请人是管理员
        Family family = getFamilyByAdmin(userId);

        // 查找被邀请人
        User invitee = userMapper.findByPhone(dto.getPhone());
        if (invitee == null) {
            throw new BusinessException("该用户不存在");
        }

        // 不能邀请自己
        if (invitee.getId().equals(userId)) {
            throw new BusinessException("不能邀请自己");
        }

        // 检查被邀请人是否已经是家庭成员
        FamilyMember existingMember = familyMemberMapper.findByUserId(invitee.getId());
        if (existingMember != null) {
            throw new BusinessException("该用户已经是一个家庭的成员");
        }

        // 检查是否已有待处理的邀请
        if (invitationMapper.existsPendingInvitation(family.getId(), invitee.getId()) > 0) {
            throw new BusinessException("已经向该用户发送过邀请，请等待处理");
        }

        // 创建邀请
        Invitation invitation = new Invitation();
        invitation.setFamilyId(family.getId());
        invitation.setInviterId(userId);
        invitation.setInviteeId(invitee.getId());
        invitation.setStatus(Invitation.STATUS_PENDING);
        invitation.setExpiredAt(LocalDateTime.now().plusDays(7)); // 7天后过期
        invitationMapper.insert(invitation);
    }

    /**
     * 获取用户收到的待处理邀请列表
     *
     * @param userId 用户ID
     * @return 邀请列表
     */
    public List<InvitationVO> getPendingInvitations(Long userId) {
        List<Invitation> invitations = invitationMapper.findPendingByInviteeId(userId);
        return invitations.stream()
                .map(this::convertToInvitationVO)
                .collect(Collectors.toList());
    }

    /**
     * 获取家庭发出的邀请列表
     *
     * @param userId 用户ID（必须是管理员）
     * @return 邀请列表
     */
    public List<InvitationVO> getFamilyInvitations(Long userId) {
        Family family = getFamilyByAdmin(userId);
        List<Invitation> invitations = invitationMapper.findByFamilyId(family.getId());
        return invitations.stream()
                .map(this::convertToInvitationVO)
                .collect(Collectors.toList());
    }

    /**
     * 接受邀请
     *
     * @param userId       用户ID
     * @param invitationId 邀请ID
     */
    @Transactional
    public void acceptInvitation(Long userId, Long invitationId) {
        Invitation invitation = getValidInvitation(userId, invitationId);

        // 检查用户是否已有家庭
        FamilyMember existingMember = familyMemberMapper.findByUserId(userId);
        if (existingMember != null) {
            throw new BusinessException("您已经是一个家庭的成员");
        }

        // 更新邀请状态
        invitationMapper.updateStatus(invitationId, Invitation.STATUS_ACCEPTED);

        // 添加为家庭成员
        FamilyMember member = new FamilyMember();
        member.setFamilyId(invitation.getFamilyId());
        member.setUserId(userId);
        familyMemberMapper.insert(member);
    }

    /**
     * 拒绝邀请
     *
     * @param userId       用户ID
     * @param invitationId 邀请ID
     */
    @Transactional
    public void declineInvitation(Long userId, Long invitationId) {
        getValidInvitation(userId, invitationId);

        // 更新邀请状态
        invitationMapper.updateStatus(invitationId, Invitation.STATUS_DECLINED);
    }

    /**
     * 获取并验证邀请
     */
    private Invitation getValidInvitation(Long userId, Long invitationId) {
        Invitation invitation = invitationMapper.findById(invitationId);
        if (invitation == null) {
            throw new BusinessException("邀请不存在");
        }

        if (!invitation.getInviteeId().equals(userId)) {
            throw new BusinessException("无权操作此邀请");
        }

        if (invitation.getStatus() != Invitation.STATUS_PENDING) {
            throw new BusinessException("该邀请已处理");
        }

        // 检查是否过期
        if (invitation.getExpiredAt() != null && invitation.getExpiredAt().isBefore(LocalDateTime.now())) {
            invitationMapper.updateStatus(invitationId, Invitation.STATUS_EXPIRED);
            throw new BusinessException("该邀请已过期");
        }

        return invitation;
    }

    /**
     * 设置成员昵称
     *
     * @param adminId  管理员用户ID
     * @param memberId 成员记录ID
     * @param nickname 新昵称
     */
    @Transactional
    public void setMemberNickname(Long adminId, Long memberId, String nickname) {
        // 验证是管理员
        Family family = getFamilyByAdmin(adminId);

        // 获取成员
        FamilyMember member = familyMemberMapper.findById(memberId);
        if (member == null) {
            throw new BusinessException("成员不存在");
        }

        // 验证成员属于该家庭
        if (!member.getFamilyId().equals(family.getId())) {
            throw new BusinessException("该成员不属于您的家庭");
        }

        // 更新昵称
        familyMemberMapper.updateNickname(memberId, nickname);
    }

    /**
     * 移除家庭成员
     *
     * @param adminId  管理员用户ID
     * @param memberId 成员记录ID
     */
    @Transactional
    public void removeMember(Long adminId, Long memberId) {
        // 验证是管理员
        Family family = getFamilyByAdmin(adminId);

        // 获取成员
        FamilyMember member = familyMemberMapper.findById(memberId);
        if (member == null) {
            throw new BusinessException("成员不存在");
        }

        // 验证成员属于该家庭
        if (!member.getFamilyId().equals(family.getId())) {
            throw new BusinessException("该成员不属于您的家庭");
        }

        // 不能移除管理员自己
        if (member.getUserId().equals(adminId)) {
            throw new BusinessException("不能移除管理员");
        }

        // 删除成员
        familyMemberMapper.deleteById(memberId);
    }

    /**
     * 根据管理员ID获取家庭
     */
    private Family getFamilyByAdmin(Long adminId) {
        Family family = familyMapper.findByAdminId(adminId);
        if (family == null) {
            throw new BusinessException("您不是任何家庭的管理员");
        }
        return family;
    }

    /**
     * 创建默认分类
     */
    private void createDefaultCategories(Long familyId) {
        // 创建支出分类
        for (DefaultCategoryConfig.CategoryTemplate template : DefaultCategoryConfig.DEFAULT_EXPENSE_CATEGORIES) {
            createCategoryFromTemplate(familyId, null, template, Category.TYPE_EXPENSE);
        }

        // 创建收入分类
        for (DefaultCategoryConfig.CategoryTemplate template : DefaultCategoryConfig.DEFAULT_INCOME_CATEGORIES) {
            createCategoryFromTemplate(familyId, null, template, Category.TYPE_INCOME);
        }
    }

    /**
     * 从模板创建分类
     */
    private void createCategoryFromTemplate(Long familyId, Long parentId,
                                            DefaultCategoryConfig.CategoryTemplate template, int type) {
        Category category = new Category();
        category.setFamilyId(familyId);
        category.setParentId(parentId);
        category.setName(template.getName());
        category.setType(type);
        category.setIcon(template.getIcon());
        category.setSortOrder(template.getSortOrder());
        categoryMapper.insert(category);

        // 递归创建子分类
        if (template.hasChildren()) {
            for (DefaultCategoryConfig.CategoryTemplate child : template.getChildren()) {
                createCategoryFromTemplate(familyId, category.getId(), child, type);
            }
        }
    }

    /**
     * 创建默认账本
     */
    private void createDefaultAccountBook(Long familyId) {
        AccountBook accountBook = new AccountBook();
        accountBook.setFamilyId(familyId);
        accountBook.setName("日常账本");
        accountBook.setIsDefault(1);
        accountBookMapper.insert(accountBook);
    }

    /**
     * 将Family实体转换为FamilyVO
     */
    private FamilyVO convertToVO(Family family, Long currentUserId) {
        FamilyVO vo = new FamilyVO();
        vo.setId(family.getId());
        vo.setName(family.getName());
        vo.setAdminId(family.getAdminId());
        vo.setCreatedAt(family.getCreatedAt());

        // 获取管理员昵称
        User admin = userMapper.findById(family.getAdminId());
        if (admin != null) {
            vo.setAdminNickname(admin.getNickname() != null ? admin.getNickname() : admin.getPhone());
        }

        // 获取成员数量
        vo.setMemberCount(familyMemberMapper.countByFamilyId(family.getId()));

        return vo;
    }

    /**
     * 将FamilyMember实体转换为MemberVO
     */
    private MemberVO convertToMemberVO(FamilyMember member, Long adminId) {
        MemberVO vo = new MemberVO();
        vo.setId(member.getId());
        vo.setUserId(member.getUserId());
        vo.setNickname(member.getNickname());
        vo.setJoinedAt(member.getJoinedAt());
        vo.setIsAdmin(member.getUserId().equals(adminId));

        // 获取用户信息
        User user = userMapper.findById(member.getUserId());
        if (user != null) {
            vo.setPhone(user.getPhone());
            vo.setUserNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
        }

        return vo;
    }

    /**
     * 将Invitation实体转换为InvitationVO
     */
    private InvitationVO convertToInvitationVO(Invitation invitation) {
        InvitationVO vo = new InvitationVO();
        vo.setId(invitation.getId());
        vo.setFamilyId(invitation.getFamilyId());
        vo.setInviterId(invitation.getInviterId());
        vo.setInviteeId(invitation.getInviteeId());
        vo.setStatus(invitation.getStatus());
        vo.setCreatedAt(invitation.getCreatedAt());
        vo.setExpiredAt(invitation.getExpiredAt());

        // 获取家庭名称
        Family family = familyMapper.findById(invitation.getFamilyId());
        if (family != null) {
            vo.setFamilyName(family.getName());
        }

        // 获取邀请人信息
        User inviter = userMapper.findById(invitation.getInviterId());
        if (inviter != null) {
            vo.setInviterNickname(inviter.getNickname() != null ? inviter.getNickname() : inviter.getPhone());
        }

        // 获取被邀请人信息
        User invitee = userMapper.findById(invitation.getInviteeId());
        if (invitee != null) {
            vo.setInviteeNickname(invitee.getNickname() != null ? invitee.getNickname() : invitee.getPhone());
            vo.setInviteePhone(invitee.getPhone());
        }

        // 设置状态描述
        vo.setStatusText(getStatusText(invitation.getStatus()));

        return vo;
    }

    /**
     * 获取邀请状态描述
     */
    private String getStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case Invitation.STATUS_PENDING:
                return "待处理";
            case Invitation.STATUS_ACCEPTED:
                return "已接受";
            case Invitation.STATUS_DECLINED:
                return "已拒绝";
            case Invitation.STATUS_EXPIRED:
                return "已过期";
            default:
                return "未知";
        }
    }
}
