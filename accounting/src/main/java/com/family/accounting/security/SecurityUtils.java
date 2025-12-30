package com.family.accounting.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全工具类
 * 用于获取当前登录用户信息
 */
public final class SecurityUtils {

    private SecurityUtils() {
        // 私有构造函数，防止实例化
    }

    /**
     * 获取当前登录用户ID
     *
     * @return 用户ID，未登录返回null
     */
    public static Long getCurrentUserId() {
        JwtUserDetails userDetails = getCurrentUserDetails();
        return userDetails != null ? userDetails.getUserId() : null;
    }

    /**
     * 获取当前登录用户手机号
     *
     * @return 手机号，未登录返回null
     */
    public static String getCurrentPhone() {
        JwtUserDetails userDetails = getCurrentUserDetails();
        return userDetails != null ? userDetails.getPhone() : null;
    }

    /**
     * 获取当前登录用户详情
     *
     * @return 用户详情，未登录返回null
     */
    public static JwtUserDetails getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof JwtUserDetails) {
            return (JwtUserDetails) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * 检查当前用户是否已登录
     *
     * @return 是否已登录
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof JwtUserDetails;
    }
}
