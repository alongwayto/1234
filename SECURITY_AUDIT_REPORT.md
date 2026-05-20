# 智能校园设备管理系统 - 安全审计报告

**审计日期**: 2024年  
**系统**: Campus Equipment Management System  
**技术栈**: SpringBoot 2.7 + Vue 3 + MySQL 8.0  

---

## 执行摘要

本安全审计发现了 **15 个安全问题**，其中包括 **5 个高风险** 和 **10 个中风险** 问题。

---

## 发现的安全问题

### 🔴 高风险问题

#### 1. CORS 配置过于宽松
**位置**: `backend/src/main/java/com/campus/equipment/config/WebMvcConfig.java:14`

问题: 允许所有来源的跨域请求
```java
.allowedOriginPatterns("*")
.allowCredentials(true)
```

修复: 限制为具体域名
```java
.allowedOrigins("https://yourdomain.com")
```

---

#### 2. XSS 漏洞 - AI 聊天模块
**位置**: `frontend/src/views/ai/index.vue:71`

问题: 使用 v-html 渲染未转义的内容
```vue
<div class="msg-content" v-html="formatMsg(msg.content)"></div>
```

修复: 使用 DOMPurify 清理 HTML
```bash
npm install dompurify
```

---

#### 3. 数据库 SSL 连接未启用
**位置**: `backend/src/main/resources/application.yml:11`

问题: useSSL=false

修复: 启用 SSL
```yaml
url: jdbc:mysql://localhost:3306/campus_equipment?useSSL=true
```

---

#### 4. 文件上传缺乏验证
**位置**: `backend/src/main/java/com/campus/equipment/controller/AuthController.java:77`

问题: 未验证文件类型和大小

修复: 添加文件验证逻辑

---

#### 5. 数据库备份命令注入风险
**位置**: `backend/src/main/java/com/campus/equipment/controller/BackupController.java:79`

问题: ProcessBuilder 使用未验证的参数

修复: 验证参数并使用环境变量传递密码

---

### 🟠 中风险问题

#### 6. 硬编码的默认凭证
- 数据库密码: 123456
- 用户密码: admin123
- 前端显示默认账号

修复: 使用环境变量

---

#### 7. 调试日志启用
**位置**: `application.yml:27`

```yaml
logging:
  level:
    com.campus.equipment: debug
```

修复: 改为 info

---

#### 8. 缺少安全响应头
- X-Frame-Options
- X-Content-Type-Options
- Content-Security-Policy

---

#### 9. 缺少速率限制
无 API 速率限制，容易被暴力破解

---

#### 10. localStorage 存储敏感信息
Token 存储在 localStorage，容易被 XSS 窃取

修复: 使用 HttpOnly Cookie

---

#### 11. 异常处理泄露信息
返回详细的异常消息

修复: 返回通用错误消息，详情记录到日志

---

#### 12. 缺少输入验证
某些端点缺少充分的输入验证

---

#### 13. CSRF 保护被禁用
```java
.csrf().disable()
```

---

#### 14. JWT 密钥硬编码
应从环境变量读取

---

#### 15. 缺少安全审计日志
未记录安全事件（登录失败、权限拒绝等）

---

## 修复优先级

### 立即修复（P0）
1. 修复 XSS 漏洞
2. 限制 CORS
3. 启用数据库 SSL
4. 验证文件上传
5. 修复命令注入

### 短期修复（P1）
6. 移除硬编码凭证
7. 禁用调试日志
8. 添加安全响应头
9. 实现速率限制
10. 改进异常处理

### 长期改进（P2）
11. 使用 HttpOnly Cookie
12. 增强输入验证
13. 实现安全审计日志
14. 定期更新依赖
15. 实现 WAF

---

## 部署检查清单

- [ ] 所有硬编码凭证已替换为环境变量
- [ ] HTTPS 已启用
- [ ] 数据库 SSL 连接已启用
- [ ] CORS 已限制为特定域名
- [ ] 安全响应头已配置
- [ ] 调试模式已禁用
- [ ] 文件上传验证已实现
- [ ] 速率限制已实现
- [ ] 依赖已更新到最新安全版本

---

**审计完成**: 2024年
