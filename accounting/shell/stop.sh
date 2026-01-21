#!/bin/bash
# 停止应用

PID_FILE="app.pid"

if [ ! -f "$PID_FILE" ]; then
    echo "未找到 PID 文件，尝试查找进程..."
    PID=$(ps aux | grep "accounting-1.0.0-SNAPSHOT.jar" | grep -v grep | awk '{print $2}')
    if [ -z "$PID" ]; then
        echo "应用未运行"
        exit 0
    fi
else
    PID=$(cat "$PID_FILE")
fi

if ps -p $PID > /dev/null 2>&1; then
    echo "正在停止应用 (PID: $PID)..."
    kill $PID
    
    # 等待进程结束
    for i in {1..30}; do
        if ! ps -p $PID > /dev/null 2>&1; then
            echo "应用已停止"
            rm -f "$PID_FILE"
            exit 0
        fi
        sleep 1
    done
    
    # 强制停止
    echo "强制停止应用..."
    kill -9 $PID
    rm -f "$PID_FILE"
    echo "应用已停止"
else
    echo "应用未运行"
    rm -f "$PID_FILE"
fi
