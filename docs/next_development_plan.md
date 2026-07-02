# 下一阶段开发计划

## 目标

本计划对照 `docs/需求规格说明.md` 与当前实现状态，指导项目从“简历编辑 + 教师审核 + 管理后台”继续扩展为完整的应届生求职赋能平台。

下一阶段的主线是补齐需求规格说明中的核心闭环：

```text
简历准备
→ 岗位检索与匹配
→ 简历投递
→ 求职进度追踪
→ 经验记录与提醒
→ 教师批注评分与反馈
```

当前不优先实现真实支付、真实企业接口、学校统一认证、真实邮件网关、复杂推荐算法。第一版以本地数据库和可演示闭环为主。

## 当前基础

已完成能力：

```text
登录/注册
学生多简历管理
AI 简历润色
教师资料与管理员审核
学生提交教师审核请求
教师接受/拒绝请求
教师返回独立简历版本
学生撤回审核申请
管理员处理撤回
教师端隐藏审核记录
管理员后台监管、平台设置、操作日志、禁用用户、重置密码
```

当前主要缺口：

```text
岗位库
岗位检索
岗位匹配
岗位投递
投递记录
求职状态追踪
提醒任务
求职经验记录
简历模板
简历导出
教师字段级批注
教师评分
站内通知
密码加密与敏感字段安全增强
```

## 开发原则

1. 先完成可演示业务闭环，再优化推荐算法和外部集成。
2. 新增功能优先使用当前项目已有模式：Spring Boot Controller + MyBatis Mapper + Vue 页面 + Element Plus。
3. 每个阶段都要同步更新：
   - `database/01_schema.sql`
   - `database/02_mock_data.sql`
   - 增量迁移脚本
   - `docs/current.md`
4. 每阶段完成后运行：
   - 后端：`.\mvnw.cmd clean test`
   - 前端：`npm run build`
5. 不在学生端暴露管理员监管接口；教师只能访问与自己相关的请求、记录、批注。

## 阶段一：岗位库与投递闭环

执行状态：已完成。

### 目标

实现 `UC-3.2 精准岗位匹配与投递` 的最小可用版本。

学生可以浏览岗位、筛选岗位、查看岗位详情，并选择自己的简历进行投递。投递后系统生成投递记录。

### 数据库设计

新增表：`job`

```text
id
title
company
industry
city
salary_range
requirement
description
status
create_time
update_time
```

字段说明：

```text
status:
  OPEN      招聘中
  CLOSED    已关闭
```

新增表：`job_application`

```text
id
student_id
resume_id
job_id
status
apply_time
update_time
```

字段说明：

```text
status:
  APPLIED       已投递
  SCREENING     筛选中
  WRITTEN_TEST  笔试
  INTERVIEW     面试
  OFFER         Offer
  REJECTED      已拒绝
  CLOSED        已结束
```

约束建议：

```text
job_application.student_id -> user.id
job_application.resume_id -> resume.id
job_application.job_id -> job.id
UNIQUE(student_id, job_id)
```

新增迁移脚本：

```text
database/09_job_and_application.sql
```

### 后端改动

新增模型：

```text
Job.java
JobApplication.java
```

新增 Mapper：

```text
JobMapper.java
JobApplicationMapper.java
```

新增 Controller：

```text
JobController.java
JobApplicationController.java
```

接口：

```text
GET  /api/jobs
GET  /api/jobs/{id}
POST /api/jobs/{id}/apply
GET  /api/applications/student
GET  /api/applications/{id}
POST /api/applications/{id}/status
```

`GET /api/jobs` 支持查询参数：

```text
keyword
industry
city
status
```

`POST /api/jobs/{id}/apply` 请求体：

```text
resumeId
```

业务规则：

1. 只有 `STUDENT` 可以投递岗位。
2. 只能使用自己的简历投递。
3. 只能投递 `status = OPEN` 的岗位。
4. 同一学生不能重复投递同一岗位。
5. 投递成功后创建 `job_application`，初始状态为 `APPLIED`。
6. 学生可以手动更新自己的投递状态，用于第一版本地进度管理。

### 前端改动

新增 API 文件：

```text
frontend/src/api/job.js
frontend/src/api/application.js
```

新增学生页面：

```text
/student/jobs
/student/jobs/:id
/student/applications
```

页面功能：

`/student/jobs`

```text
岗位列表
关键词搜索
行业筛选
城市筛选
状态筛选
点击进入详情
```

`/student/jobs/:id`

```text
岗位详情
选择简历
投递按钮
投递结果提示
```

`/student/applications`

```text
投递记录列表
岗位名称
公司
使用简历
当前状态
投递时间
手动更新状态
```

布局改动：

```text
StudentLayout.vue 增加“岗位列表”和“投递记录”菜单
router/index.js 增加学生端路由
```

### 管理员补充

第一版可以先由 mock 数据提供岗位。若时间允许，增加管理员岗位监管页：

```text
/admin/jobs
```

接口：

```text
GET  /api/admin/manage/jobs
POST /api/admin/manage/jobs
POST /api/admin/manage/jobs/{id}
POST /api/admin/manage/jobs/{id}/status
```

若开发时间紧张，管理员岗位管理放入阶段二之后。

### 验收标准

1. 学生能打开岗位列表并筛选岗位。
2. 学生能查看岗位详情。
3. 学生能选择自己的简历投递岗位。
4. 重复投递同一岗位会被拒绝并提示。
5. 投递记录能在学生端展示。
6. 学生能手动更新投递状态。
7. 后端测试与前端构建通过。

### 完成情况

已完成：

```text
job 表
job_application 表
database/09_job_and_application.sql
GET  /api/jobs
GET  /api/jobs/{id}
POST /api/jobs/{id}/apply
GET  /api/applications/student
GET  /api/applications/{id}
POST /api/applications/{id}/status
/student/jobs
/student/jobs/:id
/student/applications
StudentLayout.vue 菜单入口
```

实现规则：

```text
只有学生可以投递岗位
学生只能使用自己的简历投递
只能投递 OPEN 岗位
同一学生不能重复投递同一岗位
投递成功后生成 APPLIED 状态记录
学生可手动更新投递状态
```

## 阶段二：求职进度、提醒与经验记录

执行状态：已完成。

### 目标

实现 `UC-3.3 求职进度管理` 的本地版。

学生可以围绕投递记录维护状态、提醒和经验复盘，不依赖真实企业反馈接口。

### 数据库设计

新增表：`application_experience`

```text
id
application_id
student_id
stage
content
create_time
update_time
```

`stage` 建议：

```text
WRITTEN_TEST
INTERVIEW
OFFER
GENERAL
```

新增表：`application_reminder`

```text
id
application_id
student_id
remind_type
remind_time
content
status
create_time
update_time
```

`status` 建议：

```text
PENDING
DONE
CANCELLED
```

新增迁移脚本：

```text
database/10_application_progress.sql
```

### 后端改动

新增模型：

```text
ApplicationExperience.java
ApplicationReminder.java
```

新增 Mapper：

```text
ApplicationExperienceMapper.java
ApplicationReminderMapper.java
```

新增接口：

```text
GET  /api/applications/{id}/experiences
POST /api/applications/{id}/experiences
PUT  /api/application-experiences/{id}
DELETE /api/application-experiences/{id}

GET  /api/applications/{id}/reminders
POST /api/applications/{id}/reminders
POST /api/application-reminders/{id}/done
DELETE /api/application-reminders/{id}
GET  /api/application-reminders/student/pending
```

业务规则：

1. 学生只能管理自己的投递记录下的经验和提醒。
2. 提醒时间必须是未来时间。
3. 第一版不做后台定时推送，只在学生端展示待提醒事项。
4. 已完成或取消的提醒不再出现在待提醒列表。

### 前端改动

增强页面：

```text
/student/applications
```

新增能力：

```text
投递详情抽屉或详情页
状态时间线
经验记录列表
新增/编辑/删除经验记录
提醒列表
新增提醒
标记提醒完成
```

学生首页增强：

```text
显示待提醒事项
显示最近投递进度
```

### 验收标准

1. 学生能为投递记录添加经验记录。
2. 学生能为投递记录设置提醒。
3. 学生首页或投递页能看到待提醒事项。
4. 学生只能访问自己的经验和提醒。
5. 后端测试与前端构建通过。

### 完成情况

已完成：

```text
application_experience 表
application_reminder 表
database/10_application_progress.sql
GET  /api/applications/{id}/experiences
POST /api/applications/{id}/experiences
PUT  /api/applications/experiences/{id}
DELETE /api/applications/experiences/{id}
GET  /api/applications/{id}/reminders
POST /api/applications/{id}/reminders
POST /api/applications/reminders/{id}/done
DELETE /api/applications/reminders/{id}
GET  /api/applications/student/reminders/pending
/student/applications 投递详情抽屉、状态时间线、经验记录、提醒管理
/student/home 待提醒事项展示
```

实现规则：

```text
学生只能管理自己的投递记录下的经验和提醒
经验记录必须填写内容
提醒时间必须晚于当前时间
提醒支持标记完成和删除
第一版不做后台定时推送，只在学生端展示待提醒事项
```

## 阶段三：简历模板、预览与导出

执行状态：已完成。

### 目标

增强 `UC-3.1 智能简历制作与优化`。

当前系统已有结构化简历和 AI 润色，但缺少模板库、模板化预览和导出能力。

### 数据库设计

新增表：`resume_template`

```text
id
name
industry
job_type
structure
style
enabled
create_time
update_time
```

`resume` 表建议新增：

```text
template_id
```

新增迁移脚本：

```text
database/11_resume_template.sql
```

### 后端改动

新增：

```text
ResumeTemplate.java
ResumeTemplateMapper.java
ResumeTemplateController.java
```

接口：

```text
GET /api/resume-templates
GET /api/resume-templates/{id}
GET /api/resume/{id}/preview
GET /api/resume/{id}/export
```

第一版导出策略：

```text
优先实现 HTML 预览
PDF 导出可先返回可打印页面，后续再接 PDF 生成库
```

### 前端改动

增强：

```text
ResumeEditView.vue
ResumeVersionsView.vue
```

新增能力：

```text
选择简历模板
模板化预览
打印/导出入口
展示版本链来源
```

### 验收标准

1. 学生能选择简历模板。
2. 简历能以模板样式预览。
3. 学生可以打开可打印导出页面。
4. 原有保存、AI 润色、教师返回版本流程不被破坏。

### 完成情况

已完成：

```text
resume_template 表
resume.template_id 字段
database/11_resume_template.sql
GET /api/resume-templates
GET /api/resume-templates/{id}
GET /api/resume/{id}/preview
GET /api/resume/{id}/export
ResumeEditView.vue 模板选择、模板化预览、导出/打印
ResumeVersionsView.vue 返回版本预览、导出/打印、版本类型展示
```

实现规则：

```text
新增简历默认使用第一个启用模板
学生保存简历时保存 template_id
教师返回版本继承源简历模板
预览和导出由后端生成 HTML
导出第一版使用可打印 HTML，不引入 PDF 生成库
```

## 阶段四：教师批注与评分

执行状态：已完成。

### 目标

补齐 `UC-3.4 简历审核与批注` 中的字段级批注和评分。

当前教师审核体系支持返回新简历和评论，但没有结构化批注和评分。

### 数据库设计

新增表：`resume_annotation`

```text
id
request_id
resume_id
teacher_id
student_id
field_name
mark_type
content
create_time
update_time
```

字段说明：

```text
field_name:
  title
  name
  phone
  email
  target_position
  education
  experience
  skills
  awards
  self_evaluation

mark_type:
  TEXT
  STRUCTURE
  KEYWORD
```

新增表：`resume_score`

```text
id
request_id
resume_id
teacher_id
student_id
score
remark
create_time
update_time
```

新增迁移脚本：

```text
database/12_resume_annotation_score.sql
```

### 后端改动

新增模型与 Mapper：

```text
ResumeAnnotation.java
ResumeScore.java
ResumeAnnotationMapper.java
ResumeScoreMapper.java
```

新增接口：

```text
GET  /api/review-requests/{id}/annotations
POST /api/review-requests/{id}/annotations
DELETE /api/resume-annotations/{id}

GET  /api/review-requests/{id}/score
POST /api/review-requests/{id}/score
```

业务规则：

1. 只有已接受该请求的教师可以添加批注和评分。
2. 批注必须绑定到具体字段。
3. 分数范围为 `0-100`。
4. 学生可以查看自己相关请求的批注和评分。
5. 管理员可以查看所有批注和评分。

### 前端改动

教师端增强：

```text
/teacher/requests
```

或新增：

```text
/teacher/requests/:id/review
```

能力：

```text
在简历字段旁添加批注
查看已有批注
删除自己的批注
填写总分和评分说明
返回新简历时可同时提交评分
```

学生端增强：

```text
/student/review-requests
/student/resume/:id/versions
```

能力：

```text
查看教师批注
查看教师评分
按字段展示反馈
```

管理员端增强：

```text
/admin/review-records
```

能力：

```text
查看批注和评分摘要
```

### 验收标准

1. 教师能为已接受请求添加字段级批注。
2. 教师能提交评分。
3. 学生能查看批注和评分。
4. 管理员能监管批注和评分。
5. 教师不能修改他人批注。

### 完成情况

已完成：

```text
resume_annotation 表
resume_score 表
database/12_resume_annotation_score.sql
GET  /api/review-requests/{id}/annotations
POST /api/review-requests/{id}/annotations
DELETE /api/resume-annotations/{id}
GET  /api/review-requests/{id}/score
POST /api/review-requests/{id}/score
GET  /api/admin/manage/resume-annotations
GET  /api/admin/manage/resume-scores
/teacher/requests 批注与评分弹窗
/student/review-requests 查看教师批注与评分
/admin/review-records 批注监管与评分监管
```

实现规则：

```text
教师只能给自己已接受的审核请求添加批注和评分
批注必须绑定到具体简历字段
批注类型支持 TEXT、STRUCTURE、KEYWORD
评分范围限制为 0-100
同一审核请求只有一条评分，重复提交会更新评分
学生只能查看自己相关审核请求的批注和评分
管理员可以查看所有批注和评分
教师只能删除自己的批注，管理员可删除任意批注
```

## 阶段五：站内通知

执行状态：已完成。

### 目标

实现基础通知机制，为审核反馈、撤回处理、提醒事项提供统一入口。

### 数据库设计

新增表：`notification`

```text
id
user_id
type
title
content
read_status
related_type
related_id
create_time
read_time
```

状态：

```text
UNREAD
READ
```

新增迁移脚本：

```text
database/13_notification.sql
```

### 后端改动

新增：

```text
Notification.java
NotificationMapper.java
NotificationController.java
NotificationService.java
```

接口：

```text
GET  /api/notifications
GET  /api/notifications/unread-count
POST /api/notifications/{id}/read
POST /api/notifications/read-all
```

需要触发通知的动作：

```text
教师接受审核请求
教师拒绝审核请求
教师返回新简历
学生申请撤回
管理员同意撤回
管理员拒绝撤回
投递提醒到期
教师资料审核通过/拒绝
```

第一版提醒到期可以只通过查询待提醒事项实现，不做后台调度。

### 前端改动

布局增强：

```text
StudentLayout.vue
TeacherLayout.vue
AdminLayout.vue
```

新增：

```text
/notifications
```

能力：

```text
未读数量
通知列表
标记已读
全部已读
点击跳转关联对象
```

### 验收标准

1. 关键业务动作能生成通知。
2. 用户只能看到自己的通知。
3. 未读数量准确。
4. 用户可以标记通知已读。

### 完成情况

已完成：

```text
notification 表
database/13_notification.sql
Notification.java
NotificationMapper.java
NotificationService.java
NotificationController.java
GET  /api/notifications
GET  /api/notifications/unread-count
POST /api/notifications/{id}/read
POST /api/notifications/read-all
/student/notifications
/teacher/notifications
/admin/notifications
StudentLayout.vue 站内通知入口
TeacherLayout.vue 站内通知入口
AdminLayout.vue 站内通知入口
```

已接入通知触发：

```text
教师接受审核请求 -> 通知学生
教师拒绝审核请求 -> 通知学生
教师返回新简历 -> 通知学生
学生申请撤回 -> 通知教师和管理员
管理员同意撤回 -> 通知学生和教师
管理员拒绝撤回 -> 通知学生和教师
教师资料审核通过 -> 通知教师
教师资料审核拒绝 -> 通知教师
```

实现规则：

```text
用户只能查看自己的通知
用户可以将单条通知标记为已读
用户可以一键全部已读
未读数量接口已提供
第一版通知页不做关联对象自动跳转
第一版不做后台定时调度，投递提醒仍通过学生端待提醒列表展示
```

## 阶段六：安全与非功能增强

### 目标

逐步满足需求规格说明中的安全性与可维护性要求。

### 密码安全

当前问题：

```text
密码仍是明文保存
```

改造：

```text
引入 BCrypt
注册时加密密码
登录时 BCrypt 校验
管理员重置密码时保存加密值
兼容旧明文密码一次性迁移或登录时升级
```

### 审计日志增强

当前 `admin_action_log` 缺少：

```text
ip
user_agent
```

建议新增字段：

```text
ip_address
user_agent
```

扩展记录动作：

```text
修改简历
删除简历
投递岗位
修改投递状态
创建/修改岗位
修改平台配置
```

### 权限增强

检查点：

```text
学生只能访问自己的简历、投递、经验、提醒、通知
教师只能访问自己的审核请求、批注、评分
管理员才能访问 /api/admin/**
被禁用用户持有旧 token 时必须被拦截
```

### 数据保护

需求规格说明要求手机号、邮箱等隐私字段加密。

建议拆到后续专门阶段：

```text
先定义加密服务接口
再迁移 resume.phone / resume.email
再处理查询、展示和导出兼容
```

### 验收标准

1. 新注册用户密码不再明文入库。
2. 管理员重置密码后仍可登录。
3. 旧账号能平滑登录或完成迁移。
4. 审计日志包含 IP 和时间戳。
5. 权限测试覆盖主要角色边界。

## 推荐执行顺序

建议按以下顺序推进：

```text
1. 阶段一：岗位库与投递闭环
2. 阶段二：求职进度、提醒与经验记录
3. 阶段三：简历模板、预览与导出
4. 阶段四：教师批注与评分
5. 阶段五：站内通知
6. 阶段六：安全与非功能增强
```

如果课程展示时间紧张，最小展示版本建议完成：

```text
阶段一
阶段二的投递状态与经验记录
阶段四的评分
```

这样可以展示完整故事：

```text
学生创建简历
→ AI 润色
→ 找岗位
→ 投递岗位
→ 追踪进度
→ 邀请教师审核
→ 教师评分与返回修改版
→ 管理员监管
```

## 风险与注意事项

### 数据库迁移风险

已有数据库可能缺少新增字段或表。每次新增结构都必须提供独立迁移脚本，并在 `docs/current.md` 中写明。

### 当前文档滞后风险

`docs/current.md` 前半部分仍保留早期 MVP 描述，后半部分记录了新功能。后续阶段建议顺手清理旧描述，避免“旧 ReviewController 流程”和“新 review_request 流程”混在一起造成误判。

### 需求范围风险

需求规格说明中提到：

```text
学校统一认证
招聘平台外部接口
SMTP 邮件
Redis
HTTPS
真实 PDF 导出
隐私字段 AES 加密
```

这些不建议马上实现。当前阶段应先完成业务闭环，再做外部服务集成。

### 代码结构风险

当前项目大量使用注解 SQL。新增模块时可以继续保持一致，但要避免 Controller 中堆太多业务校验。若某个流程明显复杂，应新增 Service，例如：

```text
JobApplicationService
NotificationService
ResumeExportService
```

## 每阶段完成后的文档更新清单

每完成一个阶段，都需要更新：

```text
docs/current.md
docs/next_development_plan.md
database/01_schema.sql
database/02_mock_data.sql
对应增量迁移脚本
```

并记录：

```text
新增表
新增字段
新增接口
新增页面
业务规则
未完成事项
验证命令结果
```
