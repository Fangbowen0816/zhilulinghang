项目：职路启航 · 应届生求职赋能平台
路径：d:\files\study\SE\Project\zhilulinghang

技术栈：
- 后端：Spring Boot + MyBatis 注解 SQL + MySQL + JWT
- 前端：Vue 3 + Element Plus + Vite
- 当前主要角色：STUDENT、TEACHER、ADMIN
- 企业端服务尚未实现，只完成了职位库/投递等前置能力

重要文档：
- docs/current.md：当前实现状态汇总
- docs/next_development_plan.md：下一阶段开发计划
- docs/teacher_review_plan.md：教师审核体系计划
- docs/admin_backend_plan.md：管理员后台计划
- docs/需求规格说明.md：原始需求规格说明

当前已完成能力：

1. 用户与权限
- 学生、教师、管理员登录/注册
- JWT 鉴权
- AuthContext/AuthUser 支持当前用户上下文
- 管理员可禁用用户、重置密码
- 教师注册后默认不可接收请求，需要完善资料并由管理员审核后开启

2. 学生简历
- 多简历实体管理
- 每份简历有独立名称，便于学生区分
- 学生首页可显示简历列表并进入具体简历页面
- 简历字段已扩展，包括姓名、联系方式、目标岗位、教育经历、项目/实习经历、技能、获奖、自我评价等
- 支持 AI 简历润色，调用 DeepSeek 配置；如果配置缺失会提示 AI 服务不可用
- 支持简历模板、预览与导出
- 已新增 resume_template 表与 resume.template_id 字段
- 当前导出是后端生成 HTML，前端打开打印/另存为 PDF，不是真正服务端 PDF

3. 教师审核体系
- 学生可把简历提交给教师审核
- 支持指定教师或系统随机分配，第一版随机只分配 1 个教师
- 不允许重复发送给同一教师
- 允许同时发送给多个不同教师，但每个教师收到的是独立复制
- 教师可接受/拒绝审核请求
- 教师拒绝请求时需要填写理由及建议
- 教师接受后，学生可申请撤回，需要填写原因
- 撤回后简历审核流程冻结，等待管理员处理
- 教师返回的新简历不会覆盖原简历，而是创建新的 resume 实体
- 教师返回的新简历允许学生继续编辑
- 教师端可隐藏自己的审核记录，但不影响管理员视图和服务器数据

4. 管理员后台
- 管理员后台已具备监管能力
- 支持教师资料审核
- 教师审核页面列表只显示待审核教师，审核完成后移除
- 教师管理页面支持随时开放/关闭教师接收请求权限，使用开关展示
- 支持 admin_action_log
- 支持 platform_setting
- 支持禁用用户
- 支持重置密码
- 管理员可以处理学生撤回审核请求
- 管理员当前定位是“查看和监管”，不做深度业务运营

5. 职位与投递
- 已完成阶段一：岗位库与投递闭环
- 数据表包括 job、job_application
- 学生可浏览岗位、筛选岗位、查看岗位详情
- 学生可选择自己的简历投递岗位
- 防止重复投递同一岗位
- 学生可查看投递记录
- 学生可手动更新投递状态
- 前端页面包括：
  - /student/jobs
  - /student/jobs/:id
  - /student/applications

6. 求职进度、提醒与经验记录
- 已完成阶段二
- 数据表包括 application_experience、application_reminder
- 学生可围绕投递记录添加经验记录
- 学生可设置提醒
- 可标记提醒完成或删除提醒
- 学生首页/投递页可显示待提醒事项
- 第一版不做后台定时推送，只在学生端展示待提醒内容

7. 简历模板、预览与导出
- 已完成阶段三
- 数据表包括 resume_template
- 新增 migration：database/11_resume_template.sql
- 后端接口：
  - GET /api/resume-templates
  - GET /api/resume-templates/{id}
  - GET /api/resume/{id}/preview
  - GET /api/resume/{id}/export
- 前端增强：
  - ResumeEditView.vue 支持模板选择、预览、导出/打印
  - ResumeVersionsView.vue 支持教师返回版本预览、导出、版本类型展示
- 教师返回版本继承源简历模板

数据库迁移脚本现状：
- database/01_schema.sql：总 schema
- database/02_mock_data.sql：模拟数据
- database/09_job_and_application.sql：岗位与投递
- database/10_application_progress.sql：投递进度、提醒、经验记录
- database/11_resume_template.sql：简历模板

已验证过：
- 后端：.\mvnw.cmd clean test 通过
- 前端：npm run build 通过
- 前端构建存在非阻塞 warning：
  - @vueuse/core pure annotation warning
  - chunk size > 500k warning
- 本地服务曾运行：
  - 后端：http://localhost:8080
  - 前端：http://127.0.0.1:5173/

当前下一阶段计划：
- docs/next_development_plan.md 中前三阶段已完成
- 下一步应执行阶段四：教师批注与评分
- 阶段四计划内容：
  - 新增 resume_annotation 表
  - 新增 resume_score 表
  - 新增 migration：database/12_resume_annotation_score.sql
  - 后端新增模型、Mapper、Controller
  - 接口包括：
    - GET /api/review-requests/{id}/annotations
    - POST /api/review-requests/{id}/annotations
    - DELETE /api/resume-annotations/{id}
    - GET /api/review-requests/{id}/score
    - POST /api/review-requests/{id}/score
  - 教师只能给自己已接受的审核请求添加批注和评分
  - 批注必须绑定具体简历字段
  - 分数范围 0-100
  - 学生可查看自己相关请求的批注和评分
  - 管理员可监管所有批注和评分

企业端状态：
- 企业端服务尚未实现
- 当前只有岗位库和投递记录，没有企业账号、企业资料、企业发布岗位、企业查看投递、企业处理申请等能力
- 用户曾要求“先做企业端计划，不执行，然后继续现有下一阶段”
- 该请求尚未真正落地，因为会话被中断
- 建议新会话先创建 docs/company_service_plan.md 或类似计划文件，再继续阶段四教师批注与评分