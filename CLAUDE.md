# CLAUDE.md

本文件为 Claude Code (claude.ai/code) 在本仓库中工作时提供指引。

## 项目概述

家庭记账系统 — 一款面向家庭的协作记账应用，支持家庭成员管理、多账本、分类记账和统计分析。

## 技术栈

- **后端**：Java 8、Spring Boot 2.7.18、Spring Security、MyBatis 2.3.1、MySQL 5.7+、JWT (jjwt 0.9.1)
- **前端**：Vue 2.6、Vant 2.12（移动端 UI）、Vue Router 3.5、Vuex 3.6、Axios、ECharts 5.4
- **测试**：jqwik 1.7.4（属性测试）、H2 内存数据库

## 构建与运行命令

### 后端
```bash
cd accounting
mvn spring-boot:run                    # 启动服务，端口 6009
mvn clean package -DskipTests         # 构建 fat JAR
mvn test                              # 运行测试（pom.xml 中默认跳过）
mvn test -Dmaven.test.skip=false      # 强制运行测试
```

### 前端
```bash
cd frontend
npm install
npm run serve                         # 开发服务器，端口 5010
npm run build                         # 生产环境构建
npm run lint                          # ESLint 检查
npm run test:unit                     # 单元测试
```

## 架构

### 后端 (`accounting/`)
标准 Spring Boot 分层架构：
- `controller/` — REST 接口（统一前缀 `/family-accounting`）
- `service/` — 业务逻辑层
- `mapper/` — MyBatis 接口
- `mapper/*.xml` — SQL 映射文件，位于 `src/main/resources/mapper/`
- `entity/` — 数据库实体
- `dto/` — 数据传输对象（DTO 为请求入参，VO 为响应出参）
- `security/` — JWT 过滤器、IP 限流、安全工具类
- `config/` — 安全配置、默认分类配置
- `exception/` — 全局异常处理，业务异常使用 `BusinessException`

### 前端 (`frontend/`)
Vue 2 单页应用，移动端优先设计：
- `api/` — Axios HTTP 客户端及各模块 API 封装
- `views/` — 页面组件，按功能划分（auth、transaction、statistics、profile）
- `components/` — 共享组件（CategoryPicker、IconPicker、TransactionList）
- `store/` — Vuex 状态管理，支持持久化
- `router/` — 路由配置
- `utils/` — 工具函数

### 数据库
- 建表脚本：`accounting/src/main/resources/db/schema.sql`
- 初始数据：`accounting/src/main/resources/db/data.sql`
- 所有表名以 `t_` 为前缀（t_user、t_family、t_transaction 等）
- 核心实体：User、Family、FamilyMember、Category（树形结构）、AccountBook、Transaction

## 核心设计模式

- **认证**：JWT 令牌，密码修改后令牌加入黑名单失效
- **安全**：IP 限流（100 次/分钟）、登录失败锁定（5 次失败锁定 30 分钟）
- **家庭模型**：用户属于家庭，管理员管理成员和分类
- **分类**：树形结构，通过 parent_id 关联；type 1=支出，2=收入
- **API 响应**：统一使用 `Result<T>` 包装类
- **属性测试**：使用 jqwik 的 `@Property` 注解，基于 H2 数据库运行

## 配置

- 主配置文件：`accounting/src/main/resources/config/application.yml`
- 服务上下文路径：`/family-accounting`
- 服务端口：6009
- JWT 密钥可通过环境变量 `JWT_SECRET` 配置
- MyBatis 自动将下划线列名映射为驼峰命名

## 注意事项

- 测试在 pom.xml 中**默认跳过**（`maven.test.skip=true`），需用 `-Dmaven.test.skip=false` 运行
- application.yml 中数据库配置指向远程 MySQL，本地开发需自行覆盖
- 前端 API 基础地址配置在 `frontend/src/api/request.js`
- 分类图标定义在 `DefaultCategoryConfig.java`
