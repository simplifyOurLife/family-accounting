-- 测试数据插入脚本
-- 用于验证统计功能

-- 注意：运行此脚本前请确保已有用户、家庭、分类和账本数据

-- 插入一些测试交易记录
-- 假设用户ID=1, 家庭ID=1, 账本ID=1, 分类ID从1开始

-- 支出记录
INSERT INTO t_transaction (account_book_id, category_id, user_id, type, amount, note, transaction_date) VALUES
-- 餐饮支出
(1, 1, 1, 1, 25.50, '午餐', CURDATE()),
(1, 1, 1, 1, 18.00, '早餐', CURDATE() - INTERVAL 1 DAY),
(1, 1, 1, 1, 45.80, '晚餐', CURDATE() - INTERVAL 2 DAY),

-- 交通支出  
(1, 2, 1, 1, 12.00, '地铁', CURDATE()),
(1, 2, 1, 1, 35.00, '打车', CURDATE() - INTERVAL 1 DAY),

-- 购物支出
(1, 3, 1, 1, 199.00, '衣服', CURDATE() - INTERVAL 3 DAY),
(1, 3, 1, 1, 89.90, '日用品', CURDATE() - INTERVAL 5 DAY),

-- 居住支出
(1, 4, 1, 1, 2500.00, '房租', CURDATE() - INTERVAL 1 DAY),
(1, 4, 1, 1, 150.00, '水电费', CURDATE() - INTERVAL 7 DAY),

-- 娱乐支出
(1, 5, 1, 1, 68.00, '电影票', CURDATE() - INTERVAL 2 DAY),
(1, 5, 1, 1, 120.00, '健身房', CURDATE() - INTERVAL 10 DAY);

-- 收入记录
INSERT INTO t_transaction (account_book_id, category_id, user_id, type, amount, note, transaction_date) VALUES
-- 工资收入 (假设工资分类ID为支出分类数+1，即约为10)
(1, 10, 1, 2, 8000.00, '月工资', CURDATE() - INTERVAL 5 DAY),
(1, 10, 1, 2, 8000.00, '月工资', CURDATE() - INTERVAL 35 DAY),

-- 奖金收入
(1, 11, 1, 2, 2000.00, '季度奖金', CURDATE() - INTERVAL 15 DAY),

-- 兼职收入
(1, 13, 1, 2, 500.00, '兼职收入', CURDATE() - INTERVAL 3 DAY),
(1, 13, 1, 2, 300.00, '兼职收入', CURDATE() - INTERVAL 8 DAY);