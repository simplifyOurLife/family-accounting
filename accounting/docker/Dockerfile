# 使用 JRE 基础镜像
FROM eclipse-temurin:8-jre-alpine

# 安装字体支持（验证码需要）
RUN apk add --no-cache fontconfig ttf-dejavu bash

# 创建应用用户
RUN addgroup -g 1000 appuser && adduser -D -u 1000 -G appuser appuser

WORKDIR /app
USER appuser
EXPOSE 6009

# 复制启动脚本
COPY start-docker.sh /app/start.sh
RUN chmod +x /app/start.sh

# 启动
ENTRYPOINT ["/app/start.sh"]

