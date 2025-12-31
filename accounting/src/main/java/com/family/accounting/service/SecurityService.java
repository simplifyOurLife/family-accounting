package com.family.accounting.service;

import com.family.accounting.entity.IpRequest;
import com.family.accounting.entity.LoginAttempt;
import com.family.accounting.exception.BusinessException;
import com.family.accounting.mapper.IpRequestMapper;
import com.family.accounting.mapper.LoginAttemptMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 安全防护服务类
 */
@Service
public class SecurityService {

    @Autowired
    private LoginAttemptMapper loginAttemptMapper;

    @Autowired
    private IpRequestMapper ipRequestMapper;

    // 登录失败锁定配置
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int FAILED_ATTEMPTS_WINDOW_MINUTES = 15;
    private static final int LOCKOUT_DURATION_MINUTES = 30;

    // IP速率限制配置
    private static final int MAX_REQUESTS_PER_MINUTE = 100;

    /**
     * 记录登录尝试
     *
     * @param phone     手机号
     * @param ipAddress IP地址
     * @param success   是否成功
     */
    @Transactional
    public void recordLoginAttempt(String phone, String ipAddress, boolean success) {
        LoginAttempt attempt = new LoginAttempt(phone, ipAddress, success);
        loginAttemptMapper.insert(attempt);
    }

    /**
     * 检查账户是否被锁定
     *
     * @param phone 手机号
     * @return 是否被锁定
     */
    public boolean isAccountLocked(String phone) {
        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(FAILED_ATTEMPTS_WINDOW_MINUTES);
        int failedAttempts = loginAttemptMapper.countFailedAttempts(phone, windowStart);
        
        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            // 检查是否还在锁定期内
            // 获取最近的失败尝试时间，如果在锁定期内则仍被锁定
            LocalDateTime lockoutEnd = getLastFailedAttemptTime(phone).plusMinutes(LOCKOUT_DURATION_MINUTES);
            return LocalDateTime.now().isBefore(lockoutEnd);
        }
        
        return false;
    }

    /**
     * 获取最后一次失败尝试的时间
     *
     * @param phone 手机号
     * @return 最后一次失败尝试时间
     */
    private LocalDateTime getLastFailedAttemptTime(String phone) {
        LocalDateTime lastFailedTime = loginAttemptMapper.getLastFailedAttemptTime(phone);
        return lastFailedTime != null ? lastFailedTime : LocalDateTime.now().minusHours(1);
    }

    /**
     * 检查IP是否被限制
     *
     * @param ipAddress IP地址
     * @return 是否被限制
     */
    public boolean isIpRateLimited(String ipAddress) {
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
        int requestCount = ipRequestMapper.countRequestsByIp(ipAddress, oneMinuteAgo);
        return requestCount >= MAX_REQUESTS_PER_MINUTE;
    }

    /**
     * 记录IP请求
     *
     * @param ipAddress   IP地址
     * @param requestPath 请求路径
     */
    @Transactional
    public void recordIpRequest(String ipAddress, String requestPath) {
        IpRequest request = new IpRequest(ipAddress, requestPath);
        ipRequestMapper.insert(request);
    }

    /**
     * 验证登录前的安全检查
     *
     * @param phone     手机号
     * @param ipAddress IP地址
     */
    public void validateLoginSecurity(String phone, String ipAddress) {
        // 检查IP速率限制
        if (isIpRateLimited(ipAddress)) {
            throw new BusinessException("请求过于频繁，请稍后再试");
        }

        // 检查账户锁定状态
        if (isAccountLocked(phone)) {
            throw new BusinessException("账户已被锁定，请30分钟后再试");
        }
    }

    /**
     * 获取账户剩余锁定时间（分钟）
     *
     * @param phone 手机号
     * @return 剩余锁定时间，0表示未锁定
     */
    public int getRemainingLockoutMinutes(String phone) {
        if (!isAccountLocked(phone)) {
            return 0;
        }

        // 这里简化处理，返回固定的锁定时间
        // 实际应用中可以根据最后一次失败尝试时间计算精确的剩余时间
        return LOCKOUT_DURATION_MINUTES;
    }
}