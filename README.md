# 🏠 Family Accounting | 家庭记账系统

<p align="center">
  <img src="docs/images/logo.png" alt="Family Accounting Logo" width="120" />
</p>

<p align="center">
  <strong>一款面向家庭的协作记账应用，让家庭财务管理更简单</strong>
</p>

<p align="center">
  <a href="#功能特性">功能特性</a> •
  <a href="#技术栈">技术栈</a> •
  <a href="#快速开始">快速开始</a> •
  <a href="#项目结构">项目结构</a> •
  <a href="#api-文档">API 文档</a> •
  <a href="#贡献指南">贡献指南</a>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-8-orange?style=flat-square&logo=java" alt="Java 8" />
  <img src="https://img.shields.io/badge/Spring%20Boot-2.7.18-brightgreen?style=flat-square&logo=spring-boot" alt="Spring Boot" />
  <img src="https://img.shields.io/badge/Vue-2.6-4FC08D?style=flat-square&logo=vue.js" alt="Vue 2" />
  <img src="https://img.shields.io/badge/Vant-2.12-07c160?style=flat-square" alt="Vant" />
  <img src="https://img.shields.io/badge/MySQL-5.7+-blue?style=flat-square&logo=mysql" alt="MySQL" />
  <img src="https://img.shields.io/badge/License-Apache%202.0-yellow?style=flat-square" alt="License" />
</p>

---

## 📱 应用预览

<!-- 请在此处添加应用截图 -->
<p align="center">
  <img src="docs/images/screenshot-login.png" alt="登录页面" width="200" />
  <img src="docs/images/screenshot-transaction.png" alt="记账页面" width="200" />
  <img src="docs/images/screenshot-statistics.png" alt="统计页面" width="200" />
  <img src="docs/images/screenshot-profile.png" alt="我的页面" width="200" />
</p>

## ✨ 功能特性

### 👨‍👩‍👧‍👦 家庭协作
- 创建家庭 - 一键创建家庭，自动成为管理员
- 邀请成员 - 邀请家人加入，共同管理家庭财务
- 成员管理 - 设置成员昵称，管理家庭成员
- 邀请处理 - 接受或拒绝家庭邀请

### 💰 记账功能
- 快速记账 - 简洁的记账界面，支持收入/支出快速切换
- 分类管理 - 树形分类结构，支持自定义收支分类和图标选择
- 多账本 - 支持创建多个账本，灵活管理不同场景
- 交易管理 - 支持编辑和删除交易记录

### 📊 统计分析
- 多维度统计 - 支持日/周/月/年多维度统计
- 可视化图表 - ECharts 图表展示，直观了解收支情况
- 分类明细 - 按分类查看收支占比和明细

### 🔐 安全防护
- 图片验证码 - 登录注册支持图片验证码，防止恶意攻击
- 登录保护 - 15分钟内5次失败自动锁定账户30分钟
- IP 限流 - 单个 IP 每分钟超过100次请求将被限制
- JWT 认证 - 安全的 Token 认证机制
- 令牌黑名单 - 密码修改后自动失效所有现有令牌

## 🛠 技术栈

### 后端
| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 1.8 | 开发语言 |
| Spring Boot | 2.7.18 | 应用框架 |
| Spring Security | - | 安全框架 |
| MyBatis | 2.3.1 | ORM 框架 |
| MySQL | 5.7+ | 数据库 |
| JWT | 0.9.1 | 身份认证 |
| jqwik | 1.7.4 | 属性测试 |

### 前端
| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 2.6.14 | 前端框架 |
| Vant | 2.12.54 | 移动端 UI 组件库 |
| Vue Router | 3.5.1 | 路由管理 |
| Vuex | 3.6.2 | 状态管理 |
| Axios | 0.27.2 | HTTP 客户端 |
| ECharts | 5.4.3 | 图表库 |

## 🚀 快速开始

### 环境要求

- JDK 1.8+
- Maven 3.6+
- Node.js 14+
- MySQL 5.7+

### 1. 克隆项目

```bash
git clone https://github.com/your-username/family-accounting.git
cd family-accounting
```

### 2. 数据库配置

```bash
# 创建数据库
mysql -u root -p
CREATE DATABASE family_accounting DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 导入数据库脚本
mysql -u root -p family_accounting < accounting/src/main/resources/db/schema.sql
mysql -u root -p family_accounting < accounting/src/main/resources/db/data.sql
```

### 3. 后端启动

```bash
cd accounting

# 修改数据库配置
# 编辑 src/main/resources/application.yml

# 启动后端服务
mvn spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动

### 4. 前端启动

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run serve
```

前端服务将在 `http://localhost:8081` 启动

### 5. 构建生产版本

```bash
# 后端打包
cd accounting
mvn clean package -DskipTests

# 前端打包
cd frontend
npm run build
```

## 📁 项目结构

```
family-accounting/
├── accounting/                    # 后端项目
│   ├── src/main/java/
│   │   └── com/family/accounting/
│   │       ├── config/            # 配置类
│   │       │   ├── DefaultCategoryConfig.java  # 默认分类和图标配置
│   │       │   └── SecurityConfig.java         # 安全配置
│   │       ├── controller/        # REST 控制器
│   │       │   ├── AuthController.java         # 认证接口
│   │       │   ├── FamilyController.java       # 家庭管理接口
│   │       │   ├── CategoryController.java     # 分类管理接口
│   │       │   ├── AccountBookController.java  # 账本管理接口
│   │       │   ├── TransactionController.java  # 记账接口
│   │       │   ├── StatisticsController.java   # 统计接口
│   │       │   └── UserController.java         # 用户设置接口
│   │       ├── service/           # 业务逻辑层
│   │       │   ├── UserService.java            # 用户服务
│   │       │   ├── CaptchaService.java         # 验证码服务
│   │       │   ├── SecurityService.java        # 安全防护服务
│   │       │   ├── TokenBlacklistService.java  # 令牌黑名单服务
│   │       │   ├── FamilyService.java          # 家庭服务
│   │       │   ├── CategoryService.java        # 分类服务
│   │       │   ├── AccountBookService.java     # 账本服务
│   │       │   ├── TransactionService.java     # 交易服务
│   │       │   └── StatisticsService.java      # 统计服务
│   │       ├── mapper/            # MyBatis Mapper
│   │       ├── entity/            # 实体类
│   │       ├── dto/               # 数据传输对象
│   │       ├── security/          # 安全认证
│   │       │   ├── JwtAuthenticationFilter.java  # JWT 过滤器
│   │       │   ├── IpRateLimitFilter.java        # IP 限流过滤器
│   │       │   └── SecurityUtils.java            # 安全工具类
│   │       ├── exception/         # 异常处理
│   │       └── util/              # 工具类
│   ├── src/main/resources/
│   │   ├── mapper/                # MyBatis XML
│   │   ├── db/                    # 数据库脚本
│   │   │   ├── schema.sql         # 表结构
│   │   │   ├── data.sql           # 初始数据
│   │   │   └── test-data.sql      # 测试数据
│   │   └── application.yml        # 应用配置
│   └── pom.xml
│
├── frontend/                      # 前端项目
│   ├── public/
│   ├── src/
│   │   ├── api/                   # API 接口封装
│   │   │   ├── index.js           # API 模块导出
│   │   │   ├── request.js         # Axios 配置
│   │   │   └── modules/           # 各模块 API
│   │   ├── assets/                # 静态资源
│   │   │   └── styles/            # 样式文件
│   │   ├── components/            # 公共组件
│   │   │   ├── CategoryPicker.vue # 分类选择器
│   │   │   ├── IconPicker.vue     # 图标选择器
│   │   │   └── TransactionList.vue # 交易列表
│   │   ├── router/                # 路由配置
│   │   ├── store/                 # Vuex 状态管理
│   │   │   └── modules/           # 状态模块
│   │   ├── views/                 # 页面组件
│   │   │   ├── auth/              # 认证页面
│   │   │   │   ├── Login.vue      # 登录页
│   │   │   │   └── Register.vue   # 注册页
│   │   │   ├── layout/            # 布局组件
│   │   │   │   └── Layout.vue     # 底部导航布局
│   │   │   ├── transaction/       # 记账页面
│   │   │   │   └── Transaction.vue
│   │   │   ├── statistics/        # 统计页面
│   │   │   │   └── Statistics.vue
│   │   │   └── profile/           # 个人中心
│   │   │       ├── Profile.vue           # 我的页面
│   │   │       ├── ProfileEdit.vue       # 编辑个人信息
│   │   │       ├── ChangePassword.vue    # 修改密码
│   │   │       ├── FamilyManage.vue      # 家庭管理
│   │   │       ├── Invitations.vue       # 邀请处理
│   │   │       ├── CategoryManage.vue    # 分类管理
│   │   │       └── AccountBookManage.vue # 账本管理
│   │   ├── utils/                 # 工具函数
│   │   ├── App.vue
│   │   └── main.js
│   ├── package.json
│   └── vue.config.js
│
└── docs/                          # 文档
    ├── api.md                     # API 文档
    └── images/                    # 文档图片
```

## 📖 API 文档

### 认证模块

| 方法 | 接口 | 描述 |
|------|------|------|
| GET | `/api/auth/captcha` | 获取图片验证码 |
| POST | `/api/auth/register` | 用户注册 |
| POST | `/api/auth/login` | 用户登录 |
| POST | `/api/auth/logout` | 用户登出 |
| GET | `/api/auth/info` | 获取当前用户信息 |

### 家庭管理

| 方法 | 接口 | 描述 |
|------|------|------|
| POST | `/api/family` | 创建家庭 |
| GET | `/api/family` | 获取家庭信息 |
| PUT | `/api/family` | 更新家庭信息 |
| POST | `/api/family/invite` | 邀请成员 |
| GET | `/api/family/invitations` | 获取待处理邀请 |
| POST | `/api/family/invitation/{id}/accept` | 接受邀请 |
| POST | `/api/family/invitation/{id}/decline` | 拒绝邀请 |
| GET | `/api/family/members` | 获取成员列表 |
| PUT | `/api/family/member/{id}/nickname` | 设置成员昵称 |
| DELETE | `/api/family/member/{id}` | 移除成员 |

### 分类管理

| 方法 | 接口 | 描述 |
|------|------|------|
| GET | `/api/category` | 获取分类树 |
| GET | `/api/category/list` | 获取分类列表（扁平） |
| GET | `/api/category/{id}` | 获取分类详情 |
| POST | `/api/category` | 创建分类 |
| PUT | `/api/category/{id}` | 更新分类 |
| DELETE | `/api/category/{id}` | 删除分类 |
| GET | `/api/category/{id}/can-delete` | 检查是否可删除 |
| GET | `/api/category/icons` | 获取可用图标列表 |

### 账本管理

| 方法 | 接口 | 描述 |
|------|------|------|
| GET | `/api/account-book` | 获取账本列表 |
| GET | `/api/account-book/{id}` | 获取账本详情 |
| GET | `/api/account-book/default` | 获取默认账本 |
| POST | `/api/account-book` | 创建账本 |
| PUT | `/api/account-book/{id}` | 更新账本 |
| DELETE | `/api/account-book/{id}` | 删除账本 |
| PUT | `/api/account-book/{id}/default` | 设为默认账本 |
| GET | `/api/account-book/{id}/can-delete` | 检查是否可删除 |

### 记账模块

| 方法 | 接口 | 描述 |
|------|------|------|
| GET | `/api/transaction` | 获取交易列表 |
| GET | `/api/transaction/{id}` | 获取交易详情 |
| POST | `/api/transaction` | 创建交易记录 |
| PUT | `/api/transaction/{id}` | 更新交易记录 |
| DELETE | `/api/transaction/{id}` | 删除交易记录 |

### 统计模块

| 方法 | 接口 | 描述 |
|------|------|------|
| GET | `/api/statistics/daily` | 日统计 |
| GET | `/api/statistics/weekly` | 周统计 |
| GET | `/api/statistics/monthly` | 月统计 |
| GET | `/api/statistics/yearly` | 年统计 |
| GET | `/api/statistics/category` | 分类统计 |

### 用户设置

| 方法 | 接口 | 描述 |
|------|------|------|
| PUT | `/api/user/password` | 修改密码 |
| PUT | `/api/user/profile` | 更新个人信息 |

> 完整 API 文档请参考 [API Documentation](docs/api.md)

## 🗄 数据库设计

### ER 图

<!-- 请在此处添加 ER 图 -->
<p align="center">
  <img src="docs/images/er-diagram.png" alt="ER Diagram" width="800" />
</p>

### 核心表结构

| 表名 | 说明 |
|------|------|
| `t_user` | 用户表 |
| `t_family` | 家庭表 |
| `t_family_member` | 家庭成员表 |
| `t_invitation` | 邀请表 |
| `t_category` | 分类表（支持树形结构） |
| `t_account_book` | 账本表 |
| `t_transaction` | 交易记录表 |
| `t_captcha` | 验证码表 |
| `t_login_attempt` | 登录尝试记录表 |
| `t_ip_request` | IP 请求记录表 |
| `t_token_blacklist` | 令牌黑名单表 |

## 🧪 测试

项目采用属性测试（Property-Based Testing）确保核心业务逻辑的正确性。

```bash
# 运行后端测试
cd accounting
mvn test

# 运行前端测试
cd frontend
npm run test:unit
```

### 测试覆盖

- ✅ 用户注册创建账户
- ✅ 登录认证往返一致性
- ✅ 家庭创建者成为管理员
- ✅ 分类树结构完整性
- ✅ 交易金额精度保持
- ✅ 统计计算正确性
- ✅ 中文字符存储完整性
- ✅ 密码修改往返验证
- ✅ 登录失败锁定机制
- ✅ IP 地址速率限制
- ✅ 登录尝试记录

## 🔒 安全特性

### 图片验证码
- 4位字母数字组合（排除易混淆字符 0/O/1/I/L）
- 120x40 像素 PNG 图片，包含干扰线和噪点
- 5分钟有效期，验证后立即失效
- 大小写不敏感验证

### 登录保护
- 15分钟内5次失败登录将锁定账户30分钟
- 所有登录尝试记录 IP 地址和时间戳
- 密码修改后所有现有令牌失效

### IP 限流
- 单个 IP 每分钟超过100次请求将被限制
- 基于滑动窗口的请求计数

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

## 📄 开源协议

本项目采用 [Apache-2.0 license](LICENSE) 开源协议。

## 🙏 致谢

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Vue.js](https://vuejs.org/)
- [Vant](https://vant-contrib.gitee.io/vant/)
- [ECharts](https://echarts.apache.org/)
- [jqwik](https://jqwik.net/)

---

<p align="center">
  如果这个项目对你有帮助，请给一个 ⭐️ Star 支持一下！
</p>
