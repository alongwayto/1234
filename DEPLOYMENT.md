# 智能校园设备管理系统部署与交付说明

## 环境要求

| 组件 | 建议版本 |
| --- | --- |
| JDK | 11 或 17 |
| Maven | 3.6+ |
| Node.js | 18+ |
| MySQL | 8.0+ |
| 浏览器 | Chrome、Edge、Firefox 最新稳定版 |

项目支持 Windows 和 Linux 部署。后端为 Spring Boot 2.7，前端为 Vue 3 + Element Plus，数据库为 MySQL 8。

## 数据库初始化

```bash
mysql -u root -p < backend/src/main/resources/sql/schema.sql
mysql -u root -p campus_equipment < backend/src/main/resources/sql/data.sql
```

默认账号均为 `admin123`：

| 账号 | 角色 |
| --- | --- |
| admin | 超级管理员 |
| maintainer1 | 维护员 |
| user1 | 普通用户 |

## 后端部署

```bash
cd backend
mvn clean package -DskipTests

java -jar target/equipment-management-1.0.0.jar \
  --spring.datasource.url="jdbc:mysql://127.0.0.1:3306/campus_equipment?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false" \
  --spring.datasource.username=root \
  --spring.datasource.password=your-password \
  --jwt.secret=replace-with-a-long-random-secret
```

Swagger 地址：

```text
http://localhost:8080/api/swagger-ui.html
```

## 前端部署

```bash
cd frontend
npm install
npm run build
```

将 `frontend/dist` 部署到 Nginx，接口代理到后端：

```nginx
server {
    listen 80;
    server_name example.com;
    root /var/www/campus-equipment/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 备份与恢复

系统管理中的“数据备份”支持：

- 手动备份
- 每天凌晨 2:00 自动备份
- 备份文件下载
- 从指定备份恢复数据库

后端需要能访问 `mysqldump` 和 `mysql` 命令。可通过启动参数指定：

```bash
--backup.path=/data/campus-equipment/backups/
--backup.mysql-dump-path=/usr/bin/mysqldump
--backup.mysql-path=/usr/bin/mysql
```

Windows 可配置为 MySQL 安装目录中的可执行文件路径。

## 权限模型

| 角色 | 能力范围 |
| --- | --- |
| 超级管理员 | 全部功能、用户角色、备份恢复、派单归档 |
| 维护员 | 设备维护、设备导入导出、告警处理、工单处理、数据统计 |
| 普通用户 | 查看设备、查看监控、上报故障、查看个人信息 |

## 测试与构建检查

```bash
cd backend
mvn test

cd ../frontend
npm run build
```

后端包含 JWT 工具和设备 DTO 校验单元测试。前端构建会检查 Vue 组件、路由懒加载、ECharts 与 Element Plus 依赖是否可正常打包。
