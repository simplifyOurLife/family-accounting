package com.family.accounting.service;

import com.family.accounting.entity.TokenBlacklist;
import com.family.accounting.mapper.TokenBlacklistMapper;
import com.family.accounting.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * JWT令牌黑名单服务
 * 管理失效的JWT令牌
 */
@Service
public class TokenBlacklistService {

    private static final Logger logger = LoggerFactory.getLogger(TokenBlacklistService.class);

    @Autowired
    private TokenBlacklistMapper tokenBlacklistMapper;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 将令牌加入黑名单
     *
     * @param token  JWT令牌
     * @param userId 用户ID
     * @param reason 失效原因
     */
    @Transactional
    public void addToBlacklist(String token, Long userId, String reason) {
        try {
            // 获取令牌的过期时间
            Date expiration = jwtUtil.getExpirationFromToken(token);
            if (expiration == null) {
                logger.warn("无法获取令牌过期时间，跳过黑名单添加: userId={}", userId);
                return;
            }

            // 计算令牌的hash值
            String tokenHash = calculateTokenHash(token);

            // 转换过期时间
            LocalDateTime tokenExpiredAt = expiration.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();

            // 创建黑名单记录
            TokenBlacklist blacklist = new TokenBlacklist(userId, tokenHash, reason, tokenExpiredAt);

            // 插入数据库
            tokenBlacklistMapper.insert(blacklist);

            logger.info("令牌已加入黑名单: userId={}, reason={}, expiredAt={}", 
                       userId, reason, tokenExpiredAt);

        } catch (Exception e) {
            logger.error("添加令牌到黑名单失败: userId={}, reason={}", userId, reason, e);
            throw new RuntimeException("添加令牌到黑名单失败", e);
        }
    }

    /**
     * 检查令牌是否在黑名单中
     *
     * @param token JWT令牌
     * @return 是否在黑名单中
     */
    public boolean isTokenBlacklisted(String token) {
        try {
            String tokenHash = calculateTokenHash(token);
            return tokenBlacklistMapper.existsByTokenHash(tokenHash);
        } catch (Exception e) {
            logger.error("检查令牌黑名单状态失败", e);
            // 出现异常时，为了安全起见，认为令牌已失效
            return true;
        }
    }

    /**
     * 将用户的所有令牌加入黑名单
     * 用于密码修改等场景
     *
     * @param userId 用户ID
     * @param reason 失效原因
     */
    @Transactional
    public void invalidateAllUserTokens(Long userId, String reason) {
        try {
            // 设置一个足够长的过期时间，确保覆盖所有可能的令牌
            LocalDateTime maxExpiredAt = LocalDateTime.now().plusDays(30);

            // 批量失效用户的所有令牌
            int affected = tokenBlacklistMapper.invalidateAllTokensByUserId(userId, reason, maxExpiredAt);

            logger.info("用户所有令牌已失效: userId={}, reason={}, affected={}", userId, reason, affected);

        } catch (Exception e) {
            logger.error("批量失效用户令牌失败: userId={}, reason={}", userId, reason, e);
            throw new RuntimeException("批量失效用户令牌失败", e);
        }
    }

    /**
     * 计算令牌的SHA-256 hash值
     * 用于在数据库中存储令牌的唯一标识
     *
     * @param token JWT令牌
     * @return hash值
     */
    private String calculateTokenHash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            
            // 转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
            
        } catch (NoSuchAlgorithmException e) {
            logger.error("SHA-256算法不可用", e);
            throw new RuntimeException("计算令牌hash失败", e);
        }
    }

    /**
     * 定时清理已过期的黑名单记录
     * 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        try {
            LocalDateTime currentTime = LocalDateTime.now();
            int deleted = tokenBlacklistMapper.cleanupExpiredTokens(currentTime);
            
            if (deleted > 0) {
                logger.info("清理过期黑名单记录完成: deleted={}", deleted);
            }
        } catch (Exception e) {
            logger.error("清理过期黑名单记录失败", e);
        }
    }
}