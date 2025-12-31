package com.family.accounting.service;

import com.family.accounting.mapper.IpRequestMapper;
import com.family.accounting.mapper.LoginAttemptMapper;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import org.mockito.Mockito;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * IP地址速率限制属性测试
 * Feature: family-accounting, Property 20: IP地址速率限制
 * **Validates: Requirements 10.3**
 */
class IpRateLimitPropertyTest {

    /**
     * Property 20: IP地址速率限制
     * For any IP address making more than 100 requests per minute, the system should implement rate limiting.
     * **Validates: Requirements 10.3**
     */
    @Property(tries = 10)
    void ipRateLimitShouldBlockExcessiveRequests(@ForAll("validIpAddresses") String ipAddress) {
        // Given: mock mappers and SecurityService
        LoginAttemptMapper mockLoginAttemptMapper = Mockito.mock(LoginAttemptMapper.class);
        IpRequestMapper mockIpRequestMapper = Mockito.mock(IpRequestMapper.class);
        SecurityService securityService = createSecurityService(mockLoginAttemptMapper, mockIpRequestMapper);

        // Setup: simulate 100 or more requests from this IP in the last minute
        when(mockIpRequestMapper.countRequestsByIp(eq(ipAddress), any(LocalDateTime.class)))
                .thenReturn(100);

        // When: checking if IP is rate limited
        boolean isLimited = securityService.isIpRateLimited(ipAddress);

        // Then: IP should be rate limited
        assertThat(isLimited).isTrue();
    }

    /**
     * Property: 少于100次请求不应该被限制
     */
    @Property(tries = 10)
    void lessThanHundredRequestsShouldNotBeLimited(@ForAll("validIpAddresses") String ipAddress,
                                                   @ForAll @IntRange(min = 0, max = 99) int requestCount) {
        // Given: mock mappers and SecurityService
        LoginAttemptMapper mockLoginAttemptMapper = Mockito.mock(LoginAttemptMapper.class);
        IpRequestMapper mockIpRequestMapper = Mockito.mock(IpRequestMapper.class);
        SecurityService securityService = createSecurityService(mockLoginAttemptMapper, mockIpRequestMapper);

        // Setup: simulate less than 100 requests from this IP
        when(mockIpRequestMapper.countRequestsByIp(eq(ipAddress), any(LocalDateTime.class)))
                .thenReturn(requestCount);

        // When: checking if IP is rate limited
        boolean isLimited = securityService.isIpRateLimited(ipAddress);

        // Then: IP should NOT be rate limited
        assertThat(isLimited).isFalse();
    }

    /**
     * Property: 不同IP地址的请求应该独立计算
     */
    @Property(tries = 10)
    void differentIpsShouldBeTrackedSeparately(@ForAll("validIpAddresses") String ip1,
                                               @ForAll("validIpAddresses") String ip2) {
        // Skip if same IP
        Assume.that(!ip1.equals(ip2));

        // Given: mock mappers and SecurityService
        LoginAttemptMapper mockLoginAttemptMapper = Mockito.mock(LoginAttemptMapper.class);
        IpRequestMapper mockIpRequestMapper = Mockito.mock(IpRequestMapper.class);
        SecurityService securityService = createSecurityService(mockLoginAttemptMapper, mockIpRequestMapper);

        // Setup: IP1 has 100+ requests (should be limited), IP2 has 50 requests (should not be limited)
        when(mockIpRequestMapper.countRequestsByIp(eq(ip1), any(LocalDateTime.class)))
                .thenReturn(101);
        when(mockIpRequestMapper.countRequestsByIp(eq(ip2), any(LocalDateTime.class)))
                .thenReturn(50);

        // When/Then: IP1 should be limited, IP2 should not
        assertThat(securityService.isIpRateLimited(ip1)).isTrue();
        assertThat(securityService.isIpRateLimited(ip2)).isFalse();
    }

    /**
     * Property: 记录IP请求应该调用mapper
     */
    @Property(tries = 10)
    void recordIpRequestShouldPersist(@ForAll("validIpAddresses") String ipAddress,
                                      @ForAll("validRequestPaths") String requestPath) {
        // Given: mock mappers and SecurityService
        LoginAttemptMapper mockLoginAttemptMapper = Mockito.mock(LoginAttemptMapper.class);
        IpRequestMapper mockIpRequestMapper = Mockito.mock(IpRequestMapper.class);
        SecurityService securityService = createSecurityService(mockLoginAttemptMapper, mockIpRequestMapper);

        // When: recording an IP request
        securityService.recordIpRequest(ipAddress, requestPath);

        // Then: mapper insert should be called with correct data
        verify(mockIpRequestMapper).insert(argThat(request ->
                request.getIpAddress().equals(ipAddress) &&
                request.getRequestPath().equals(requestPath)
        ));
    }

    /**
     * Property: 恰好100次请求应该被限制
     */
    @Property(tries = 10)
    void exactlyHundredRequestsShouldBeLimited(@ForAll("validIpAddresses") String ipAddress) {
        // Given: mock mappers and SecurityService
        LoginAttemptMapper mockLoginAttemptMapper = Mockito.mock(LoginAttemptMapper.class);
        IpRequestMapper mockIpRequestMapper = Mockito.mock(IpRequestMapper.class);
        SecurityService securityService = createSecurityService(mockLoginAttemptMapper, mockIpRequestMapper);

        // Setup: simulate exactly 100 requests from this IP
        when(mockIpRequestMapper.countRequestsByIp(eq(ipAddress), any(LocalDateTime.class)))
                .thenReturn(100);

        // When: checking if IP is rate limited
        boolean isLimited = securityService.isIpRateLimited(ipAddress);

        // Then: IP should be rate limited (>= 100 triggers limit)
        assertThat(isLimited).isTrue();
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
                "1.1.1.1",
                "192.168.0.100",
                "10.10.10.10",
                "172.31.255.255"
        );
    }

    @Provide
    Arbitrary<String> validRequestPaths() {
        return Arbitraries.of(
                "/api/auth/login",
                "/api/auth/register",
                "/api/transaction",
                "/api/family",
                "/api/statistics/daily",
                "/api/user/profile"
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
