# 职路启航 · 应届生求职赋能平台

职路启航是一个面向应届生的求职赋能平台，当前已形成“学生简历制作与 AI 润色、教师审核与批注评分、岗位浏览与投递、求职进度追踪、管理员监管、站内通知”的本地演示闭环。

## 项目成熟度

当前项目已经比较适合作为课程设计、答辩演示或本地原型系统使用，但还不建议直接作为生产系统上线。

已成熟的部分：

- 学生、教师、管理员三类角色闭环完整。
- 简历、教师审核、岗位投递、进度提醒、批注评分、站内通知均已有可演示流程。
- 后端接口有 JWT 鉴权和基本角色权限校验。
- 管理员后台具备用户管理、教师审核、审核监管、操作日志、平台设置等能力。
- 已提供分阶段数据库迁移脚本和总 schema。
- 后端测试和前端构建已多次验证通过。

仍需增强的部分：

- 密码仍是明文存储，生产前必须改为 BCrypt 等安全哈希。
- 手机号、邮箱等隐私字段未加密。
- 企业端尚未实现，目前只有岗位库和学生投递前置能力。
- 简历导出是可打印 HTML，不是真正服务端 PDF。
- 站内通知第一版不做后台定时调度和关联对象自动跳转。
- 缺少系统化接口测试、端到端测试和生产部署配置。

## 技术栈

后端：

- Java 17
- Spring Boot 3.4
- MyBatis 注解 SQL
- MySQL 8
- JWT

前端：

- Vue 3
- Vue Router
- Pinia
- Element Plus
- Vite

AI：

- DeepSeek Chat Completions API
- 接口由后端统一代理调用

## 目录结构

```text
backend/    Spring Boot 后端
frontend/   Vue 3 前端
database/   数据库总 schema、模拟数据和增量迁移脚本
docs/       项目状态、需求、开发计划和设计文档
```

## 环境要求

- JDK 17+
- MySQL 8+
- Node.js 20.19+ 或 22.12+
- npm
- Windows PowerShell 或其他终端

## 数据库初始化

默认数据库名：

```text
zhilulinghang
```

首次初始化可执行：

```powershell
mysql -u root -p --default-character-set=utf8mb4 < database/01_schema.sql
mysql -u root -p --default-character-set=utf8mb4 < database/02_mock_data.sql
```

PowerShell 如果不支持 `<` 重定向，可使用：

```powershell
Get-Content -Raw database/01_schema.sql | mysql -u root -p --default-character-set=utf8mb4
Get-Content -Raw database/02_mock_data.sql | mysql -u root -p --default-character-set=utf8mb4
```

已有旧数据库时，可按阶段执行增量脚本：

```text
database/03_add_resume_rich_fields.sql
database/04_create_teacher_profile.sql
database/05_create_review_request.sql
database/06_create_review_record_and_resume_versions.sql
database/07_withdraw_and_record_visibility.sql
database/08_admin_controls.sql
database/09_job_and_application.sql
database/10_application_progress.sql
database/11_resume_template.sql
database/12_resume_annotation_score.sql
database/13_notification.sql
```

建议课程演示前使用 `01_schema.sql` + `02_mock_data.sql` 重建一份干净数据。

## 后端配置

复制示例配置：

```powershell
Copy-Item backend/src/main/resources/application-local.example.yml backend/src/main/resources/application-local.yml
```

修改 `backend/src/main/resources/application-local.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/zhilulinghang?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: your_username
    password: your_password

jwt:
  secret: your_secret
  expire-seconds: 86400

deepseek:
  base-url: https://api.deepseek.com
  api-key: ${DEEPSEEK_API_KEY:}
  model: deepseek-v4-flash
  timeout-seconds: 30
```

推荐通过环境变量配置 DeepSeek API Key：

```powershell
$env:DEEPSEEK_API_KEY="your_api_key"
```

不要把真实 API Key 提交到仓库。

## 启动后端

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

默认地址：

```text
http://localhost:8080
```

后端 API 前缀：

```text
http://localhost:8080/api
```

运行后端测试：

```powershell
cd backend
.\mvnw.cmd clean test
```

## 启动前端

安装依赖：

```powershell
cd frontend
npm install
```

启动开发服务器：

```powershell
npm run dev
```

默认地址：

```text
http://127.0.0.1:5173/
```

前端当前 API baseURL 在：

```text
frontend/src/utils/request.js
```

默认配置：

```js
baseURL: "http://localhost:8080/api"
```

构建前端：

```powershell
cd frontend
npm run build
```

当前构建可能出现两个非阻塞 warning：

- `@vueuse/core` pure annotation warning
- chunk size 大于 500 kB warning

只要最终显示 build success，即可用于本地演示。

## 默认账号

模拟数据初始化后可使用：

```text
管理员：admin / 123456
学生：student / 123456
教师：teacher / 123456
学生2：student2 / 123456
```

登录时需要选择与账号匹配的角色。

## 主要页面

学生端：

```text
/student/home
/student/resume/edit
/student/resume/:id
/student/resume/:id/versions
/student/jobs
/student/jobs/:id
/student/applications
/student/review-requests
/student/notifications
```

教师端：

```text
/teacher/home
/teacher/profile
/teacher/requests
/teacher/history
/teacher/notifications
```

管理员端：

```text
/admin/home
/admin/users
/admin/teachers
/admin/teacher-approvals
/admin/resumes
/admin/review-requests
/admin/withdraw-requests
/admin/review-records
/admin/settings
/admin/action-logs
/admin/notifications
```

## 核心业务流程

学生简历流程：

```text
学生登录
→ 创建或选择简历
→ 填写结构化简历
→ 可选 AI 润色
→ 选择简历模板
→ 预览或导出/打印
→ 保存草稿
```

教师审核流程：

```text
学生提交审核请求
→ 选择指定教师或随机分配
→ 教师接受或拒绝请求
→ 教师添加字段级批注与评分
→ 教师可返回新简历版本
→ 学生查看批注、评分和返回版本
```

撤回与监管流程：

```text
教师接受请求后
→ 学生可申请撤回
→ 源简历冻结
→ 管理员同意或拒绝
→ 源简历解冻
→ 相关用户收到站内通知
```

岗位投递流程：

```text
学生浏览岗位
→ 筛选岗位
→ 查看详情
→ 选择自己的简历投递
→ 查看投递记录
→ 手动维护求职状态
→ 添加经验记录和提醒
```

## AI 简历润色

前端接口：

```text
POST /api/resume/polish
```

实际请求地址：

```text
http://localhost:8080/api/resume/polish
```

后端调用 DeepSeek：

```text
{deepseek.base-url}/chat/completions
```

AI 返回不会直接写入数据库。学生需要：

```text
点击 AI 润色
→ 查看润色结果
→ 点击采纳润色结果
→ 再点击保存草稿或提交审核
```

如果提示 AI 服务不可用，请检查：

- `DEEPSEEK_API_KEY` 是否配置
- `deepseek.model` 是否可用
- 网络或代理是否能访问 DeepSeek
- 后端日志中的 HTTP 状态码和错误信息

## 常用验证命令

后端：

```powershell
cd backend
.\mvnw.cmd clean test
```

前端：

```powershell
cd frontend
npm run build
```

数据库检查：

```powershell
mysql -u root -p -D zhilulinghang -e "SHOW TABLES;"
```

## 当前已完成模块

- 登录、注册、JWT 鉴权
- 学生多简历管理
- AI 简历润色
- 简历模板、预览、导出/打印
- 教师资料维护和管理员审核
- 学生提交审核请求
- 教师接受、拒绝、返回新简历
- 学生撤回审核请求
- 管理员处理撤回请求
- 教师字段级批注和评分
- 管理员后台监管
- 用户禁用、密码重置
- 平台设置
- 管理员操作日志
- 岗位库和学生投递
- 求职进度、经验记录、提醒
- 站内通知

## 尚未完成或后续建议

优先建议：

```text
1. 密码 BCrypt 加密与旧密码兼容迁移
2. 隐私字段加密或脱敏展示
3. 企业端账号、企业资料审核、企业岗位发布和企业处理投递
4. 管理员岗位维护页面
5. 真实 PDF 导出
6. 更完整的接口测试和端到端测试
```

企业端计划见：

```text
docs/company_service_plan.md
```

下一阶段计划见：

```text
docs/next_development_plan.md
```

当前实现状态见：

```text
docs/current.md
```

## 生产化注意事项

生产部署前至少需要处理：

- 真实密码哈希，不允许明文密码。
- API Key、JWT secret、数据库密码全部使用环境变量或密钥管理。
- 前后端跨域、HTTPS、反向代理配置。
- 数据库备份和迁移管理。
- 日志脱敏，避免泄露手机号、邮箱和 token。
- 更严格的权限测试。
- 禁止提交 `application-local.yml` 中的真实密钥。
