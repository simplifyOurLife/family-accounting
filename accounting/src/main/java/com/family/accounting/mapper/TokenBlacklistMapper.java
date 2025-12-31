package com.family.accounting.mapper;

import com.family.accounting.entity.TokenBlacklist;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * JWT令牌黑名单数据访问层
 */
@Mapper
public interface TokenBlacklistMapper {

    /**
     * 插入黑名单记录
     *
     * @param tokenBlacklist 黑名单记录
     * @return 影响行数
     */
    int insert(TokenBlacklist tokenBlacklist);

    /**
     * 检查令牌是否在黑名单中
     *
     * @param tokenHash 令牌hash值
     * @return 是否存在
     */
    boolean existsByTokenHash(@Param("tokenHash") String tokenHash);

    /**
     * 根据用户ID将所有令牌加入黑名单
     *
     * @param userId 用户ID
     * @param reason 失效原因
     * @param maxExpiredAt 最大过期时间（用于批量失效）
     * @return 影响行数
     */
    int invalidateAllTokensByUserId(@Param("userId") Long userId, 
                                   @Param("reason") String reason,
                                   @Param("maxExpiredAt") LocalDateTime maxExpiredAt);

    /**
     * 清理已过期的黑名单记录
     * 删除令牌过期时间早于当前时间的记录
     *
     * @param currentTime 当前时间
     * @return 删除的记录数
     */
    int cleanupExpiredTokens(@Param("currentTime") LocalDateTime currentTime);
}