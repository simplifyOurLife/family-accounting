package com.family.accounting.entity;

import java.time.LocalDateTime;

/**
 * 登录尝试记录实体
 */
public class LoginAttempt {
    
    private Long id;
    private String phone;
    private String ipAddress;
    private Boolean success;
    private LocalDateTime createdAt;
    
    public LoginAttempt() {}
    
    public LoginAttempt(String phone, String ipAddress, Boolean success) {
        this.phone = phone;
        this.ipAddress = ipAddress;
        this.success = success;
        this.createdAt = LocalDateTime.now();
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public Boolean getSuccess() {
        return success;
    }
    
    public void setSuccess(Boolean success) {
        this.success = success;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}