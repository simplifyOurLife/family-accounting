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
  <img src="https://img.shields.io/badge/License-MIT-yellow?style=flat-square" alt="License" />
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
- **创建家庭** - 一键创建家庭，自动成为管理员
- **邀请成员** - 邀请家人加入，共同管理家庭财务
- **成员管理** - 设置成员昵称，管理家庭成员

### 💰 记账功能
- **快速记账** - 简洁的记账界面，支持收入/支出快速切换
- **分类管理** - 树形分类结构，支持自定义收支分类
- **多账本** - 支持创建多个账本，灵活管理不同场景

### 📊 统计分析
- **多维度统计** - 支持日/周/月/年多维度统计
- **可视化图表** - ECharts 图表展示，直观了解收支情况
- **分类明细** - 按分类查看收支占比和明细

### 🔐 安全防护
- **图片验证码** - 登录注册支持图片验证码，防止恶意攻击
- **登录保护** - 多次失败自动锁定，保护账户安全
- **JWT 认证** - 安全的 Token 认证机制

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
│   │       ├── controller/        # REST 控制器
│   │       ├── service/           # 业务逻辑层
│   │       ├── mapper/            # MyBatis Mapper
│   │       ├── entity/            # 实体类
│   │       ├── dto/               # 数据传输对象
│   │       ├── vo/                # 视图对象
│   │       ├── config/            # 配置类
│   │       ├── security/          # 安全认证
│   │       ├── exception/         # 异常处理
│   │       └── util/              # 工具类
│   ├── src/main/resources/
│   │   ├── mapper/                # MyBatis XML
│   │   ├── db/                    # 数据库脚本
│   │   └── application.yml        # 应用配置
│   └── pom.xml
│
└── frontend/                      # 前端项目
    ├── public/
    ├── src/
    │   ├── api/                   # API 接口封装
    │   ├── assets/                # 静态资源
    │   ├── components/            # 公共组件
    │   ├── router/                # 路由配置
    │   ├── store/                 # Vuex 状态管理
    │   ├── views/                 # 页面组件
    │   │   ├── auth/              # 认证页面
    │   │   ├── transaction/       # 记账页面
    │   │   ├── statistics/        # 统计页面
    │   │   └── profile/           # 个人中心
    │   ├── utils/                 # 工具函数
    │   ├── App.vue
    │   └── main.js
    ├── package.json
    └── vue.config.js
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
| POST | `/api/family/invite` | 邀请成员 |
| GET | `/api/family/members` | 获取成员列表 |

### 记账模块

| 方法 | 接口 | 描述 |
|------|------|------|
| GET | `/api/transaction` | 获取交易列表 |
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

> 完整 API 文档请参考 [API Documentation](docs/api.md)

## 🗄 数据库设计

### ER 图

<!-- 请在此处添加 ER 图 -->
<p align="center">
  <img src="docs/images/er-diagram.png" alt="ER Diagram" width="800" />
</p>

### 核心表结构

- `t_user` - 用户表
- `t_family` - 家庭表
- `t_family_member` - 家庭成员表
- `t_category` - 分类表（支持树形结构）
- `t_account_book` - 账本表
- `t_transaction` - 交易记录表
- `t_captcha` - 验证码表
- `t_login_attempt` - 登录尝试记录表

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

---

<p align="center">
  如果这个项目对你有帮助，请给一个 ⭐️ Star 支持一下！
</p>
