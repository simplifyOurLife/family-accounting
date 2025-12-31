package com.family.accounting.service;

import com.family.accounting.dto.LoginDTO;
import com.family.accounting.dto.TokenVO;
import com.family.accounting.entity.User;
import com.family.accounting.exception.BusinessException;
import com.family.accounting.mapper.UserMapper;
import com.family.accounting.util.JwtUtil;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 登录认证属性测试
 * <p>
 * Feature: family-accounting, Property 2: 登录认证往返一致性
 * Validates: Requirements 1.3
 */
class LoginPropertyTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Property 2: 登录认证往返一致性
     * For any registered user with known credentials, logging in with correct phone and password
     * should return a valid token, and using that token should identify the same user.
     */
    @Property(tries = 10)
    void loginShouldReturnTokenThatIdentifiesSameUser(
            @ForAll("validPhoneNumbers") String phone,
            @ForAll @StringLength(min = 6, max = 20) String password,
            @ForAll @LongRange(min = 1, max = 1000000) Long userId
    ) {
        // Given: a mock UserMapper and real JwtUtil
        UserMapper mockMapper = mock(UserMapper.class);
        JwtUtil jwtUtil = createJwtUtil();
        SecurityService mockSecurityService = mock(SecurityService.class);
        TokenBlacklistService mockTokenBlacklistService = mock(TokenBlacklistService.class);
        CaptchaService mockCaptchaService = mock(CaptchaService.class);

        UserService userService = new UserService();
        setField(userService, "userMapper", mockMapper);
        setField(userService, "passwordEncoder", passwordEncoder);
        setField(userService, "jwtUtil", jwtUtil);
        setField(userService, "securityService", mockSecurityService);
        setField(userService, "tokenBlacklistService", mockTokenBlacklistService);
        setField(userService, "captchaService", mockCaptchaService);

        // Setup: user exists with encrypted password
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setPhone(phone);
        existingUser.setPassword(passwordEncoder.encode(password));
        existingUser.setNickname("TestUser");

        when(mockMapper.findByPhone(phone)).thenReturn(existingUser);
        when(mockCaptchaService.verifyCaptcha(anyString(), anyString())).thenReturn(true);

        // When: logging in with correct credentials
        LoginDTO dto = new LoginDTO();
        dto.setPhone(phone);
        dto.setPassword(password);

        TokenVO tokenVO = userService.login(dto, "127.0.0.1");

        // Then: should return a valid token
        assertNotNull(tokenVO);
        assertNotNull(tokenVO.getToken());
        assertEquals(userId, tokenVO.getUserId());
        assertEquals(phone, tokenVO.getPhone());

        // And: the token should identify the same user
        Long tokenUserId = jwtUtil.getUserIdFromToken(tokenVO.getToken());
        String tokenPhone = jwtUtil.getPhoneFromToken(tokenVO.getToken());

        assertEquals(userId, tokenUserId);
        assertEquals(phone, tokenPhone);

        // And: the token should be valid
        assertTrue(jwtUtil.validateToken(tokenVO.getToken()));
    }

    /**
     * Property 2: 登录认证往返一致性 - 错误密码拒绝
     * For any registered user, logging in with incorrect password should be rejected.
     */
    @Property(tries = 10)
    void loginShouldRejectIncorrectPassword(
            @ForAll("validPhoneNumbers") String phone,
            @ForAll @StringLength(min = 6, max = 20) String correctPassword,
            @ForAll @StringLength(min = 6, max = 20) String wrongPassword
    ) {
        // Skip if passwords happen to be the same
        Assume.that(!correctPassword.equals(wrongPassword));

        // Given: a mock UserMapper and UserService
        UserMapper mockMapper = mock(UserMapper.class);
        JwtUtil jwtUtil = createJwtUtil();
        SecurityService mockSecurityService = mock(SecurityService.class);
        TokenBlacklistService mockTokenBlacklistService = mock(TokenBlacklistService.class);
        CaptchaService mockCaptchaService = mock(CaptchaService.class);

        UserService userService = new UserService();
        setField(userService, "userMapper", mockMapper);
        setField(userService, "passwordEncoder", passwordEncoder);
        setField(userService, "jwtUtil", jwtUtil);
        setField(userService, "securityService", mockSecurityService);
        setField(userService, "tokenBlacklistService", mockTokenBlacklistService);
        setField(userService, "captchaService", mockCaptchaService);

        // Setup: user exists with encrypted correct password
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setPhone(phone);
        existingUser.setPassword(passwordEncoder.encode(correctPassword));

        when(mockMapper.findByPhone(phone)).thenReturn(existingUser);
        when(mockCaptchaService.verifyCaptcha(anyString(), anyString())).thenReturn(true);

        // When: logging in with wrong password
        LoginDTO dto = new LoginDTO();
        dto.setPhone(phone);
        dto.setPassword(wrongPassword);

        // Then: should throw BusinessException
        assertThrows(BusinessException.class, () -> userService.login(dto, "127.0.0.1"));
    }

    /**
     * Property 2: 登录认证往返一致性 - 不存在用户拒绝
     * For any non-existent phone number, login should be rejected.
     */
    @Property(tries = 10)
    void loginShouldRejectNonExistentUser(
            @ForAll("validPhoneNumbers") String phone,
            @ForAll @StringLength(min = 6, max = 20) String password
    ) {
        // Given: a mock UserMapper and UserService
        UserMapper mockMapper = mock(UserMapper.class);
        JwtUtil jwtUtil = createJwtUtil();
        SecurityService mockSecurityService = mock(SecurityService.class);
        TokenBlacklistService mockTokenBlacklistService = mock(TokenBlacklistService.class);
        CaptchaService mockCaptchaService = mock(CaptchaService.class);

        UserService userService = new UserService();
        setField(userService, "userMapper", mockMapper);
        setField(userService, "passwordEncoder", passwordEncoder);
        setField(userService, "jwtUtil", jwtUtil);
        setField(userService, "securityService", mockSecurityService);
        setField(userService, "tokenBlacklistService", mockTokenBlacklistService);
        setField(userService, "captchaService", mockCaptchaService);

        // Setup: user does not exist
        when(mockMapper.findByPhone(phone)).thenReturn(null);
        when(mockCaptchaService.verifyCaptcha(anyString(), anyString())).thenReturn(true);

        // When: logging in with non-existent phone
        LoginDTO dto = new LoginDTO();
        dto.setPhone(phone);
        dto.setPassword(password);

        // Then: should throw BusinessException
        assertThrows(BusinessException.class, () -> userService.login(dto, "127.0.0.1"));
    }

    /**
     * 生成有效的中国手机号
     */
    @Provide
    Arbitrary<String> validPhoneNumbers() {
        return Arbitraries.integers()
                .between(130, 199)
                .flatMap(prefix ->
                        Arbitraries.integers()
                                .between(10000000, 99999999)
                                .map(suffix -> String.valueOf(prefix) + String.valueOf(suffix))
                );
    }

    /**
     * 创建JwtUtil实例用于测试
     */
    private JwtUtil createJwtUtil() {
        JwtUtil jwtUtil = new JwtUtil();
        setField(jwtUtil, "secret", "test-secret-key-for-property-testing-2024");
        setField(jwtUtil, "expiration", 86400000L);
        return jwtUtil;
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