# 家庭记账系统产品 Roadmap 设计

> 编写日期：2026-05-28

## 概述

基于自用场景，围绕"记账效率低、统计分析浅、缺少提醒、数据迁移不便"四个痛点，采用体验优先渐进式路线，分四个阶段逐步完善产品。

## 设计原则

- **自用优先**：不做多租户、商业化等复杂设计，聚焦家庭实际使用场景
- **渐进交付**：每阶段独立可用，不做一半用不上的功能
- **最小侵入**：优先复用现有架构和数据结构，减少迁移成本

---

## 阶段 1：快速记账

**目标**：减少记账操作步骤，让日常记账从 30 秒缩短到 10 秒以内。

### 1.1 记账模板

用户可将常用交易保存为模板，记账时一键复用。

**功能点：**
- 将当前交易保存为模板（保存分类、金额、备注）
- 记账页面顶部展示模板列表，点击即填入表单
- 支持编辑和删除模板
- 每个用户最多 20 个模板

**数据表：`t_transaction_template`**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 模板ID |
| user_id | BIGINT | 用户ID |
| family_id | BIGINT | 家庭ID |
| category_id | BIGINT | 分类ID |
| amount | DECIMAL(12,2) | 金额 |
| note | VARCHAR(200) | 备注 |
| icon | VARCHAR(50) | 图标（冗余，加速展示） |
| sort_order | INT | 排序 |
| created_at | DATETIME | 创建时间 |

**接口：**
- `GET /api/transaction-template` — 获取模板列表
- `POST /api/transaction-template` — 创建模板
- `PUT /api/transaction-template/{id}` — 更新模板
- `DELETE /api/transaction-template/{id}` — 删除模板

### 1.2 最近常用分类置顶

记账时分类选择器顶部显示"最近使用"区域。

**功能点：**
- 分类选择器顶部显示最近使用的 5 个分类
- 基于用户最近 30 天交易记录聚合
- 无需新建表，查询 `t_transaction` 按 user_id 聚合 category_id 并取 Top 5
- 结果可缓存 5 分钟

**接口：**
- `GET /api/category/recent` — 获取最近常用分类

### 1.3 金额快捷输入（计算器键盘）

替换原生数字键盘为自定义计算器键盘。

**功能点：**
- 自定义键盘包含数字、运算符（+、-、×、÷）、退格、确认
- 支持直接输入算式（如 `12.5+3.2`）并计算结果
- 显示实时计算过程
- 纯前端改造，不涉及后端

**技术方案：**
- 新建 `CalculatorKeyboard.vue` 组件
- 使用 `eval` 或自实现表达式解析（安全起见用后者）
- 通过 Vue 事件与记账表单通信

### 1.4 批量快速记账

记完一笔后快速继续记下一笔。

**功能点：**
- 记账成功后不清空日期和账本选择，仅清空金额和备注
- 显示"继续记账"提示，3 秒后自动消失
- 可通过开关切换模式（批量/单笔）
- 纯前端改造

**阶段 1 改动范围：**

| 层级 | 改动 |
|------|------|
| 数据库 | 新增 `t_transaction_template` 表 |
| 后端 | 新增模板 CRUD 接口、常用分类查询接口 |
| 前端 | 模板管理 UI、分类选择器改造、计算器键盘组件、记账流程优化 |

---

## 阶段 2：智能统计

**目标**：让数据会说话，从"记了多少"到"花得怎么样"。

### 2.1 预算管理

支持设置月度总预算和分类预算，实时监控执行进度。

**功能点：**
- 设置月度总预算金额
- 为特定分类设置预算上限
- 统计页面显示预算执行进度条（已花/预算）
- 超支时显示红色警告
- 支持按月份查看历史预算执行情况

**数据表：`t_budget`**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 预算ID |
| family_id | BIGINT | 家庭ID |
| category_id | BIGINT | 分类ID（NULL 表示总预算） |
| amount | DECIMAL(12,2) | 预算金额 |
| month | VARCHAR(7) | 月份（格式：2026-01） |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

**接口：**
- `GET /api/budget?month=2026-01` — 获取某月预算
- `POST /api/budget` — 创建/更新预算
- `GET /api/budget/progress?month=2026-01` — 获取预算执行进度

### 2.2 月度趋势对比

折线图展示收支趋势，支持同比环比。

**功能点：**
- 折线图展示近 6 个月 / 12 个月的收入和支出趋势
- 支持同比对比（今年 vs 去年同月）
- 支持环比对比（本月 vs 上月）
- 在现有 ECharts 统计页面中扩展

**接口：**
- `GET /api/statistics/trend?months=6` — 获取趋势数据
- 返回格式：`[{month, income, expense}, ...]`

### 2.3 成员消费分析

统计页面新增"成员"维度。

**功能点：**
- 展示各家庭成员当月消费排行
- 饼图展示成员消费占比
- 柱状图对比成员消费金额
- 支持切换时间范围

**接口：**
- `GET /api/statistics/member?month=2026-01` — 获取成员消费统计

### 2.4 分类深度分析

支持从大分类钻取到子分类。

**功能点：**
- 点击某个大分类可展开查看子分类占比
- 分类消费变化趋势（该分类近几个月的花销走势）
- 支持从统计页面跳转到该分类下的交易明细列表
- 复用现有分类树结构

**接口：**
- `GET /api/statistics/category/{id}/sub?month=2026-01` — 子分类统计
- `GET /api/statistics/category/{id}/trend?months=6` — 分类趋势

**阶段 2 改动范围：**

| 层级 | 改动 |
|------|------|
| 数据库 | 新增 `t_budget` 表 |
| 后端 | 新增预算 CRUD、趋势查询、成员分析、子分类分析接口 |
| 前端 | 预算管理页面、趋势图表、成员分析图表、分类钻取交互 |

---

## 阶段 3：提醒与周期

**目标**：不漏账单，不忘还款。

### 3.1 周期性账单自动生成

按规则自动创建重复交易。

**功能点：**
- 创建周期规则（分类、金额、备注、周期）
- 支持按月/按周/按年
- 系统按规则在指定日期自动创建交易记录
- 自动生成的记录标记来源，可手动修改
- 支持暂停和删除周期规则

**数据表：`t_recurring_transaction`**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 规则ID |
| user_id | BIGINT | 创建人ID |
| account_book_id | BIGINT | 账本ID |
| category_id | BIGINT | 分类ID |
| type | TINYINT | 类型：1-支出 2-收入 |
| amount | DECIMAL(12,2) | 金额 |
| note | VARCHAR(200) | 备注 |
| frequency | VARCHAR(10) | 周期：MONTHLY/WEEKLY/YEARLY |
| day_of_month | INT | 每月几号（月周期） |
| day_of_week | INT | 星期几（周周期） |
| next_date | DATE | 下次执行日期 |
| active | TINYINT | 是否启用：0-否 1-是 |
| created_at | DATETIME | 创建时间 |

**接口：**
- `GET /api/recurring-transaction` — 获取周期规则列表
- `POST /api/recurring-transaction` — 创建周期规则
- `PUT /api/recurring-transaction/{id}` — 更新规则
- `DELETE /api/recurring-transaction/{id}` — 删除规则
- `PUT /api/recurring-transaction/{id}/toggle` — 启停规则

**调度方案：**
- 使用 Spring `@Scheduled` 每天凌晨执行
- 扫描 `next_date <= 今天` 且 `active = 1` 的规则
- 创建交易记录并更新 `next_date`

### 3.2 还款/缴费提醒

创建提醒事项，到期前通知。

**功能点：**
- 创建提醒（标题、金额、到期日、重复频率、提前提醒天数）
- 支持一次性提醒和周期性提醒
- 到期前提前 N 天提醒
- 提醒列表展示在首页或单独页面

**数据表：`t_reminder`**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 提醒ID |
| user_id | BIGINT | 用户ID |
| family_id | BIGINT | 家庭ID |
| title | VARCHAR(100) | 提醒标题 |
| amount | DECIMAL(12,2) | 金额（可选） |
| due_date | DATE | 到期日 |
| repeat_frequency | VARCHAR(10) | 重复频率：NONE/MONTHLY/WEEKLY/YEARLY |
| advance_days | INT | 提前几天提醒 |
| last_notified_at | DATETIME | 上次提醒时间 |
| active | TINYINT | 是否启用 |
| created_at | DATETIME | 创建时间 |

**接口：**
- `GET /api/reminder` — 获取提醒列表
- `POST /api/reminder` — 创建提醒
- `PUT /api/reminder/{id}` — 更新提醒
- `DELETE /api/reminder/{id}` — 删除提醒

### 3.3 月度账单汇总

每月自动生成上月账单摘要。

**功能点：**
- 每月 1 日自动生成上月汇总（总收入、总支出、结余、各分类占比）
- 汇总可在统计页面查看
- 纯计算逻辑，无需新表，基于现有交易数据聚合

**接口：**
- `GET /api/statistics/monthly-summary?month=2026-01` — 获取月度汇总

**阶段 3 改动范围：**

| 层级 | 改动 |
|------|------|
| 数据库 | 新增 `t_recurring_transaction`、`t_reminder` 表 |
| 后端 | 周期任务调度、提醒服务、月度汇总接口 |
| 前端 | 周期账单管理、提醒管理、月度汇总展示 |

---

## 阶段 4：数据互通

**目标**：数据自由进出，不被锁定。

### 4.1 CSV/Excel 导出

支持按条件导出交易记录。

**功能点：**
- 按账本、时间范围、分类筛选导出
- 导出字段：日期、分类、金额、类型、备注、记录人
- 支持 CSV 和 Excel 两种格式
- 后端生成文件流，前端触发下载

**接口：**
- `GET /api/transaction/export?format=csv&bookId=1&startDate=2026-01-01&endDate=2026-01-31` — 导出

### 4.2 CSV/Excel 导入

支持从其他记账工具导入数据。

**功能点：**
- 上传 CSV/Excel 文件
- 提供导入模板下载
- 导入前预览数据
- 支持字段映射（列对应关系配置）
- 自动匹配已有分类，无法匹配的提示用户手动选择
- 导入结果反馈（成功数、失败数、失败原因）

**接口：**
- `POST /api/transaction/import/upload` — 上传文件
- `POST /api/transaction/import/preview` — 预览映射结果
- `POST /api/transaction/import/confirm` — 确认导入
- `GET /api/transaction/import/template` — 下载导入模板

### 4.3 数据备份与恢复

全量数据备份和恢复。

**功能点：**
- 导出完整数据备份（JSON 格式）
- 备份包含：用户、家庭、分类、账本、交易、模板、预算等所有数据
- 支持从备份文件恢复
- 恢复时支持覆盖模式（清空现有数据）和合并模式（增量添加）

**接口：**
- `GET /api/system/backup` — 导出备份
- `POST /api/system/restore?mode=overwrite|merge` — 恢复备份

**阶段 4 改动范围：**

| 层级 | 改动 |
|------|------|
| 后端 | 导入导出服务（Apache POI）、备份恢复服务 |
| 前端 | 导入导出页面、字段映射交互、备份管理 |
| 依赖 | 新增 Apache POI（Excel 处理） |

---

## 新增数据表汇总

| 阶段 | 表名 | 说明 |
|------|------|------|
| 1 | t_transaction_template | 记账模板 |
| 2 | t_budget | 预算管理 |
| 3 | t_recurring_transaction | 周期性账单规则 |
| 3 | t_reminder | 提醒事项 |

---

## 技术风险与注意事项

1. **周期任务调度**：Spring @Scheduled 在多实例部署时需加分布式锁，当前单实例可直接使用
2. **Excel 导入**：大文件导入需考虑内存限制，建议使用流式读取（SXSSFWorkbook）
3. **预算计算**：月度预算进度需实时计算，建议对当月数据做缓存
4. **分类钻取**：现有分类树最多两层，钻取逻辑简单，无需递归优化
