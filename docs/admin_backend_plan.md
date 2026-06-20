# 管理员后台建设计划

## 目标

管理员后台定位为平台运营与风控中心，而不是普通的数据表查看器。

管理员需要管理：

- 用户与账号状态
- 教师资料审核与教师管理
- 简历与简历版本监管
- 审核请求全局监管
- 学生撤回申请处理
- 审核记录留存与追踪
- 管理员操作审计
- 平台策略配置

本计划是后续实现前的设计文档。确认前不开始改代码。

## 当前已有能力

当前系统已经具备：

- 管理员登录
- 管理员首页基础页
- 用户管理基础页
- 简历管理基础页
- 教师资料审核页 `/admin/teacher-approvals`
- 撤回申请处理页 `/admin/withdraw-requests`
- 教师审核请求和审核记录相关后端基础数据

当前不足：

- 管理员首页没有运营待办和统计
- 用户管理能力较弱
- 缺少全量教师管理
- 缺少全量审核请求管理
- 缺少全量审核记录管理
- 缺少简历版本链查看
- 缺少管理员操作日志
- 缺少平台配置能力

## 管理端页面规划

建议最终形成如下路由结构：

```text
/admin/home
/admin/users
/admin/teachers
/admin/teacher-approvals
/admin/resumes
/admin/review-requests
/admin/withdraw-requests
/admin/review-records
/admin/action-logs
/admin/settings
```

## 页面设计

### 1. 管理员工作台

路由：

```text
/admin/home
```

目标：

把首页变成管理员待办和运营概览，而不是空页面。

展示指标：

```text
待审核教师数量
待处理撤回申请数量
今日新增学生
今日新增教师
今日新增审核请求
今日完成审核数量
冻结简历数量
```

展示待办：

```text
待审核教师资料
待处理撤回申请
最近审核请求
最近审核记录
```

第一版可以只做统计卡片和跳转入口。

### 2. 用户管理

路由：

```text
/admin/users
```

目标：

管理平台账号。

第一版功能：

```text
查看用户列表
按角色筛选
按用户名搜索
查看注册时间
```

后续增强：

```text
禁用/启用账号
重置密码
查看用户详情
查看用户关联简历/请求/记录
```

建议后续给 `user` 表增加：

```text
status
last_login_time
```

### 3. 教师资料审核

路由：

```text
/admin/teacher-approvals
```

目标：

处理 `approval_status = PENDING` 的教师资料。

当前已有基础实现。

建议增强：

```text
只显示待审核教师
支持查看完整教师资料
通过
拒绝并填写原因
```

### 4. 教师管理

路由：

```text
/admin/teachers
```

目标：

管理所有教师，而不仅是待审核教师。

功能：

```text
查看全部教师资料
按 approval_status 筛选
按 available 筛选
按展示名称/院系/擅长方向搜索
查看教师详情
查看教师审核请求
查看教师审核记录
强制关闭教师接收请求
重新打回审核
```

后续建议给 `teacher_profile` 增加：

```text
admin_locked
lock_reason
```

### 5. 简历管理

路由：

```text
/admin/resumes
```

目标：

全局查看简历与简历版本链。

展示字段：

```text
简历 ID
学生 ID
简历名称
版本类型
源简历 ID
生成教师 ID
状态
是否冻结
更新时间
```

功能：

```text
按学生搜索
按简历名称搜索
按 status 筛选
按 version_type 筛选
查看简历详情
查看同源返回版本
查看相关审核请求
```

第一版建议只允许查看，不允许管理员直接编辑学生简历。

### 6. 审核请求管理

路由：

```text
/admin/review-requests
```

目标：

全局监管 `review_request` 流转。

展示字段：

```text
请求 ID
源简历
学生
教师
分配方式
状态
学生说明
拒绝理由
拒绝建议
撤回原因
管理员处理意见
创建时间
更新时间
```

筛选条件：

```text
状态
学生
教师
分配方式
时间范围
```

功能：

```text
查看请求详情
查看源简历
查看教师返回版本
跳转到撤回处理
```

不建议管理员在这里直接强行修改状态，第一版只做监管查看。

### 7. 撤回申请处理

路由：

```text
/admin/withdraw-requests
```

目标：

专门处理 `status = WITHDRAW_PENDING` 的请求。

当前已有基础实现。

建议增强：

```text
查看源简历详情
查看学生撤回原因
查看教师信息
同意撤回
拒绝撤回并填写原因
```

### 8. 审核记录管理

路由：

```text
/admin/review-records
```

目标：

管理员查看完整审核记录，包括教师端已经隐藏的记录。

展示字段：

```text
记录 ID
请求 ID
源简历
返回简历
学生
教师
动作
审核意见
教师端是否隐藏
创建时间
```

筛选条件：

```text
教师
学生
时间范围
是否教师端隐藏
```

功能：

```text
查看源简历
查看返回简历
查看关联请求
```

第一版不提供删除记录功能。

### 9. 管理员操作日志

路由：

```text
/admin/action-logs
```

目标：

记录管理员关键操作，提升平台可追踪性。

建议新增表：

```text
admin_action_log
```

字段：

```text
id
admin_id
action
target_type
target_id
detail
create_time
```

需要记录的操作：

```text
通过教师资料
拒绝教师资料
同意撤回申请
拒绝撤回申请
强制关闭教师接收请求
重新打回教师资料
禁用用户
启用用户
重置密码
```

第一版可以先记录教师审核和撤回处理。

### 10. 平台配置

路由：

```text
/admin/settings
```

目标：

集中管理平台规则。

建议新增表：

```text
platform_setting
```

可配置项：

```text
是否允许学生注册
是否允许教师注册
教师资料是否需要管理员审核
随机分配教师数量
单份简历最多同时发送教师数量
是否允许学生撤回已接受请求
```

第一版可以只预留页面，不急着实现动态配置。

## 推荐实施顺序

### 第一阶段：补齐监管页面

目标：

用最小成本让管理员能监管当前已经实现的审核体系。

实现：

```text
/admin/home 工作台统计
/admin/review-requests 全局审核请求管理
/admin/review-records 全局审核记录管理
/admin/teachers 全量教师管理
```

后端新增：

```text
GET /api/admin/dashboard
GET /api/admin/review-requests
GET /api/admin/review-records
GET /api/admin/teachers
```

### 第二阶段：增强现有管理页面

实现：

```text
完善 /admin/users
完善 /admin/resumes
完善 /admin/teacher-approvals
完善 /admin/withdraw-requests
```

重点：

```text
搜索
筛选
详情查看
状态标签
关联跳转
```

### 第三阶段：管理员操作日志

实现：

```text
admin_action_log 表
/admin/action-logs 页面
教师资料审核写日志
撤回处理写日志
```

### 第四阶段：平台配置

实现：

```text
platform_setting 表
/admin/settings 页面
将部分硬编码规则改为配置读取
```

## 第一轮建议实现范围

建议先实现：

```text
1. /admin/home 工作台统计
2. /admin/teachers 教师管理
3. /admin/review-requests 审核请求管理
4. /admin/review-records 审核记录管理
```

暂不实现：

```text
管理员操作日志
平台配置
禁用用户
重置密码
付费交易
评价系统
```

## 需要确认的问题

1. 第一轮是否只做查看和监管，不做强制改状态？
2. 教师管理页是否需要第一版就支持“强制关闭接收请求”？
3. 审核请求管理页是否允许管理员查看完整简历内容？
4. 审核记录管理页是否允许管理员导出记录？
5. 是否现在就引入 `admin_action_log`，还是等第二轮？

## 默认建议

已确认采用：

```text
第一轮只做查看和监管
教师管理页第一版支持管理员开放或关闭接收请求权限
管理员可以查看完整简历内容
审核记录允许导出
admin_action_log 放到第三阶段
```

## 已确认决策

1. 第一轮只做查看和监管，不做强制改审核请求状态。
2. 教师管理页第一版需要支持管理员开放或关闭接收请求权限。
3. 审核请求管理页允许管理员查看完整简历内容。
4. 审核记录管理页允许管理员导出记录。
5. `admin_action_log` 第二轮之后再引入，不在第一轮实现。

## 第一轮完成情况

已完成：

```text
/admin/home 工作台统计
/admin/users 用户列表、角色筛选、用户名搜索
/admin/resumes 简历列表、状态筛选、版本筛选、关键词搜索
/admin/teacher-approvals 教师资料审核，只显示待审核教师，审核完成后移出列表
/admin/teachers 教师管理、审核状态筛选、接收请求筛选、接收请求权限开关
/admin/review-requests 审核请求全局监管、状态筛选、分配方式筛选、详情查看
/admin/review-records 审核记录全局监管、教师端隐藏筛选、CSV 导出
```

新增后端接口：

```text
GET  /api/admin/dashboard
GET  /api/admin/manage/users
GET  /api/admin/manage/teachers
POST /api/admin/manage/teachers/{id}/available
GET  /api/admin/manage/resumes
GET  /api/admin/manage/review-requests
GET  /api/admin/manage/review-records
```

第一轮未实现、第二轮已处理：

```text
admin_action_log
platform_setting
禁用用户
重置密码
```

## 第二轮完成情况

已完成：

```text
/admin/users 用户启用/禁用、管理员重置密码
/admin/settings 平台设置列表、新增设置、编辑设置值与说明
/admin/action-logs 管理员操作日志查看、关键词搜索
```

新增数据库结构：

```text
user.enabled
platform_setting
admin_action_log
```

已有数据库可执行迁移脚本：

```text
database/08_admin_controls.sql
```

新增后端接口：

```text
POST /api/admin/manage/users/{id}/enabled
POST /api/admin/manage/users/{id}/password
GET  /api/admin/manage/settings
POST /api/admin/manage/settings
POST /api/admin/manage/settings/{id}
GET  /api/admin/manage/action-logs
```

已纳入操作日志的动作：

```text
ENABLE_USER
DISABLE_USER
RESET_PASSWORD
SET_TEACHER_AVAILABLE
CREATE_SETTING
UPDATE_SETTING
APPROVE_TEACHER_PROFILE
REJECT_TEACHER_PROFILE
APPROVE_WITHDRAW_REQUEST
REJECT_WITHDRAW_REQUEST
```

第二轮仍未实现：

```text
强制修改审核请求状态
付费交易
评价系统
```
