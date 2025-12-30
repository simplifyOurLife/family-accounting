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
