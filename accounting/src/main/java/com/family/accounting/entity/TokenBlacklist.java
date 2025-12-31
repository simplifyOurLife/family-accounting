package com.family.accounting.entity;

import java.time.LocalDateTime;

/**
 * JWT令牌黑名单实体
 * 用于存储已失效的JWT令牌
 */
public class TokenBlacklist {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * JWT令牌（完整token或token的hash值）
     */
    private String tokenHash;
    
    /**
     * 令牌失效原因
     */
    private String reason;
    
    /**
     * 令牌原始过期时间
     */
    private LocalDateTime tokenExpiredAt;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    // 构造函数
    public TokenBlacklist() {}

    public TokenBlacklist(Long userId, String tokenHash, String reason, LocalDateTime tokenExpiredAt) {
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.reason = reason;
        this.tokenExpiredAt = tokenExpiredAt;
        this.createdAt = LocalDateTime.now();
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getTokenExpiredAt() {
        return tokenExpiredAt;
    }

    public void setTokenExpiredAt(LocalDateTime tokenExpiredAt) {
        this.tokenExpiredAt = tokenExpiredAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "TokenBlacklist{" +
                "id=" + id +
                ", userId=" + userId +
                ", tokenHash='" + tokenHash + '\'' +
                ", reason='" + reason + '\'' +
                ", tokenExpiredAt=" + tokenExpiredAt +
                ", createdAt=" + createdAt +
                '}';
    }
}