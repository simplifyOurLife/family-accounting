-- 家庭记账系统数据库初始化脚本
-- 数据库: MySQL 5.7.x
-- 字符集: utf8mb4

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS family_accounting
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE family_accounting;

-- =====================================================
-- 1. 用户表 (t_user)
-- =====================================================
CREATE TABLE IF NOT EXISTS t_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    phone VARCHAR(20) NOT NULL COMMENT '手机号',
    password VARCHAR(100) NOT NULL COMMENT '密码(BCrypt加密)',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar VARCHAR(255) COMMENT '头像URL',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =====================================================
-- 2. 家庭表 (t_family)
-- =====================================================
CREATE TABLE IF NOT EXISTS t_family (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '家庭ID',
    name VARCHAR(50) NOT NULL COMMENT '家庭名称',
    admin_id BIGINT NOT NULL COMMENT '管理员用户ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT fk_family_admin FOREIGN KEY (admin_id) REFERENCES t_user(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='家庭表';


-- =====================================================
-- 3. 家庭成员表 (t_family_member)
-- =====================================================
CREATE TABLE IF NOT EXISTS t_family_member (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '成员记录ID',
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    nickname VARCHAR(50) COMMENT '在家庭中的昵称',
    joined_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    UNIQUE KEY uk_family_user (family_id, user_id),
    CONSTRAINT fk_member_family FOREIGN KEY (family_id) REFERENCES t_family(id) ON DELETE CASCADE,
    CONSTRAINT fk_member_user FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='家庭成员表';

-- =====================================================
-- 4. 邀请表 (t_invitation)
-- =====================================================
CREATE TABLE IF NOT EXISTS t_invitation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '邀请ID',
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    inviter_id BIGINT NOT NULL COMMENT '邀请人ID',
    invitee_id BIGINT NOT NULL COMMENT '被邀请人ID',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-待处理 1-已接受 2-已拒绝 3-已过期',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    expired_at DATETIME COMMENT '过期时间',
    CONSTRAINT fk_invitation_family FOREIGN KEY (family_id) REFERENCES t_family(id) ON DELETE CASCADE,
    CONSTRAINT fk_invitation_inviter FOREIGN KEY (inviter_id) REFERENCES t_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_invitation_invitee FOREIGN KEY (invitee_id) REFERENCES t_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邀请表';

-- =====================================================
-- 5. 分类表 (t_category)
-- =====================================================
CREATE TABLE IF NOT EXISTS t_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    parent_id BIGINT DEFAULT NULL COMMENT '父分类ID',
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    type TINYINT NOT NULL COMMENT '类型: 1-支出 2-收入',
    icon VARCHAR(50) COMMENT '图标',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    CONSTRAINT fk_category_family FOREIGN KEY (family_id) REFERENCES t_family(id) ON DELETE CASCADE,
    CONSTRAINT fk_category_parent FOREIGN KEY (parent_id) REFERENCES t_category(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分类表';


-- =====================================================
-- 6. 账本表 (t_account_book)
-- =====================================================
CREATE TABLE IF NOT EXISTS t_account_book (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '账本ID',
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    name VARCHAR(50) NOT NULL COMMENT '账本名称',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认账本: 0-否 1-是',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT fk_book_family FOREIGN KEY (family_id) REFERENCES t_family(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账本表';

-- =====================================================
-- 7. 交易记录表 (t_transaction)
-- =====================================================
CREATE TABLE IF NOT EXISTS t_transaction (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '交易ID',
    account_book_id BIGINT NOT NULL COMMENT '账本ID',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    user_id BIGINT NOT NULL COMMENT '记录人ID',
    type TINYINT NOT NULL COMMENT '类型: 1-支出 2-收入',
    amount DECIMAL(12,2) NOT NULL COMMENT '金额',
    note VARCHAR(200) COMMENT '备注',
    transaction_date DATE NOT NULL COMMENT '交易日期',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_book_date (account_book_id, transaction_date),
    INDEX idx_user (user_id),
    INDEX idx_category (category_id),
    CONSTRAINT fk_trans_book FOREIGN KEY (account_book_id) REFERENCES t_account_book(id) ON DELETE CASCADE,
    CONSTRAINT fk_trans_category FOREIGN KEY (category_id) REFERENCES t_category(id) ON DELETE RESTRICT,
    CONSTRAINT fk_trans_user FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='交易记录表';
-- =====================================================
-- 8. 图片验证码表 (t_captcha)
-- =====================================================
CREATE TABLE IF NOT EXISTS t_captcha (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '验证码ID',
    captcha_key VARCHAR(36) NOT NULL COMMENT '验证码唯一标识(UUID)',
    code VARCHAR(10) NOT NULL COMMENT '验证码内容',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    expired_at DATETIME NOT NULL COMMENT '过期时间',
    UNIQUE KEY uk_captcha_key (captcha_key),
    INDEX idx_expired (expired_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图片验证码表';

-- =====================================================
-- 9. 登录尝试记录表 (t_login_attempt)
-- =====================================================
CREATE TABLE IF NOT EXISTS t_login_attempt (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    phone VARCHAR(20) COMMENT '手机号',
    ip_address VARCHAR(45) NOT NULL COMMENT 'IP地址',
    success TINYINT NOT NULL COMMENT '是否成功: 0-失败 1-成功',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_phone_time (phone, created_at),
    INDEX idx_ip_time (ip_address, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录尝试记录表';

-- =====================================================
-- 10. IP请求记录表 (t_ip_request)
-- =====================================================
CREATE TABLE IF NOT EXISTS t_ip_request (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    ip_address VARCHAR(45) NOT NULL COMMENT 'IP地址',
    request_path VARCHAR(255) COMMENT '请求路径',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_ip_time (ip_address, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IP请求记录表';

-- =====================================================
-- 11. JWT令牌黑名单表 (t_token_blacklist)
-- =====================================================
CREATE TABLE IF NOT EXISTS t_token_blacklist (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    token_hash VARCHAR(255) NOT NULL COMMENT '令牌hash值',
    reason VARCHAR(100) NOT NULL COMMENT '失效原因',
    token_expired_at DATETIME NOT NULL COMMENT '令牌原始过期时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_token_hash (token_hash),
    INDEX idx_user_id (user_id),
    INDEX idx_expired_at (token_expired_at),
    CONSTRAINT fk_blacklist_user FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='JWT令牌黑名单表';

-- =====================================================
-- 12. 搜索历史表 (t_search_history)
-- =====================================================
CREATE TABLE IF NOT EXISTS t_search_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '搜索历史ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    keyword VARCHAR(200) NOT NULL COMMENT '搜索关键词',
    search_count INT DEFAULT 1 COMMENT '搜索次数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_family (user_id, family_id),
    INDEX idx_keyword (keyword),
    INDEX idx_updated_at (updated_at),
    CONSTRAINT fk_search_user FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_search_family FOREIGN KEY (family_id) REFERENCES t_family(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='搜索历史表';

-- =====================================================
-- 13. 保存的筛选条件表 (t_saved_filter)
-- =====================================================
CREATE TABLE IF NOT EXISTS t_saved_filter (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '筛选条件ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    family_id BIGINT NOT NULL COMMENT '家庭ID',
    name VARCHAR(50) NOT NULL COMMENT '筛选条件名称',
    filter_json TEXT NOT NULL COMMENT '筛选条件JSON',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_family (user_id, family_id),
    CONSTRAINT fk_filter_user FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_filter_family FOREIGN KEY (family_id) REFERENCES t_family(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='保存的筛选条件表';

-- =====================================================
-- 添加交易记录表的搜索优化索引
-- =====================================================
-- 备注全文索引（用于关键词搜索）
ALTER TABLE t_transaction ADD FULLTEXT INDEX idx_note_fulltext (note) WITH PARSER ngram;

-- 金额范围查询索引
ALTER TABLE t_transaction ADD INDEX idx_amount (amount);

-- 复合查询索引（账本+日期+金额）
ALTER TABLE t_transaction ADD INDEX idx_book_date_amount (account_book_id, transaction_date, amount);

-- 类型筛选索引
ALTER TABLE t_transaction ADD INDEX idx_type (type);