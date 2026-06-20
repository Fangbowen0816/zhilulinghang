前端代码
前端入口在 main.js。它创建 Vue 应用，并挂载了：
Pinia：管理登录用户状态
Vue Router：页面路由
Element Plus：UI 组件库
路由在 index.js。目前分三类页面：
text



/login

/admin/home
/admin/users
/admin/resumes

/student/home
/student/resume/edit
/student/resume/:id
/student/resume/feedback

/teacher/home
/teacher/review
/teacher/review/:id

路由守卫做两件事：
没有 token 时，只能访问 /login
已登录后，按照 role 限制访问范围，例如学生不能进 /admin 或 /teacher
登录状态存在 user.js。它把 username、token、role 同步保存到 localStorage，刷新页面后仍然保持登录。
接口请求统一在 request.js。这里配置了：
js



baseURL: "http://localhost:8080/api"

所以前端请求 /auth/login 时，实际访问的是：
text



http://localhost:8080/api/auth/login

请求拦截器会自动加 JWT：
text



Authorization: Bearer token

登录页在 LoginView.vue。现在已经不是 mock 登录了，而是调用后端 loginApi，成功后保存后端返回的 token 和 role，再跳转到对应首页。
学生核心页面是：
ResumeEditView.vue
ResumeFeedbackView.vue
StudentHomeView.vue 现在会显示当前学生的所有简历。
每一份简历都是 resume 表中的一条独立实体记录，学生可以在首页滚动列表中点击简历名称进入对应简历界面。
ResumeEditView.vue 负责：
加载自己的简历
填写简历名称、姓名、手机号、邮箱、求职意向、教育经历、项目经历、技能、奖项证书、自我评价
保存草稿
提交审核
预览简历
显示当前状态和教师反馈
保存草稿时要求姓名必填，并校验手机号和邮箱格式。
简历名称可以不手动填写，保存时会自动生成，例如“张同学 - Java 后端开发实习生”。
提交审核前会额外要求填写求职意向，并至少填写教育经历、项目经历或技能中的一项。
教师核心页面是：
ReviewListView.vue
ReviewDetailView.vue
教师流程是：
ReviewListView.vue 获取待审核简历列表
点击“审核”进入详情页
ReviewDetailView.vue 展示简历详情
教师填写意见
点击“审核通过”或“退回修改”
前端接口文件主要是：
auth.js：登录、获取当前用户
resume.js：学生简历相关接口
review.js：教师审核相关接口


后端代码
后端入口是 BackendApplication.java。它启动 Spring Boot 应用。
Maven 配置在 pom.xml，主要依赖有：
spring-boot-starter-web：提供 REST API
mybatis-spring-boot-starter：连接数据库、写 SQL Mapper
mysql-connector-j：MySQL 驱动
spring-boot-starter-test：测试
配置文件在：
application.yml
application-local.yml
application-local.example.yml
application-local.yml 里配置了本地 MySQL、端口 8080、JWT secret。
后端模型有：
User：用户，包含 id/username/password/role/createTime
Resume：简历，包含学生 ID、简历名称、学生姓名、联系方式、求职意向、简历内容、状态、教师意见、创建/更新时间
对应文件在：
User.java
Resume.java
数据库访问层是 MyBatis Mapper：
UserMapper.java
ResumeMapper.java
它们直接用注解 SQL 查询数据库，例如：
根据用户名查用户
根据学生 ID 查简历
插入简历
更新简历
提交审核
教师审核
认证相关代码在 security 包。核心是：
JwtUtil：生成和解析 JWT
AuthInterceptor：拦截 /api/** 请求，检查 token
AuthContext：保存当前请求里的登录用户
AuthUser：当前登录用户信息
接口权限现在是通过代码判断实现的。例如学生接口会检查当前用户是否是 STUDENT 或 ADMIN，教师审核接口会检查是否是 TEACHER 或 ADMIN。
后端控制器主要有：
AuthController.java
ResumeController.java
ReviewController.java
AuthController 提供：
text



POST /api/auth/login
POST /api/auth/register
GET  /api/auth/me

登录逻辑是：
根据用户名查用户
比较密码
检查前端选择的角色是否匹配数据库角色
生成 token
返回 token/userId/username/role
注册逻辑是：
只允许注册 STUDENT 或 TEACHER
用户名不能重复
教师注册后自动创建 teacher_profile
教师注册后的资料默认为待管理员审核，且不能接收请求
ResumeController 提供学生简历接口：
text



GET  /api/resume/me
GET  /api/resume/my
GET  /api/resume/{id}
POST /api/resume
PUT  /api/resume/{id}
POST /api/resume/{id}/submit
DELETE /api/resume/{id}
GET  /api/resume/feedback

它保证学生只能查看、编辑、删除和提交自己的简历。
ReviewController 提供教师审核接口：
text



GET  /api/review/list
GET  /api/review/{id}
POST /api/review/{id}

教师只能修改审核状态和教师意见，不能改学生填写的简历正文。
数据库脚本在：
01_schema.sql
02_mock_data.sql
03_add_resume_rich_fields.sql
目前建了两张核心表：
text



user
resume

resume 表中的每一条记录代表一份独立简历。
核心字段包括：
text



id
student_id
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
status
teacher_comment
create_time
update_time

其中 title 是简历名称，用于学生区分多份简历，例如“Java 后端校招简历”“前端实习简历”。
name 是简历正文里的学生姓名。
如果本地数据库已经存在旧版 resume 表，可以执行 03_add_resume_rich_fields.sql 增量增加这些字段；也可以重新执行 01_schema.sql 和 02_mock_data.sql 重建示例数据。

并初始化了账号：
text



admin / 123456
student / 123456
teacher / 123456
student2 / 123456

完整业务流程
现在项目的主流程是：
text



学生登录
→ 学生填写简历
→ 保存草稿
→ 提交审核
→ 教师登录
→ 查看待审核简历
→ 填写审核意见
→ 审核通过或退回
→ 学生重新登录
→ 查看反馈
→ 修改简历并重新提交

这就是当前 MVP 的核心闭环。


AI 简历润色功能
学生简历编辑页 ResumeEditView.vue 现在提供 AI 润色服务。
学生先填写原始简历信息，至少需要姓名、教育经历、项目经历、技能中的任意一项有内容。
润色要求/目标是可选项，例如：
text



面向 Java 后端校招，突出项目成果和技术能力

点击“AI 润色”后，前端调用：
text



POST /api/resume/polish

前端接口在 resume.js：
js



polishResumeApi(data)

请求体包含：
text



name
phone
email
targetPosition
education
experience
skills
awards
selfEvaluation
goal

后端入口在 ResumeController.java：
text



POST /api/resume/polish

该接口只允许 STUDENT 或 ADMIN 调用。
后端不会直接保存 AI 返回内容，只返回润色结果给前端。
前端用弹窗展示润色后的姓名、教育经历、项目经历、技能和润色说明。
学生点击“采纳润色结果”后，润色内容才会写回当前表单。
写回表单后仍然需要学生点击“保存草稿”或“提交审核”，数据库才会更新。

AI 服务代码在：
text



ResumePolishService.java
DeepSeekProperties.java
ResumePolishRequest.java
ResumePolishResponse.java
AiServiceException.java

ResumePolishService.java 负责：
构造后端 system_prompt
合并学生原始简历和润色目标
调用 DeepSeek API
解析 AI 返回 JSON
校验字段和长度
返回结构化润色结果

DeepSeek 配置项是：
yaml



deepseek:
  base-url: https://api.deepseek.com
  api-key: ${DEEPSEEK_API_KEY:}
  model: deepseek-v4-flash
  timeout-seconds: 30

本地运行时需要配置 DEEPSEEK_API_KEY，或者在 application-local.yml 中配置 deepseek.api-key。
真实 API Key 不应该提交到代码仓库。

后端要求 DeepSeek 返回 JSON，字段为：
text



title
name
phone
email
targetPosition
education
experience
skills
awards
selfEvaluation
summary

如果 DeepSeek 未配置、调用失败、返回格式错误或内容为空，后端会返回错误信息，前端弹出失败提示，不会修改当前简历表单。
DeepSeek 调用失败时，后端会按 HTTP 状态码返回更明确的错误信息，例如 API Key 无效、权限不足、额度或频率限制、请求参数错误、网络超时或 DeepSeek 服务端异常。


教师资料与注册
当前已完成教师审核体系改造的第一阶段。
详细计划保存在：
text



docs/teacher_review_plan.md

新增数据库表：
text



teacher_profile

教师资料字段包括：
text



id
teacher_id
display_name
department
title
bio
expertise_tags
available
approval_status
approval_comment
approved_by
approved_time
create_time
update_time

教师注册后会自动创建 teacher_profile：
text



available = false
approval_status = PENDING

只有管理员审核通过后，教师才能开启接收请求。

新增接口：
text



GET /api/teachers
GET /api/teachers/{id}
GET /api/teacher/profile
PUT /api/teacher/profile

GET  /api/admin/teacher-profiles
GET  /api/admin/teacher-profiles/pending
POST /api/admin/teacher-profiles/{id}/approve
POST /api/admin/teacher-profiles/{id}/reject

新增前端页面：
text



/register
/teacher/profile
/admin/teacher-approvals

本阶段没有实现审核请求、教师接收/拒绝请求、教师返回新简历和撤回流程，这些属于后续阶段。


审核请求
当前已完成教师审核体系改造的第二阶段。
新增数据库表：
text



review_request

review_request 字段包括：
text



id
source_resume_id
student_id
teacher_id
assign_mode
status
student_message
teacher_reply
decline_reason
decline_suggestion
create_time
update_time

assign_mode 当前支持：
text



SELECTED
RANDOM

status 当前支持：
text



PENDING
ACCEPTED
DECLINED
CANCELLED
COMPLETED

当前已实现流程：
text



学生在简历编辑页点击提交审核
→ 选择指定教师或系统随机分配
→ 创建 review_request
→ 教师在教师端审核请求页面查看请求
→ 教师接受或拒绝请求
→ 学生在审核请求页面查看状态

规则：
随机分配第一版只分配 1 位教师。
只有 approval_status = APPROVED 且 available = true 的教师可以被选择或随机分配。
同一份源简历不能重复发送给同一教师。
教师拒绝请求时必须填写拒绝理由和建议。

新增接口：
text



POST /api/review-requests
GET  /api/review-requests/student
GET  /api/review-requests/teacher
GET  /api/review-requests/{id}
POST /api/review-requests/{id}/accept
POST /api/review-requests/{id}/decline

新增前端页面：
text



/student/review-requests
/teacher/requests

本阶段没有实现教师返回新简历、审核记录 review_record、学生撤回申请和管理员处理，这些属于后续阶段。


教师返回新简历
当前已完成教师审核体系改造的第三阶段。
resume 表新增版本来源字段：
text



source_resume_id
generated_by_teacher_id
version_type

version_type 当前主要使用：
text



ORIGINAL
TEACHER_RETURNED

新增数据库表：
text



review_record

review_record 字段包括：
text



id
request_id
source_resume_id
returned_resume_id
student_id
teacher_id
action
comment
teacher_deleted
create_time

当前流程：
text



教师接受审核请求
→ 教师基于源简历编辑返回版本
→ 提交返回
→ 系统创建新的 resume 实体
→ 原简历不被覆盖
→ review_request.status = COMPLETED
→ 写入 review_record
→ 学生可以查看返回版本并继续编辑

新增接口：
text



POST /api/review-requests/{id}/return
GET  /api/review-records/student
GET  /api/review-records/teacher
GET  /api/resume/{id}/versions

新增前端能力：
教师端 /teacher/requests 中，已接受的请求可以点击“返回”生成新的简历版本。
学生端简历编辑页可以查看某份源简历的教师返回版本。
学生端 /student/review-requests 可以查看审核记录，并进入返回版本继续编辑。

本阶段没有实现学生撤回申请、管理员处理撤回、简历冻结和教师端隐藏审核记录，这些属于第四阶段。


撤回与记录管理
当前已完成教师审核体系改造的第四阶段。

review_request 新增字段：
text



withdraw_reason
admin_decision
admin_comment
admin_id
admin_time

resume 新增字段：
text



frozen
freeze_reason

新增接口：
text



POST /api/review-requests/{id}/withdraw
GET  /api/admin/withdraw-requests
POST /api/admin/withdraw-requests/{id}/approve
POST /api/admin/withdraw-requests/{id}/reject
POST /api/review-records/{id}/hide

当前流程：
text



教师接受审核请求
→ 学生可以申请撤回
→ 学生填写撤回原因
→ 请求进入 WITHDRAW_PENDING
→ 源简历被冻结
→ 管理员同意或拒绝撤回
→ 源简历解冻

冻结规则：
撤回处理中，源简历不能编辑、删除、提交或再次发起审核请求。

教师端记录管理：
教师可以在审核历史页面删除自己的审核记录。
该删除只是教师端隐藏，服务器数据仍保留，管理员视图后续仍可查看。

新增前端页面：
text



/admin/withdraw-requests
/teacher/history


管理员后台
当前已完成管理员后台第一轮建设。
详细计划保存在：
text



docs/admin_backend_plan.md

管理端当前页面包括：
text



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

/admin/home 是管理员工作台，展示学生、教师、待审核教师、简历、冻结简历、审核请求、撤回待处理、审核记录等统计。

/admin/teacher-approvals 用于教师资料审核，只显示待审核教师，审核通过或拒绝后从列表移除。

/admin/teachers 用于全量教师管理，支持按审核状态、接收请求状态和关键词筛选，并支持管理员通过开关开放或关闭教师接收请求权限；只有审核通过的教师才能被开启。

/admin/review-requests 用于全局查看审核请求，支持状态、分配方式和关键词筛选，并可查看请求详情。

/admin/review-records 用于全局查看审核记录，包括教师端已经隐藏的记录，并支持 CSV 导出。

/admin/settings 用于管理 platform_setting，支持新增设置、编辑设置值和说明。

/admin/action-logs 用于查看最近的管理员操作日志，支持关键词搜索。

新增管理员接口：
text



GET  /api/admin/dashboard
GET  /api/admin/manage/users
POST /api/admin/manage/users/{id}/enabled
POST /api/admin/manage/users/{id}/password
GET  /api/admin/manage/teachers
POST /api/admin/manage/teachers/{id}/available
GET  /api/admin/manage/resumes
GET  /api/admin/manage/review-requests
GET  /api/admin/manage/review-records
GET  /api/admin/manage/settings
POST /api/admin/manage/settings
POST /api/admin/manage/settings/{id}
GET  /api/admin/manage/action-logs

管理员后台第二轮已完成：
text



admin_action_log
platform_setting
禁用用户
重置密码

新增数据库迁移脚本：
text



database/08_admin_controls.sql

user 表新增 enabled 字段。被禁用账号不能登录，已有 token 调用后端接口时也会被拦截。

已纳入管理员操作日志的动作包括：启用/禁用用户、重置密码、教师接收请求权限开关、平台设置新增/更新、教师资料审核通过/拒绝、撤回请求同意/拒绝。

本轮仍未实现强制修改审核请求状态、付费交易和评价系统。
