package com.family.accounting.service;

import com.family.accounting.entity.User;
import com.family.accounting.mapper.UserMapper;
import com.family.accounting.util.JwtUtil;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 密码修改令牌失效属性测试
 * <p>
 * Feature: family-accounting, Property 22: 密码修改令牌失效
 * Validates: Requirements 7.1
 */
class PasswordChangeTokenInvalidationPropertyTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Property 22: 密码修改令牌失效
     * For any successful password change, all existing authentication tokens for that user should be invalidated.
     */
    @Property(tries = 10)
    void passwordChangeShouldInvalidateAllUserTokens(
            @ForAll @LongRange(min = 1, max = 10000) Long userId,
            @ForAll @StringLength(min = 6, max = 20) String oldPassword,
            @ForAll @StringLength(min = 6, max = 20) String newPassword,
            @ForAll @StringLength(min = 10, max = 50) String phone
    ) {
        // Skip if old and new passwords are the same
        Assume.that(!oldPassword.equals(newPassword));

        // Given: mock dependencies
        UserMapper mockMapper = Mockito.mock(UserMapper.class);
        JwtUtil mockJwtUtil = Mockito.mock(JwtUtil.class);
        SecurityService mockSecurityService = Mockito.mock(SecurityService.class);
        TokenBlacklistService mockTokenBlacklistService = Mockito.mock(TokenBlacklistService.class);

        UserService userService = new UserService();
        setField(userService, "userMapper", mockMapper);
        setField(userService, "passwordEncoder", passwordEncoder);
        setField(userService, "jwtUtil", mockJwtUtil);
        setField(userService, "securityService", mockSecurityService);
        setField(userService, "tokenBlacklistService", mockTokenBlacklistService);

        // Setup: user exists with old password
        String encodedOldPassword = passwordEncoder.encode(oldPassword);
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setPhone(phone);
        existingUser.setPassword(encodedOldPassword);

        when(mockMapper.findById(userId)).thenReturn(existingUser);
        when(mockMapper.updatePassword(eq(userId), anyString())).thenReturn(1);

        // When: changing password with correct old password
        userService.changePassword(userId, oldPassword, newPassword);

        // Then: password should be updated in database
        verify(mockMapper).updatePassword(eq(userId), anyString());

        // And: all user tokens should be invalidated
        verify(mockTokenBlacklistService).invalidateAllUserTokens(eq(userId), eq("密码修改"));
    }

    /**
     * Property 22: 密码修改令牌失效 - 验证令牌黑名单机制
     * For any token that was valid before password change, it should be blacklisted after password change.
     */
    @Property(tries = 10)
    void tokenShouldBeBlacklistedAfterPasswordChange(
            @ForAll @LongRange(min = 1, max = 10000) Long userId,
            @ForAll @StringLength(min = 6, max = 20) String oldPassword,
            @ForAll @StringLength(min = 6, max = 20) String newPassword,
            @ForAll @StringLength(min = 10, max = 50) String phone,
            @ForAll @StringLength(min = 20, max = 100) String existingToken
    ) {
        // Skip if old and new passwords are the same
        Assume.that(!oldPassword.equals(newPassword));

        // Given: mock dependencies
        UserMapper mockMapper = Mockito.mock(UserMapper.class);
        JwtUtil mockJwtUtil = Mockito.mock(JwtUtil.class);
        SecurityService mockSecurityService = Mockito.mock(SecurityService.class);
        TokenBlacklistService mockTokenBlacklistService = Mockito.mock(TokenBlacklistService.class);

        UserService userService = new UserService();
        setField(userService, "userMapper", mockMapper);
        setField(userService, "passwordEncoder", passwordEncoder);
        setField(userService, "jwtUtil", mockJwtUtil);
        setField(userService, "securityService", mockSecurityService);
        setField(userService, "tokenBlacklistService", mockTokenBlacklistService);

        // Setup: user exists with old password
        String encodedOldPassword = passwordEncoder.encode(oldPassword);
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setPhone(phone);
        existingUser.setPassword(encodedOldPassword);

        when(mockMapper.findById(userId)).thenReturn(existingUser);
        when(mockMapper.updatePassword(eq(userId), anyString())).thenReturn(1);

        // Setup: token was valid before password change
        when(mockJwtUtil.validateToken(existingToken)).thenReturn(true);
        when(mockJwtUtil.getUserIdFromToken(existingToken)).thenReturn(userId);
        when(mockJwtUtil.getExpirationFromToken(existingToken)).thenReturn(new Date(System.currentTimeMillis() + 3600000)); // 1 hour from now

        // Initially token is not blacklisted
        when(mockTokenBlacklistService.isTokenBlacklisted(existingToken)).thenReturn(false);

        // When: changing password
        userService.changePassword(userId, oldPassword, newPassword);

        // Then: invalidateAllUserTokens should be called with correct parameters
        verify(mockTokenBlacklistService).invalidateAllUserTokens(userId, "密码修改");

        // And: after password change, any existing token should be considered invalid
        // This is verified by checking that invalidateAllUserTokens was called
        // The actual blacklist check would happen in the TokenBlacklistService
    }

    /**
     * Property 22: 密码修改令牌失效 - 密码修改失败时不应失效令牌
     * For any failed password change (wrong old password), tokens should NOT be invalidated.
     */
    @Property(tries = 10)
    void failedPasswordChangeShouldNotInvalidateTokens(
            @ForAll @LongRange(min = 1, max = 10000) Long userId,
            @ForAll @StringLength(min = 6, max = 20) String actualOldPassword,
            @ForAll @StringLength(min = 6, max = 20) String wrongOldPassword,
            @ForAll @StringLength(min = 6, max = 20) String newPassword,
            @ForAll @StringLength(min = 10, max = 50) String phone
    ) {
        // Skip if wrong password happens to match actual password
        Assume.that(!actualOldPassword.equals(wrongOldPassword));

        // Given: mock dependencies
        UserMapper mockMapper = Mockito.mock(UserMapper.class);
        JwtUtil mockJwtUtil = Mockito.mock(JwtUtil.class);
        SecurityService mockSecurityService = Mockito.mock(SecurityService.class);
        TokenBlacklistService mockTokenBlacklistService = Mockito.mock(TokenBlacklistService.class);

        UserService userService = new UserService();
        setField(userService, "userMapper", mockMapper);
        setField(userService, "passwordEncoder", passwordEncoder);
        setField(userService, "jwtUtil", mockJwtUtil);
        setField(userService, "securityService", mockSecurityService);
        setField(userService, "tokenBlacklistService", mockTokenBlacklistService);

        // Setup: user exists with actual old password
        String encodedActualPassword = passwordEncoder.encode(actualOldPassword);
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setPhone(phone);
        existingUser.setPassword(encodedActualPassword);

        when(mockMapper.findById(userId)).thenReturn(existingUser);

        // When: attempting to change password with wrong old password
        try {
            userService.changePassword(userId, wrongOldPassword, newPassword);
            fail("Expected BusinessException to be thrown");
        } catch (Exception e) {
            // Expected exception for wrong password
        }

        // Then: password should NOT be updated
        verify(mockMapper, never()).updatePassword(anyLong(), anyString());

        // And: tokens should NOT be invalidated
        verify(mockTokenBlacklistService, never()).invalidateAllUserTokens(anyLong(), anyString());
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