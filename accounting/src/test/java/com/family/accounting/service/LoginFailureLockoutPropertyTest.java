package com.family.accounting.service;

import com.family.accounting.exception.BusinessException;
import com.family.accounting.mapper.IpRequestMapper;
import com.family.accounting.mapper.LoginAttemptMapper;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import org.mockito.Mockito;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 登录失败锁定属性测试
 * Feature: family-accounting, Property 19: 登录失败锁定
 * **Validates: Requirements 10.1**
 */
class LoginFailureLockoutPropertyTest {

    /**
     * Property 19: 登录失败锁定
     * For any user account, 5 failed login attempts within 15 minutes should temporarily lock the account for 30 minutes.
     * **Validates: Requirements 10.1**
     */
    @Property(tries = 10)
    void loginFailureLockout(@ForAll("validPhoneNumbers") String phone,
                             @ForAll("validIpAddresses") String ipAddress) {
        // Given: mock mappers and SecurityService
        LoginAttemptMapper mockLoginAttemptMapper = Mockito.mock(LoginAttemptMapper.class);
        IpRequestMapper mockIpRequestMapper = Mockito.mock(IpRequestMapper.class);
        SecurityService securityService = createSecurityService(mockLoginAttemptMapper, mockIpRequestMapper);

        // Setup: simulate 5 failed attempts already recorded
        when(mockLoginAttemptMapper.countFailedAttempts(eq(phone), any(LocalDateTime.class)))
                .thenReturn(5);
        when(mockLoginAttemptMapper.getLastFailedAttemptTime(phone))
                .thenReturn(LocalDateTime.now().minusMinutes(1)); // Last failure was 1 minute ago
        when(mockIpRequestMapper.countRequestsByIp(eq(ipAddress), any(LocalDateTime.class)))
                .thenReturn(0);

        // When: checking if account is locked
        boolean isLocked = securityService.isAccountLocked(phone);

        // Then: account should be locked
        assertThat(isLocked).isTrue();

        // And: validateLoginSecurity should throw exception
        assertThatThrownBy(() -> securityService.validateLoginSecurity(phone, ipAddress))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("账户已被锁定");
    }

    /**
     * Property: 少于5次失败不应该锁定账户
     */
    @Property(tries = 10)
    void lessThanFiveFailuresShouldNotLockAccount(@ForAll("validPhoneNumbers") String phone,
                                                  @ForAll("validIpAddresses") String ipAddress,
                                                  @ForAll @IntRange(min = 0, max = 4) int failureCount) {
        // Given: mock mappers and SecurityService
        LoginAttemptMapper mockLoginAttemptMapper = Mockito.mock(LoginAttemptMapper.class);
        IpRequestMapper mockIpRequestMapper = Mockito.mock(IpRequestMapper.class);
        SecurityService securityService = createSecurityService(mockLoginAttemptMapper, mockIpRequestMapper);

        // Setup: simulate less than 5 failed attempts
        when(mockLoginAttemptMapper.countFailedAttempts(eq(phone), any(LocalDateTime.class)))
                .thenReturn(failureCount);
        when(mockIpRequestMapper.countRequestsByIp(eq(ipAddress), any(LocalDateTime.class)))
                .thenReturn(0);

        // When: checking if account is locked
        boolean isLocked = securityService.isAccountLocked(phone);

        // Then: account should NOT be locked
        assertThat(isLocked).isFalse();
    }

    /**
     * Property: 锁定期过后账户应该解锁
     */
    @Property(tries = 10)
    void accountShouldUnlockAfterLockoutPeriod(@ForAll("validPhoneNumbers") String phone,
                                               @ForAll("validIpAddresses") String ipAddress) {
        // Given: mock mappers and SecurityService
        LoginAttemptMapper mockLoginAttemptMapper = Mockito.mock(LoginAttemptMapper.class);
        IpRequestMapper mockIpRequestMapper = Mockito.mock(IpRequestMapper.class);
        SecurityService securityService = createSecurityService(mockLoginAttemptMapper, mockIpRequestMapper);

        // Setup: simulate 5 failed attempts, but last failure was 31 minutes ago (past lockout period)
        when(mockLoginAttemptMapper.countFailedAttempts(eq(phone), any(LocalDateTime.class)))
                .thenReturn(5);
        when(mockLoginAttemptMapper.getLastFailedAttemptTime(phone))
                .thenReturn(LocalDateTime.now().minusMinutes(31)); // Last failure was 31 minutes ago
        when(mockIpRequestMapper.countRequestsByIp(eq(ipAddress), any(LocalDateTime.class)))
                .thenReturn(0);

        // When: checking if account is locked
        boolean isLocked = securityService.isAccountLocked(phone);

        // Then: account should NOT be locked (lockout period expired)
        assertThat(isLocked).isFalse();
    }

    /**
     * Property: 记录登录尝试应该调用mapper
     */
    @Property(tries = 10)
    void recordLoginAttemptShouldPersist(@ForAll("validPhoneNumbers") String phone,
                                         @ForAll("validIpAddresses") String ipAddress,
                                         @ForAll boolean success) {
        // Given: mock mappers and SecurityService
        LoginAttemptMapper mockLoginAttemptMapper = Mockito.mock(LoginAttemptMapper.class);
        IpRequestMapper mockIpRequestMapper = Mockito.mock(IpRequestMapper.class);
        SecurityService securityService = createSecurityService(mockLoginAttemptMapper, mockIpRequestMapper);

        // When: recording a login attempt
        securityService.recordLoginAttempt(phone, ipAddress, success);

        // Then: mapper insert should be called with correct data
        verify(mockLoginAttemptMapper).insert(argThat(attempt ->
                attempt.getPhone().equals(phone) &&
                attempt.getIpAddress().equals(ipAddress) &&
                attempt.getSuccess() == success
        ));
    }

    /**
     * Property: 恰好5次失败应该锁定账户
     */
    @Property(tries = 10)
    void exactlyFiveFailuresShouldLockAccount(@ForAll("validPhoneNumbers") String phone) {
        // Given: mock mappers and SecurityService
        LoginAttemptMapper mockLoginAttemptMapper = Mockito.mock(LoginAttemptMapper.class);
        IpRequestMapper mockIpRequestMapper = Mockito.mock(IpRequestMapper.class);
        SecurityService securityService = createSecurityService(mockLoginAttemptMapper, mockIpRequestMapper);

        // Setup: simulate exactly 5 failed attempts within window
        when(mockLoginAttemptMapper.countFailedAttempts(eq(phone), any(LocalDateTime.class)))
                .thenReturn(5);
        when(mockLoginAttemptMapper.getLastFailedAttemptTime(phone))
                .thenReturn(LocalDateTime.now().minusMinutes(5)); // Last failure was 5 minutes ago

        // When: checking if account is locked
        boolean isLocked = securityService.isAccountLocked(phone);

        // Then: account should be locked
        assertThat(isLocked).isTrue();
    }

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

    @Provide
    Arbitrary<String> validIpAddresses() {
        return Arbitraries.of(
                "192.168.1.1",
                "10.0.0.1",
                "172.16.0.1",
                "127.0.0.1",
                "203.208.60.1",
                "8.8.8.8",
                "1.1.1.1"
        );
    }

    /**
     * 创建SecurityService实例并注入mock依赖
     */
    private SecurityService createSecurityService(LoginAttemptMapper loginAttemptMapper,
                                                   IpRequestMapper ipRequestMapper) {
        SecurityService service = new SecurityService();
        setField(service, "loginAttemptMapper", loginAttemptMapper);
        setField(service, "ipRequestMapper", ipRequestMapper);
        return service;
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
