#!/usr/bin/env bash
set -euo pipefail

# 预览运行脚本 - 智能校园设备管理系统
# 前端独立预览，不需要后端服务
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_DIR"

# 显式声明关键环境变量
export PORT=5000
export VITE_API_BASE_URL=""

# 清理 5000 端口残留进程（绝不碰 9000）
fuser -k 5000/tcp 2>/dev/null || true
sleep 1

# 启动前端预览服务
cd "$PROJECT_DIR/frontend"
exec pnpm exec vite --host 0.0.0.0 --port 5000
