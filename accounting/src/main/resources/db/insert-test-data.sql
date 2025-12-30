-- 插入测试数据脚本
-- 注意：这个脚本假设已经有用户、家庭、分类和账本数据

-- 首先检查是否有用户数据
SELECT 'Users:' as info, COUNT(*) as count FROM t_user;
SELECT 'Families:' as info, COUNT(*) as count FROM t_family;
SELECT 'Categories:' as info, COUNT(*) as count FROM t_category;
SELECT 'Account Books:' as info, COUNT(*) as count FROM t_account_book;
SELECT 'Transactions:' as info, COUNT(*) as count FROM t_transaction;

-- 如果有数据，插入一些测试交易记录
-- 假设用户ID=1, 账本ID=1, 分类ID从1开始

-- 插入测试交易记录（只有在有基础数据的情况下才会成功）
INSERT IGNORE INTO t_transaction (account_book_id, category_id, user_id, type, amount, note, transaction_date) VALUES
-- 今天的记录
(1, 1, 1, 1, 25.50, '午餐', CURDATE()),
(1, 2, 1, 1, 12.00, '地铁', CURDATE()),
(1, 3, 1, 1, 89.90, '日用品', CURDATE()),

-- 昨天的记录
(1, 1, 1, 1, 18.00, '早餐', CURDATE() - INTERVAL 1 DAY),
(1, 2, 1, 1, 35.00, '打车', CURDATE() - INTERVAL 1 DAY),
(1, 4, 1, 1, 2500.00, '房租', CURDATE() - INTERVAL 1 DAY),

-- 前天的记录
(1, 1, 1, 1, 45.80, '晚餐', CURDATE() - INTERVAL 2 DAY),
(1, 5, 1, 1, 68.00, '电影票', CURDATE() - INTERVAL 2 DAY),

-- 收入记录（假设收入分类从ID 10开始）
(1, 10, 1, 2, 8000.00, '月工资', CURDATE() - INTERVAL 5 DAY),
(1, 13, 1, 2, 500.00, '兼职收入', CURDATE() - INTERVAL 3 DAY);

-- 再次检查数据
SELECT 'After insert - Transactions:' as info, COUNT(*) as count FROM t_transaction;