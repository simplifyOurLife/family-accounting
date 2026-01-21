# Docker 部署

## 构建应用

```bash
cd accounting
mvn package
```

构建完成后，在 `target/` 目录会生成：
- `accounting-1.0.0-SNAPSHOT.jar` - 应用 JAR
- `lib/` - 依赖库目录
- `config/` - 配置文件目录

## 构建 Docker 镜像

```bash
cd docker
docker build -t family-accounting:latest .
```

## 部署到 NAS

### 1. 准备文件

将以下文件上传到 NAS：

```
/your/nas/path/
├── app.jar              # 重命名 accounting-1.0.0-SNAPSHOT.jar
├── lib/                 # 依赖库目录
├── config/              # 配置文件目录
│   └── application.yml
└── logs/                # 日志目录（可选，容器会自动创建）
```

### 2. 配置数据库

编辑 `config/application.yml`，修改数据库连接和 JWT 密钥。

### 3. 在 Container Station 创建容器

- **镜像**：`family-accounting:latest`
- **端口映射**：`主机端口` → `6009`
- **挂载卷**：
  - `/your/nas/path/app.jar` → `/app/app.jar`
  - `/your/nas/path/lib` → `/app/lib`
  - `/your/nas/path/config` → `/app/config`
  - `/your/nas/path/logs` → `/app/logs`
- **环境变量**：`TZ=Asia/Shanghai`

## 更新应用

1. 重新构建：`mvn package`
2. 替换 NAS 上的 `app.jar` 文件
3. 在 Container Station 重启容器
