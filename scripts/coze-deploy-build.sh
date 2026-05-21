#!/usr/bin/env bash
set -euo pipefail

# 部署构建脚本 - 智能校园设备管理系统
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_DIR"

echo "开始部署构建..."

# 构建前端
cd "$PROJECT_DIR/frontend"
pnpm run build

echo "部署构建完成"
