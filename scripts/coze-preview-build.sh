#!/usr/bin/env bash
set -euo pipefail

# 预览构建脚本 - 智能校园设备管理系统
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_DIR"

# 安装前端依赖
if [ -d "frontend/node_modules" ]; then
    echo "依赖已安装，跳过..."
else
    echo "安装前端依赖..."
    cd frontend
    pnpm install
    cd "$PROJECT_DIR"
fi

echo "预览构建完成"
