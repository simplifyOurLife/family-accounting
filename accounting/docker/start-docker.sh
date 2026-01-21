#!/bin/bash
# Docker 容器内启动脚本

set -e

echo "=========================================="
echo "家庭记账系统启动中..."
echo "=========================================="

# 检查 jar 文件
if [ ! -f /app/app.jar ]; then
    echo "错误: 未找到 /app/app.jar"
    exit 1
fi

# 创建日志目录
mkdir -p /app/logs

# 设置 JVM 参数
if [ -z "$JAVA_OPTS" ]; then
    JAVA_OPTS="-Xms256m -Xmx512m"
fi

echo "JVM 参数: $JAVA_OPTS"
echo "时区: ${TZ:-Asia/Shanghai}"
echo "=========================================="

# 启动应用
exec java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app/app.jar
