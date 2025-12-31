package com.family.accounting.mapper;

import com.family.accounting.entity.LoginAttempt;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录尝试记录 Mapper
 */
@Mapper
public interface LoginAttemptMapper {
    
    /**
     * 插入登录尝试记录
     */
    void insert(LoginAttempt loginAttempt);
    
    /**
     * 根据手机号和时间范围查询失败登录次数
     */
    int countFailedAttempts(@Param("phone") String phone, 
                           @Param("startTime") LocalDateTime startTime);
    
    /**
     * 根据IP地址和时间范围查询请求次数
     */
    int countRequestsByIp(@Param("ipAddress") String ipAddress, 
                         @Param("startTime") LocalDateTime startTime);
    
    /**
     * 根据手机号查询登录尝试记录
     */
    List<LoginAttempt> findByPhone(@Param("phone") String phone);
    
    /**
     * 根据IP地址查询登录尝试记录
     */
    List<LoginAttempt> findByIpAddress(@Param("ipAddress") String ipAddress);
    
    /**
     * 获取指定手机号最后一次失败登录的时间
     */
    LocalDateTime getLastFailedAttemptTime(@Param("phone") String phone);
}