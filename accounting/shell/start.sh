#!/bin/bash
# 启动应用

APP_JAR="accounting-1.0.0-SNAPSHOT.jar"
PID_FILE="app.pid"

# 检查是否已经在运行
if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if ps -p $PID > /dev/null 2>&1; then
        echo "应用已在运行 (PID: $PID)"
        exit 1
    fi
fi

# 检查文件
if [ ! -f "$APP_JAR" ]; then
    echo "错误: 找不到 $APP_JAR"
    exit 1
fi

if [ ! -d "lib" ]; then
    echo "错误: 找不到 lib 目录"
    exit 1
fi

# 创建日志目录
mkdir -p logs

# 启动应用
echo "正在启动应用..."
nohup java -Xms256m -Xmx512m -jar $APP_JAR > logs/app.log 2>&1 &

# 保存 PID
echo $! > $PID_FILE
echo "应用已启动 (PID: $(cat $PID_FILE))"
echo "日志: tail -f logs/app.log"
