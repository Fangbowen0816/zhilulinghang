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
ResumeEditView.vue 负责：
加载自己的简历
填写姓名、教育经历、项目经历、技能
保存草稿
提交审核
显示当前状态和教师反馈
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
Resume：简历，包含学生 ID、简历内容、状态、教师意见、创建/更新时间
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
GET  /api/auth/me

登录逻辑是：
根据用户名查用户
比较密码
检查前端选择的角色是否匹配数据库角色
生成 token
返回 token/userId/username/role
ResumeController 提供学生简历接口：
text



GET  /api/resume/me
POST /api/resume
PUT  /api/resume/{id}
POST /api/resume/{id}/submit
GET  /api/resume/feedback

它保证学生只能操作自己的简历。
ReviewController 提供教师审核接口：
text



GET  /api/review/list
GET  /api/review/{id}
POST /api/review/{id}

教师只能修改审核状态和教师意见，不能改学生填写的简历正文。
数据库脚本在：
01_schema.sql
02_mock_data.sql
目前建了两张核心表：
text



user
resume

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