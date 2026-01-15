package com.family.accounting.service;

import com.family.accounting.dto.RegisterDTO;
import com.family.accounting.dto.UserVO;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 用户服务属性测试
 * <p>
 * Feature: family-accounting, Property 1: 用户注册创建账户
 * Validates: Requirements 1.1
 */
class UserServicePropertyTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Property 1: 用户注册创建账户
     * For any valid phone number and password combination, registering a new user
     * should create a user account that can be retrieved with the same phone number.
     */
    @Property(tries = 100)
    void registerShouldCreateUserWithSamePhone(
            @ForAll("validPhoneNumbers") String phone,
            @ForAll @StringLength(min = 6, max = 20) String password,
            @ForAll @StringLength(max = 50) String nickname
    ) {
        // Given: a mock UserMapper and UserService
        UserMapper mockMapper = Mockito.mock(UserMapper.class);
        JwtUtil mockJwtUtil = Mockito.mock(JwtUtil.class);
        CaptchaService mockCaptchaService = Mockito.mock(CaptchaService.class);
        SecurityService mockSecurityService = Mockito.mock(SecurityService.class);
        TokenBlacklistService mockTokenBlacklistService = Mockito.mock(TokenBlacklistService.class);

        UserService userService = new UserService();
        setField(userService, "userMapper", mockMapper);
        setField(userService, "passwordEncoder", passwordEncoder);
        setField(userService, "jwtUtil", mockJwtUtil);
        setField(userService, "captchaService", mockCaptchaService);
        setField(userService, "securityService", mockSecurityService);
        setField(userService, "tokenBlacklistService", mockTokenBlacklistService);

        // Setup: captcha verification returns true (bypass captcha check)
        when(mockCaptchaService.verifyCaptcha(any(), any())).thenReturn(true);

        // Setup: phone does not exist
        when(mockMapper.existsByPhone(phone)).thenReturn(0);

        // Capture the inserted user
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L); // Simulate auto-generated ID
            return 1;
        }).when(mockMapper).insert(any(User.class));

        // When: registering a new user
        RegisterDTO dto = new RegisterDTO();
        dto.setPhone(phone);
        dto.setPassword(password);
        dto.setNickname(nickname);

        UserVO result = userService.register(dto);

        // Then: the returned user should have the same phone number
        assertNotNull(result);
        assertEquals(phone, result.getPhone());
        assertEquals(nickname, result.getNickname());
        assertNotNull(result.getId());

        // Verify insert was called with correct phone
        verify(mockMapper).insert(argThat(user ->
                user.getPhone().equals(phone) &&
                        passwordEncoder.matches(password, user.getPassword())
        ));
    }

    /**
     * Property 1: 用户注册创建账户 - 密码加密存储
     * For any valid registration, the password should be stored encrypted (not plain text).
     */
    @Property(tries = 100)
    void registerShouldEncryptPassword(
            @ForAll("validPhoneNumbers") String phone,
            @ForAll @StringLength(min = 6, max = 20) String password
    ) {
        // Given: a mock UserMapper and UserService
        UserMapper mockMapper = Mockito.mock(UserMapper.class);
        JwtUtil mockJwtUtil = Mockito.mock(JwtUtil.class);
        CaptchaService mockCaptchaService = Mockito.mock(CaptchaService.class);
        SecurityService mockSecurityService = Mockito.mock(SecurityService.class);
        TokenBlacklistService mockTokenBlacklistService = Mockito.mock(TokenBlacklistService.class);

        UserService userService = new UserService();
        setField(userService, "userMapper", mockMapper);
        setField(userService, "passwordEncoder", passwordEncoder);
        setField(userService, "jwtUtil", mockJwtUtil);
        setField(userService, "captchaService", mockCaptchaService);
        setField(userService, "securityService", mockSecurityService);
        setField(userService, "tokenBlacklistService", mockTokenBlacklistService);

        // Setup: captcha verification returns true (bypass captcha check)
        when(mockCaptchaService.verifyCaptcha(any(), any())).thenReturn(true);

        // Setup: phone does not exist
        when(mockMapper.existsByPhone(phone)).thenReturn(0);

        // Capture the inserted user's password
        final String[] storedPassword = new String[1];
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            storedPassword[0] = user.getPassword();
            user.setId(1L);
            return 1;
        }).when(mockMapper).insert(any(User.class));

        // When: registering a new user
        RegisterDTO dto = new RegisterDTO();
        dto.setPhone(phone);
        dto.setPassword(password);

        userService.register(dto);

        // Then: the stored password should be encrypted (not equal to plain text)
        assertNotNull(storedPassword[0]);
        assertNotEquals(password, storedPassword[0]);

        // And: the encrypted password should match the original when verified
        assertTrue(passwordEncoder.matches(password, storedPassword[0]));
    }

    /**
     * Property 1: 用户注册创建账户 - 重复手机号拒绝
     * For any phone number that already exists, registration should be rejected.
     */
    @Property(tries = 100)
    void registerShouldRejectDuplicatePhone(
            @ForAll("validPhoneNumbers") String phone,
            @ForAll @StringLength(min = 6, max = 20) String password
    ) {
        // Given: a mock UserMapper and UserService
        UserMapper mockMapper = Mockito.mock(UserMapper.class);
        JwtUtil mockJwtUtil = Mockito.mock(JwtUtil.class);
        CaptchaService mockCaptchaService = Mockito.mock(CaptchaService.class);
        SecurityService mockSecurityService = Mockito.mock(SecurityService.class);
        TokenBlacklistService mockTokenBlacklistService = Mockito.mock(TokenBlacklistService.class);

        UserService userService = new UserService();
        setField(userService, "userMapper", mockMapper);
        setField(userService, "passwordEncoder", passwordEncoder);
        setField(userService, "jwtUtil", mockJwtUtil);
        setField(userService, "captchaService", mockCaptchaService);
        setField(userService, "securityService", mockSecurityService);
        setField(userService, "tokenBlacklistService", mockTokenBlacklistService);

        // Setup: captcha verification returns true (bypass captcha check)
        when(mockCaptchaService.verifyCaptcha(any(), any())).thenReturn(true);

        // Setup: phone already exists
        when(mockMapper.existsByPhone(phone)).thenReturn(1);

        // When: attempting to register with existing phone
        RegisterDTO dto = new RegisterDTO();
        dto.setPhone(phone);
        dto.setPassword(password);

        // Then: should throw BusinessException
        assertThrows(BusinessException.class, () -> userService.register(dto));

        // And: insert should never be called
        verify(mockMapper, never()).insert(any(User.class));
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
