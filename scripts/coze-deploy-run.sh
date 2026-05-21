#!/usr/bin/env bash
set -euo pipefail

# 部署运行脚本 - 智能校园设备管理系统
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_DIR"

echo "启动生产服务..."

# 启动后端
cd "$PROJECT_DIR/backend"
nohup mvn spring-boot:run > /workspace/projects/.logs/backend-deploy.log 2>&1 &

# 启动静态文件服务（使用nginx或简单HTTP服务器）
# 假设dist目录已准备好
echo "部署服务已启动"
