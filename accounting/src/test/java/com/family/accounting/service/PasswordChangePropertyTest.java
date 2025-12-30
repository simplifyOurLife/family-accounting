package com.family.accounting.service;

import com.family.accounting.entity.User;
import com.family.accounting.exception.BusinessException;
import com.family.accounting.mapper.UserMapper;
import com.family.accounting.util.JwtUtil;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 密码修改属性测试
 * <p>
 * Feature: family-accounting, Property 14: 密码修改往返验证
 * Validates: Requirements 7.1
 */
class PasswordChangePropertyTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Property 14: 密码修改往返验证
     * For any successful password change, the old password should no longer work for login,
     * and the new password should work.
     */
    @Property(tries = 100)
    void passwordChangeShouldInvalidateOldAndValidateNew(
            @ForAll @LongRange(min = 1, max = 10000) Long userId,
            @ForAll @StringLength(min = 6, max = 20) String oldPassword,
            @ForAll @StringLength(min = 6, max = 20) String newPassword
    ) {
        // Skip if old and new passwords are the same
        Assume.that(!oldPassword.equals(newPassword));

        // Given: a mock UserMapper and UserService
        UserMapper mockMapper = Mockito.mock(UserMapper.class);
        JwtUtil mockJwtUtil = Mockito.mock(JwtUtil.class);

        UserService userService = new UserService();
        setField(userService, "userMapper", mockMapper);
        setField(userService, "passwordEncoder", passwordEncoder);
        setField(userService, "jwtUtil", mockJwtUtil);

        // Setup: user exists with old password
        String encodedOldPassword = passwordEncoder.encode(oldPassword);
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setPhone("13800138000");
        existingUser.setPassword(encodedOldPassword);

        when(mockMapper.findById(userId)).thenReturn(existingUser);

        // Capture the new password when updatePassword is called
        final String[] capturedNewPassword = new String[1];
        doAnswer(invocation -> {
            capturedNewPassword[0] = invocation.getArgument(1);
            // Update the mock user's password for subsequent verifications
            existingUser.setPassword(capturedNewPassword[0]);
            return 1;
        }).when(mockMapper).updatePassword(eq(userId), anyString());

        // When: changing password with correct old password
        userService.changePassword(userId, oldPassword, newPassword);

        // Then: updatePassword should have been called
        verify(mockMapper).updatePassword(eq(userId), anyString());

        // And: the new password should be stored encrypted
        assertNotNull(capturedNewPassword[0]);
        assertNotEquals(newPassword, capturedNewPassword[0]); // Not plain text

        // And: the new password should verify correctly
        assertTrue(passwordEncoder.matches(newPassword, capturedNewPassword[0]));

        // And: the old password should NOT verify against the new stored password
        assertFalse(passwordEncoder.matches(oldPassword, capturedNewPassword[0]));
    }

    /**
     * Property 14: 密码修改往返验证 - 旧密码错误时拒绝修改
     * For any password change attempt with incorrect old password, the change should be rejected.
     */
    @Property(tries = 100)
    void passwordChangeShouldRejectIncorrectOldPassword(
            @ForAll @LongRange(min = 1, max = 10000) Long userId,
            @ForAll @StringLength(min = 6, max = 20) String actualOldPassword,
            @ForAll @StringLength(min = 6, max = 20) String wrongOldPassword,
            @ForAll @StringLength(min = 6, max = 20) String newPassword
    ) {
        // Skip if wrong password happens to match actual password
        Assume.that(!actualOldPassword.equals(wrongOldPassword));

        // Given: a mock UserMapper and UserService
        UserMapper mockMapper = Mockito.mock(UserMapper.class);
        JwtUtil mockJwtUtil = Mockito.mock(JwtUtil.class);

        UserService userService = new UserService();
        setField(userService, "userMapper", mockMapper);
        setField(userService, "passwordEncoder", passwordEncoder);
        setField(userService, "jwtUtil", mockJwtUtil);

        // Setup: user exists with actual old password
        String encodedActualPassword = passwordEncoder.encode(actualOldPassword);
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setPhone("13800138000");
        existingUser.setPassword(encodedActualPassword);

        when(mockMapper.findById(userId)).thenReturn(existingUser);

        // When: attempting to change password with wrong old password
        // Then: should throw BusinessException
        assertThrows(BusinessException.class, () ->
                userService.changePassword(userId, wrongOldPassword, newPassword)
        );

        // And: updatePassword should never be called
        verify(mockMapper, never()).updatePassword(anyLong(), anyString());
    }

    /**
     * Property 14: 密码修改往返验证 - 验证新密码可用于登录
     * After password change, verifyPassword should return true for new password
     * and false for old password.
     */
    @Property(tries = 100)
    void afterPasswordChangeVerifyShouldWorkWithNewPassword(
            @ForAll @LongRange(min = 1, max = 10000) Long userId,
            @ForAll @StringLength(min = 6, max = 20) String oldPassword,
            @ForAll @StringLength(min = 6, max = 20) String newPassword
    ) {
        // Skip if old and new passwords are the same
        Assume.that(!oldPassword.equals(newPassword));

        // Given: a mock UserMapper and UserService
        UserMapper mockMapper = Mockito.mock(UserMapper.class);
        JwtUtil mockJwtUtil = Mockito.mock(JwtUtil.class);

        UserService userService = new UserService();
        setField(userService, "userMapper", mockMapper);
        setField(userService, "passwordEncoder", passwordEncoder);
        setField(userService, "jwtUtil", mockJwtUtil);

        // Setup: user exists with old password
        String encodedOldPassword = passwordEncoder.encode(oldPassword);
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setPhone("13800138000");
        existingUser.setPassword(encodedOldPassword);

        when(mockMapper.findById(userId)).thenReturn(existingUser);

        // Capture and apply the new password
        doAnswer(invocation -> {
            String newEncodedPassword = invocation.getArgument(1);
            existingUser.setPassword(newEncodedPassword);
            return 1;
        }).when(mockMapper).updatePassword(eq(userId), anyString());

        // When: changing password
        userService.changePassword(userId, oldPassword, newPassword);

        // Then: verifyPassword should return true for new password
        assertTrue(userService.verifyPassword(userId, newPassword));

        // And: verifyPassword should return false for old password
        assertFalse(userService.verifyPassword(userId, oldPassword));
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
