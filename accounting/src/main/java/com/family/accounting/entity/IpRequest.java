package com.family.accounting.entity;

import java.time.LocalDateTime;

/**
 * IP请求记录实体
 */
public class IpRequest {
    
    private Long id;
    private String ipAddress;
    private String requestPath;
    private LocalDateTime createdAt;
    
    public IpRequest() {}
    
    public IpRequest(String ipAddress, String requestPath) {
        this.ipAddress = ipAddress;
        this.requestPath = requestPath;
        this.createdAt = LocalDateTime.now();
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getRequestPath() {
        return requestPath;
    }
    
    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}