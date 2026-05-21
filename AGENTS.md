# 智能校园设备管理系统 - Agent 项目规范

## 项目概述

智能校园设备管理系统是一个基于 Spring Boot + Vue 3 的前后端分离系统，用于校园设备的智能化管理。系统支持设备状态实时监控、AI智能诊断、故障管理、预测性维护等功能。

## 技术栈

### 后端
- **框架**: Spring Boot 2.7.18
- **ORM**: MyBatis-Plus 3.5.5
- **数据库**: MySQL 8.0
- **安全**: Spring Security + JWT
- **文档**: SpringDoc OpenAPI (Swagger 3)
- **WebSocket**: Spring WebSocket（STOMP协议）
- **AI集成**: coze-coding-dev-sdk

### 前端
- **框架**: Vue 3 + Composition API
- **UI库**: Element Plus 2.4.4
- **图表**: ECharts 5.4.3 + vue-echarts
- **状态管理**: Pinia 2.1.7
- **路由**: Vue Router 4
- **构建**: Vite 4.5.3

## 目录结构

```
/workspace/projects/
├── backend/                 # Spring Boot 后端
│   └── src/main/java/com/campus/equipment/
│       ├── config/          # 配置类
│       ├── controller/      # 控制器
│       ├── service/         # 服务层
│       ├── mapper/          # 数据访问层
│       ├── entity/          # 实体类
│       ├── dto/             # 数据传输对象
│       ├── vo/              # 视图对象
│       ├── websocket/       # WebSocket处理（实时监控）
│       ├── ai/              # AI集成模块（智能诊断）
│       ├── aspect/          # 切面（日志记录）
│       └── task/            # 定时任务
├── frontend/               # Vue 3 前端
│   └── src/
│       ├── api/            # API接口
│       │   ├── ai/         # AI智能体API
│       │   └── lifecycle/  # 生命周期API
│       ├── components/      # 公共组件
│       │   ├── ai/         # AI组件
│       │   └── monitor/    # 监控组件
│       ├── views/           # 页面视图
│       │   ├── ai/         # AI页面
│       │   ├── dashboard/  # 仪表盘
│       │   └── lifecycle/  # 生命周期页面
│       ├── store/           # Pinia状态
│       ├── utils/           # 工具类
│       └── router/          # 路由配置
└── scripts/               # 构建和部署脚本
```

## 核心模块

### 1. 设备管理模块
- 设备CRUD操作
- 设备分类管理
- 位置管理
- 设备导入导出（Excel）

### 2. 状态监控模块（智能增强）
- **WebSocket实时推送**: 设备状态实时更新
- **AI智能诊断**: 基于运行数据分析故障
- **预警管理**: 多级别预警（低/中/高/紧急）

### 3. 设备生命周期管理
- 采购记录
- 维修记录
- 更换记录
- **预测性维护提醒**: 基于设备运行数据的预测

### 4. AI智能体聊天
- 设备咨询问答
- 故障自诊指导
- 报修流程引导
- 对话历史保存

### 5. 数据可视化
- 设备运行趋势图
- 资源占用率仪表盘
- 故障统计报表
- 维护成本分析

### 6. 权限管理
- RBAC权限体系
- 角色: 管理员、维护员、普通用户
- 操作日志审计

## 数据库关键表

| 表名 | 说明 |
|------|------|
| sys_user | 系统用户 |
| sys_role | 角色 |
| sys_permission | 权限 |
| device_info | 设备信息 |
| device_status_record | 设备状态记录 |
| device_alert | 设备预警 |
| fault_report | 故障上报 |
| fault_work_order | 故障工单 |
| device_lifecycle | 设备生命周期记录 |
| predictive_maintenance | 预测性维护记录 |
| ai_chat_session | AI聊天会话 |
| ai_chat_message | AI聊天消息 |
| sys_operation_log | 操作日志 |

## API接口前缀

- `/api/device/**` - 设备管理
- `/api/monitor/**` - 状态监控
- `/api/fault/**` - 故障管理
- `/api/ai/**` - AI智能体
- `/api/statistics/**` - 统计分析
- `/api/system/**` - 系统管理
- `/api/device/lifecycle/**` - 设备生命周期管理
- `/api/predictive-maintenance/**` - 预测性维护

## WebSocket端点

- `/ws/equipment` - 设备状态实时推送
- STOMP协议，支持订阅主题

## 运行环境

- JDK: 11 或 17
- Node.js: 18+
- MySQL: 8.0+
- Maven: 3.6+

## 默认账号

| 账号 | 密码 | 角色 |
|------|------|------|
| admin | admin123 | 超级管理员 |
| maintainer1 | admin123 | 维护员 |
| user1 | admin123 | 普通用户 |

## 运行命令

### 后端
```bash
cd backend
mvn spring-boot:run
```

### 前端
```bash
cd frontend
pnpm install
pnpm run dev
```

## 注意事项

1. 后端使用端口 8080，前端开发服务器端口 5173
2. 预览模式前端需代理API到后端
3. AI功能需要配置 DOUBAO_API_KEY 环境变量
4. MySQL使用端口3307，数据库名 campus_equipment
5. 预览端口固定为 5000

## 预览链路配置

- 项目根目录: `/workspace/projects/`
- 技术项目目录: `/workspace/projects/frontend/` (前端) 和 `/workspace/projects/backend/` (后端)
- 预览入口: `pnpm exec vite --host 0.0.0.0 --port 5000`
- 构建脚本: `scripts/coze-preview-build.sh`
- 运行脚本: `scripts/coze-preview-run.sh`
- 预览服务: 5000 端口，监听 0.0.0.0
