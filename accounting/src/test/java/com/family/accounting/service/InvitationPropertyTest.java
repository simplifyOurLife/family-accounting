package com.family.accounting.service;

import com.family.accounting.dto.InviteDTO;
import com.family.accounting.dto.MemberVO;
import com.family.accounting.entity.*;
import com.family.accounting.exception.BusinessException;
import com.family.accounting.mapper.*;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 邀请处理属性测试
 * <p>
 * Feature: family-accounting
 * Property 4: 邀请处理状态一致性
 * Validates: Requirements 2.3, 2.4
 */
class InvitationPropertyTest {

    /**
     * Property 4: 邀请处理状态一致性 - 接受邀请
     * For any valid family invitation, accepting it should add the user to the family members list.
     * <p>
     * **Validates: Requirements 2.3**
     */
    @Property(tries = 100)
    void acceptInvitationShouldAddUserToFamily(
            @ForAll @LongRange(min = 1, max = 10000) Long inviteeId,
            @ForAll @LongRange(min = 1, max = 10000) Long familyId,
            @ForAll @LongRange(min = 1, max = 10000) Long invitationId
    ) {
        // Given: mock mappers
        FamilyMapper familyMapper = Mockito.mock(FamilyMapper.class);
        FamilyMemberMapper familyMemberMapper = Mockito.mock(FamilyMemberMapper.class);
        CategoryMapper categoryMapper = Mockito.mock(CategoryMapper.class);
        AccountBookMapper accountBookMapper = Mockito.mock(AccountBookMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        InvitationMapper invitationMapper = Mockito.mock(InvitationMapper.class);

        FamilyService familyService = new FamilyService();
        setField(familyService, "familyMapper", familyMapper);
        setField(familyService, "familyMemberMapper", familyMemberMapper);
        setField(familyService, "categoryMapper", categoryMapper);
        setField(familyService, "accountBookMapper", accountBookMapper);
        setField(familyService, "userMapper", userMapper);
        setField(familyService, "invitationMapper", invitationMapper);

        // Setup: valid pending invitation
        Invitation invitation = new Invitation();
        invitation.setId(invitationId);
        invitation.setFamilyId(familyId);
        invitation.setInviteeId(inviteeId);
        invitation.setStatus(Invitation.STATUS_PENDING);
        invitation.setExpiredAt(LocalDateTime.now().plusDays(7));
        when(invitationMapper.findById(invitationId)).thenReturn(invitation);

        // Setup: user has no existing family
        when(familyMemberMapper.findByUserId(inviteeId)).thenReturn(null);

        // Setup: capture member insert
        AtomicLong memberIdCounter = new AtomicLong(1);
        doAnswer(inv -> {
            FamilyMember member = inv.getArgument(0);
            member.setId(memberIdCounter.getAndIncrement());
            return 1;
        }).when(familyMemberMapper).insert(any(FamilyMember.class));

        // When: accepting the invitation
        familyService.acceptInvitation(inviteeId, invitationId);

        // Then: invitation status should be updated to accepted
        verify(invitationMapper).updateStatus(invitationId, Invitation.STATUS_ACCEPTED);

        // And: user should be added as family member
        ArgumentCaptor<FamilyMember> memberCaptor = ArgumentCaptor.forClass(FamilyMember.class);
        verify(familyMemberMapper).insert(memberCaptor.capture());
        assertEquals(familyId, memberCaptor.getValue().getFamilyId());
        assertEquals(inviteeId, memberCaptor.getValue().getUserId());
    }

    /**
     * Property 4: 邀请处理状态一致性 - 拒绝邀请
     * For any valid family invitation, declining it should update the invitation status
     * without adding the user to the family.
     * <p>
     * **Validates: Requirements 2.4**
     */
    @Property(tries = 100)
    void declineInvitationShouldNotAddUserToFamily(
            @ForAll @LongRange(min = 1, max = 10000) Long inviteeId,
            @ForAll @LongRange(min = 1, max = 10000) Long familyId,
            @ForAll @LongRange(min = 1, max = 10000) Long invitationId
    ) {
        // Given: mock mappers
        FamilyMapper familyMapper = Mockito.mock(FamilyMapper.class);
        FamilyMemberMapper familyMemberMapper = Mockito.mock(FamilyMemberMapper.class);
        CategoryMapper categoryMapper = Mockito.mock(CategoryMapper.class);
        AccountBookMapper accountBookMapper = Mockito.mock(AccountBookMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        InvitationMapper invitationMapper = Mockito.mock(InvitationMapper.class);

        FamilyService familyService = new FamilyService();
        setField(familyService, "familyMapper", familyMapper);
        setField(familyService, "familyMemberMapper", familyMemberMapper);
        setField(familyService, "categoryMapper", categoryMapper);
        setField(familyService, "accountBookMapper", accountBookMapper);
        setField(familyService, "userMapper", userMapper);
        setField(familyService, "invitationMapper", invitationMapper);

        // Setup: valid pending invitation
        Invitation invitation = new Invitation();
        invitation.setId(invitationId);
        invitation.setFamilyId(familyId);
        invitation.setInviteeId(inviteeId);
        invitation.setStatus(Invitation.STATUS_PENDING);
        invitation.setExpiredAt(LocalDateTime.now().plusDays(7));
        when(invitationMapper.findById(invitationId)).thenReturn(invitation);

        // When: declining the invitation
        familyService.declineInvitation(inviteeId, invitationId);

        // Then: invitation status should be updated to declined
        verify(invitationMapper).updateStatus(invitationId, Invitation.STATUS_DECLINED);

        // And: user should NOT be added as family member
        verify(familyMemberMapper, never()).insert(any(FamilyMember.class));
    }

    /**
     * Property 4: 邀请处理状态一致性 - 已处理邀请不能再次处理
     * For any invitation that has already been processed, attempting to accept or decline
     * should be rejected.
     * <p>
     * **Validates: Requirements 2.3, 2.4**
     */
    @Property(tries = 100)
    void processedInvitationShouldNotBeProcessedAgain(
            @ForAll @LongRange(min = 1, max = 10000) Long inviteeId,
            @ForAll @LongRange(min = 1, max = 10000) Long invitationId,
            @ForAll("processedStatuses") Integer processedStatus
    ) {
        // Given: mock mappers
        FamilyMapper familyMapper = Mockito.mock(FamilyMapper.class);
        FamilyMemberMapper familyMemberMapper = Mockito.mock(FamilyMemberMapper.class);
        CategoryMapper categoryMapper = Mockito.mock(CategoryMapper.class);
        AccountBookMapper accountBookMapper = Mockito.mock(AccountBookMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        InvitationMapper invitationMapper = Mockito.mock(InvitationMapper.class);

        FamilyService familyService = new FamilyService();
        setField(familyService, "familyMapper", familyMapper);
        setField(familyService, "familyMemberMapper", familyMemberMapper);
        setField(familyService, "categoryMapper", categoryMapper);
        setField(familyService, "accountBookMapper", accountBookMapper);
        setField(familyService, "userMapper", userMapper);
        setField(familyService, "invitationMapper", invitationMapper);

        // Setup: already processed invitation
        Invitation invitation = new Invitation();
        invitation.setId(invitationId);
        invitation.setInviteeId(inviteeId);
        invitation.setStatus(processedStatus);
        when(invitationMapper.findById(invitationId)).thenReturn(invitation);

        // When/Then: attempting to accept should throw exception
        assertThrows(BusinessException.class,
                () -> familyService.acceptInvitation(inviteeId, invitationId));

        // And: attempting to decline should throw exception
        assertThrows(BusinessException.class,
                () -> familyService.declineInvitation(inviteeId, invitationId));

        // And: no status update should occur
        verify(invitationMapper, never()).updateStatus(any(), any());

        // And: no member should be added
        verify(familyMemberMapper, never()).insert(any(FamilyMember.class));
    }

    /**
     * 生成已处理的邀请状态
     */
    @Provide
    Arbitrary<Integer> processedStatuses() {
        return Arbitraries.of(
                Invitation.STATUS_ACCEPTED,
                Invitation.STATUS_DECLINED,
                Invitation.STATUS_EXPIRED
        );
    }

    /**
     * 使用反射设置私有字段
     */
    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }
}
