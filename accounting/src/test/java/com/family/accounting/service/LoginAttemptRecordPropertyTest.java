package com.family.accounting.service;

import com.family.accounting.entity.LoginAttempt;
import com.family.accounting.mapper.IpRequestMapper;
import com.family.accounting.mapper.LoginAttemptMapper;
import net.jqwik.api.*;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

/**
 * 登录尝试记录属性测试
 * Feature: family-accounting, Property 20: 登录尝试记录
 * **Validates: Requirements 10.3**
 */
class LoginAttemptRecordPropertyTest {

    /**
     * Property 20: 登录尝试记录
     * For any login attempt (successful or failed), the system should log the attempt with timestamp and IP address.
     * **Validates: Requirements 10.3**
     */
    @Property(tries = 10)
    void loginAttemptsShouldBeRecorded(@ForAll("validPhoneNumbers") String phone,
                                       @ForAll("validIpAddresses") String ipAddress,
                                       @ForAll boolean success) {
        // Given: mock mappers and SecurityService
        LoginAttemptMapper mockLoginAttemptMapper = Mockito.mock(LoginAttemptMapper.class);
        IpRequestMapper mockIpRequestMapper = Mockito.mock(IpRequestMapper.class);
        SecurityService securityService = createSecurityService(mockLoginAttemptMapper, mockIpRequestMapper);

        // When: recording a login attempt
        securityService.recordLoginAttempt(phone, ipAddress, success);

        // Then: mapper insert should be called with correct data including timestamp
        verify(mockLoginAttemptMapper).insert(argThat(attempt ->
                attempt.getPhone().equals(phone) &&
                attempt.getIpAddress().equals(ipAddress) &&
                attempt.getSuccess() == success &&
                attempt.getCreatedAt() != null
        ));
    }

    /**
     * Property: 成功和失败的登录尝试都应该被记录
     */
    @Property(tries = 10)
    void bothSuccessfulAndFailedAttemptsShouldBeRecorded(@ForAll("validPhoneNumbers") String phone,
                                                         @ForAll("validIpAddresses") String ipAddress) {
        // Given: mock mappers and SecurityService
        LoginAttemptMapper mockLoginAttemptMapper = Mockito.mock(LoginAttemptMapper.class);
        IpRequestMapper mockIpRequestMapper = Mockito.mock(IpRequestMapper.class);
        SecurityService securityService = createSecurityService(mockLoginAttemptMapper, mockIpRequestMapper);

        // When: recording both successful and failed login attempts
        securityService.recordLoginAttempt(phone, ipAddress, true);
        securityService.recordLoginAttempt(phone, ipAddress, false);

        // Then: mapper insert should be called twice
        verify(mockLoginAttemptMapper, times(2)).insert(any(LoginAttempt.class));

        // And: verify successful attempt was recorded
        verify(mockLoginAttemptMapper).insert(argThat(attempt ->
                attempt.getPhone().equals(phone) &&
                attempt.getIpAddress().equals(ipAddress) &&
                attempt.getSuccess() == true
        ));

        // And: verify failed attempt was recorded
        verify(mockLoginAttemptMapper).insert(argThat(attempt ->
                attempt.getPhone().equals(phone) &&
                attempt.getIpAddress().equals(ipAddress) &&
                attempt.getSuccess() == false
        ));
    }

    /**
     * Property: 不同手机号的登录尝试应该独立记录
     */
    @Property(tries = 10)
    void differentPhoneNumbersShouldBeRecordedSeparately(@ForAll("validPhoneNumbers") String phone1,
                                                         @ForAll("validPhoneNumbers") String phone2,
                                                         @ForAll("validIpAddresses") String ipAddress) {
        // Skip if same phone number
        Assume.that(!phone1.equals(phone2));

        // Given: mock mappers and SecurityService
        LoginAttemptMapper mockLoginAttemptMapper = Mockito.mock(LoginAttemptMapper.class);
        IpRequestMapper mockIpRequestMapper = Mockito.mock(IpRequestMapper.class);
        SecurityService securityService = createSecurityService(mockLoginAttemptMapper, mockIpRequestMapper);

        // When: recording login attempts for two different phone numbers
        securityService.recordLoginAttempt(phone1, ipAddress, true);
        securityService.recordLoginAttempt(phone2, ipAddress, false);

        // Then: mapper insert should be called twice with different phone numbers
        verify(mockLoginAttemptMapper, times(2)).insert(any(LoginAttempt.class));

        // And: verify first phone was recorded
        verify(mockLoginAttemptMapper).insert(argThat(attempt ->
                attempt.getPhone().equals(phone1)
        ));

        // And: verify second phone was recorded
        verify(mockLoginAttemptMapper).insert(argThat(attempt ->
                attempt.getPhone().equals(phone2)
        ));
    }

    /**
     * Property: 不同IP地址的登录尝试应该独立记录
     */
    @Property(tries = 10)
    void differentIpAddressesShouldBeRecordedSeparately(@ForAll("validPhoneNumbers") String phone,
                                                        @ForAll("validIpAddresses") String ip1,
                                                        @ForAll("validIpAddresses") String ip2) {
        // Skip if same IP address
        Assume.that(!ip1.equals(ip2));

        // Given: mock mappers and SecurityService
        LoginAttemptMapper mockLoginAttemptMapper = Mockito.mock(LoginAttemptMapper.class);
        IpRequestMapper mockIpRequestMapper = Mockito.mock(IpRequestMapper.class);
        SecurityService securityService = createSecurityService(mockLoginAttemptMapper, mockIpRequestMapper);

        // When: recording login attempts from two different IP addresses
        securityService.recordLoginAttempt(phone, ip1, true);
        securityService.recordLoginAttempt(phone, ip2, false);

        // Then: mapper insert should be called twice with different IP addresses
        verify(mockLoginAttemptMapper, times(2)).insert(any(LoginAttempt.class));

        // And: verify first IP was recorded
        verify(mockLoginAttemptMapper).insert(argThat(attempt ->
                attempt.getIpAddress().equals(ip1)
        ));

        // And: verify second IP was recorded
        verify(mockLoginAttemptMapper).insert(argThat(attempt ->
                attempt.getIpAddress().equals(ip2)
        ));
    }

    /**
     * Property: 登录尝试记录应该包含时间戳
     */
    @Property(tries = 10)
    void loginAttemptShouldIncludeTimestamp(@ForAll("validPhoneNumbers") String phone,
                                            @ForAll("validIpAddresses") String ipAddress,
                                            @ForAll boolean success) {
        // Given: mock mappers and SecurityService
        LoginAttemptMapper mockLoginAttemptMapper = Mockito.mock(LoginAttemptMapper.class);
        IpRequestMapper mockIpRequestMapper = Mockito.mock(IpRequestMapper.class);
        SecurityService securityService = createSecurityService(mockLoginAttemptMapper, mockIpRequestMapper);

        // When: recording a login attempt
        securityService.recordLoginAttempt(phone, ipAddress, success);

        // Then: the recorded attempt should have a non-null timestamp
        verify(mockLoginAttemptMapper).insert(argThat(attempt ->
                attempt.getCreatedAt() != null
        ));
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
