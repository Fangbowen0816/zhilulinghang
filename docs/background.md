# 智路领航（职路启航）项目背景总述

## 项目简介

智路领航（职路启航）是一套面向高校应届毕业生的求职赋能平台，目标是帮助学生完成从简历制作、教师审核反馈到求职管理的完整闭环。

系统采用前后端分离架构：

* Frontend：Vue3 + Vite + Element Plus
* Backend：Spring Boot
* Database：MySQL
* Authentication：JWT
* Role-based Access Control（RBAC）

当前开发重点是实现一个课程项目可运行版本，优先保证核心业务闭环，而非复杂AI算法或企业级部署。

---

# 核心业务闭环

系统围绕以下流程展开：

```text
学生创建简历
    ↓
提交审核
    ↓
教师审核并给出建议
    ↓
学生查看反馈并修改
    ↓
形成最终简历
```

这是项目最核心、必须首先实现的业务流程。

---

# 用户角色

目前实现三个角色：

## 1. 管理员（admin）

职责：

* 用户管理
* 角色管理
* 系统配置

主要页面：

```text
/admin/home
```

---

## 2. 学生（student）

职责：

* 编辑简历
* 提交审核
* 查看教师反馈

主要页面：

```text
/student/home
/student/resume/edit
/student/resume/feedback
```

---

## 3. 教师（teacher）

职责：

* 查看待审核简历
* 给出审核意见
* 修改审核状态

主要页面：

```text
/teacher/home
/teacher/review
/teacher/review/:id
```

---

## 4. 企业（enterprise）

暂不实现。

仅预留数据库和权限设计。

---

# 当前已完成内容

前端基础框架已完成：

```text
Vue3
Vite
Vue Router
Pinia
Axios
Element Plus
```

已完成：

```text
登录页面
角色权限管理
AdminLayout
StudentLayout
TeacherLayout
路由守卫
localStorage 登录持久化
```

当前路由：

```text
/login

/admin/home

/student/home
/student/resume/edit
/student/resume/feedback

/teacher/home
/teacher/review
/teacher/review/:id
```

---

# 当前开发目标

优先实现最小可运行版本（MVP）。

开发顺序：

## 第一阶段

用户认证

功能：

* 登录
* JWT认证
* 获取当前用户信息

接口：

```text
POST /api/auth/login

GET /api/auth/me
```

---

## 第二阶段

学生简历模块

数据模型：

```text
Resume
```

字段：

```java
id
studentId
name
education
experience
skills
status
teacherComment
createTime
updateTime
```

功能：

```text
创建简历
修改简历
查询简历
提交审核
```

接口：

```text
GET    /api/resume/me
POST   /api/resume
PUT    /api/resume/{id}
POST   /api/resume/{id}/submit
```

---

## 第三阶段

教师审核模块

功能：

```text
查看待审核简历
查看简历详情
填写审核意见
审核通过/退回
```

接口：

```text
GET  /api/review/list
GET  /api/review/{id}
POST /api/review/{id}
```

审核结果：

```text
PENDING
APPROVED
REJECTED
```

---

## 第四阶段

学生反馈查看

功能：

```text
查看审核意见
根据意见修改简历
重新提交
```

接口：

```text
GET /api/resume/feedback
```

---

# 数据库最小设计

## User

```sql
id
username
password
role
create_time
```

role：

```text
ADMIN
STUDENT
TEACHER
```

---

## Resume

```sql
id
student_id
name
education
experience
skills
status
teacher_comment
create_time
update_time
```

status：

```text
DRAFT
SUBMITTED
APPROVED
REJECTED
```

---

# 权限规则

学生：

```text
只能操作自己的简历
```

教师：

```text
只能审核简历
不能修改学生内容
```

管理员：

```text
拥有全部权限
```

---

# 开发原则

1. 先实现业务闭环，再考虑AI功能。
2. 先使用Mock数据，不依赖真实招聘平台。
3. 所有接口遵循RESTful规范。
4. 使用JWT进行认证。
5. 使用RBAC进行权限控制。
6. 所有页面必须通过角色路由守卫访问。
7. 优先保证：

```text
学生制作简历
→ 教师审核
→ 学生查看反馈
```

流程完整可运行。

---

# 当前开发状态

已经完成：

```text
前端框架
登录页面
Layout结构
路由系统
权限守卫
```

下一步应开发：

```text
Spring Boot后端初始化
↓
MySQL数据库设计
↓
登录接口
↓
简历CRUD接口
↓
教师审核接口
↓
前后端联调
```

