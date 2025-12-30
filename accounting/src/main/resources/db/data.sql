-- 家庭记账系统默认数据初始化脚本
-- 此脚本用于在创建新家庭时插入默认分类数据
-- 注意: 此脚本中的 {family_id} 需要在应用层替换为实际的家庭ID

-- =====================================================
-- 默认支出分类 (type = 1)
-- =====================================================

-- 以下是默认分类模板，实际插入时需要替换 family_id

/*
-- 一级支出分类
INSERT INTO t_category (family_id, parent_id, name, type, icon, sort_order) VALUES
({family_id}, NULL, '餐饮', 1, 'food', 1),
({family_id}, NULL, '交通', 1, 'transport', 2),
({family_id}, NULL, '购物', 1, 'shopping', 3),
({family_id}, NULL, '居住', 1, 'home', 4),
({family_id}, NULL, '娱乐', 1, 'entertainment', 5),
({family_id}, NULL, '医疗', 1, 'medical', 6),
({family_id}, NULL, '教育', 1, 'education', 7),
({family_id}, NULL, '通讯', 1, 'communication', 8),
({family_id}, NULL, '其他支出', 1, 'other', 99);

-- 一级收入分类
INSERT INTO t_category (family_id, parent_id, name, type, icon, sort_order) VALUES
({family_id}, NULL, '工资', 2, 'salary', 1),
({family_id}, NULL, '奖金', 2, 'bonus', 2),
({family_id}, NULL, '投资收益', 2, 'investment', 3),
({family_id}, NULL, '兼职', 2, 'parttime', 4),
({family_id}, NULL, '其他收入', 2, 'other', 99);
*/

-- =====================================================
-- 默认分类数据定义（供应用层使用）
-- =====================================================
-- 支出分类:
-- 1. 餐饮 (food)
--    - 早餐、午餐、晚餐、零食、饮料
-- 2. 交通 (transport)
--    - 公交、地铁、打车、加油、停车
-- 3. 购物 (shopping)
--    - 日用品、服饰、数码、家电
-- 4. 居住 (home)
--    - 房租、水电、物业、维修
-- 5. 娱乐 (entertainment)
--    - 电影、游戏、旅游、运动
-- 6. 医疗 (medical)
--    - 药品、挂号、检查、保健
-- 7. 教育 (education)
--    - 学费、书籍、培训、文具
-- 8. 通讯 (communication)
--    - 话费、网费、会员
-- 9. 其他支出 (other)

-- 收入分类:
-- 1. 工资 (salary)
-- 2. 奖金 (bonus)
-- 3. 投资收益 (investment)
-- 4. 兼职 (parttime)
-- 5. 其他收入 (other)
