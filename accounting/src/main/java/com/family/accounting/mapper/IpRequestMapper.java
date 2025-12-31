package com.family.accounting.mapper;

import com.family.accounting.entity.IpRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * IP请求记录 Mapper
 */
@Mapper
public interface IpRequestMapper {
    
    /**
     * 插入IP请求记录
     */
    void insert(IpRequest ipRequest);
    
    /**
     * 根据IP地址和时间范围查询请求次数
     */
    int countRequestsByIp(@Param("ipAddress") String ipAddress, 
                         @Param("startTime") LocalDateTime startTime);
}