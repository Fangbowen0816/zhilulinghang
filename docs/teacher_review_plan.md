# 教师审核体系改造计划

## 目标

把教师从“能看到所有待审核简历的审核员”升级为“可被学生选择、可接受或拒绝审核请求、能返回独立简历版本、拥有个人介绍和审核记录的服务提供者”。

本计划必须按阶段推进，不跳步实现交易市场、评价系统或复杂推荐。

## 已确认决策

1. 学生提交审核时可以选择指定教师，也可以选择系统随机分配。
2. 随机分配第一版只分配 1 个教师。
3. 不允许同一份源简历向同一个教师重复发送未结束请求。
4. 教师拒绝请求时必须填写拒绝理由和建议。
5. 教师接受请求后，学生可以申请撤回，需填写原因；请求进入管理员处理状态，相关简历暂时冻结。
6. 允许一份简历同时发送给多个教师。
7. 发送给每个教师时都基于同一份源简历建立独立审核请求。
8. 教师返回的新简历不能覆盖原简历，必须作为新的 `resume` 实体。
9. 教师返回的新简历允许学生继续编辑。
10. 教师资料由教师自己编辑。
11. 教师注册后默认不可接收请求，需完善资料并通过管理员审核后才能开启接收请求。
12. 教师端可以删除审核记录，但只是隐藏教师端视图，不改变管理员视图和服务器数据。

## 分阶段实现

### 第一阶段：账号注册与教师资料

本阶段只做账号注册、教师资料、管理员审核教师资料。

后端：

- 新增 `POST /api/auth/register`
- 只允许公开注册 `STUDENT` 和 `TEACHER`
- 教师注册后自动创建 `teacher_profile`
- 教师资料默认：
  - `available = false`
  - `approval_status = PENDING`
- 新增教师资料查询/编辑接口
- 新增管理员审核教师资料接口

前端：

- 新增注册页 `/register`
- 登录页增加注册链接
- 教师端新增个人资料页
- 管理员端新增教师资料审核页

### 第二阶段：审核请求

本阶段做学生提交审核请求，不做教师返回新简历。

- 新增 `review_request`
- 学生可以指定一个或多个教师
- 学生可以选择系统随机分配 1 个可接收请求的教师
- 防止同一源简历重复发送给同一教师
- 教师可以接受或拒绝请求
- 拒绝必须填写理由和建议

### 第三阶段：教师返回新简历

- 教师基于源简历填写修改后的内容
- 完成审核时创建新的 `resume` 实体
- 新简历记录：
  - `source_resume_id`
  - `generated_by_teacher_id`
  - `version_type = TEACHER_RETURNED`
- 原简历不被覆盖
- 学生可以继续编辑教师返回版本
- 生成 `review_record`

### 第四阶段：撤回与记录管理

- 教师接受请求后，学生可以申请撤回
- 撤回需填写原因
- 请求进入 `WITHDRAW_PENDING`
- 简历冻结
- 管理员同意或拒绝撤回
- 教师端可隐藏审核记录
- 管理员保留完整记录

### 后续扩展：市场化能力

暂不实现，只预留可扩展性。

- 服务套餐
- 付费订单
- 加急审核
- 多轮修改
- 学生评价
- 退款申诉

## 第一阶段数据设计

### teacher_profile

```text
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
```

`approval_status`：

```text
PENDING
APPROVED
REJECTED
```

规则：

- 教师注册后 `approval_status = PENDING`
- 教师注册后 `available = false`
- 只有 `approval_status = APPROVED` 时教师才能开启 `available = true`
- 管理员拒绝时必须填写 `approval_comment`

## 第一阶段接口

认证：

```text
POST /api/auth/register
```

教师资料：

```text
GET /api/teachers
GET /api/teachers/{id}
GET /api/teacher/profile
PUT /api/teacher/profile
```

管理员教师审核：

```text
GET /api/admin/teacher-profiles
GET /api/admin/teacher-profiles/pending
POST /api/admin/teacher-profiles/{id}/approve
POST /api/admin/teacher-profiles/{id}/reject
```

## 当前执行状态

- [x] 第一阶段：账号注册与教师资料
- [x] 第二阶段：审核请求
- [x] 第三阶段：教师返回新简历
- [x] 第四阶段：撤回与记录管理

## 第一阶段完成情况

已完成：

- `POST /api/auth/register`
- `teacher_profile` 表
- 教师注册后自动创建待审核资料
- 教师资料编辑页 `/teacher/profile`
- 管理员教师资料审核页 `/admin/teacher-approvals`
- 管理员通过/拒绝教师资料
- 学生和管理员可查询已审核且开启接收请求的教师列表接口

未进入下一阶段的内容：

- 未实现撤回申请和审核记录隐藏

## 第二阶段完成情况

已完成：

- `review_request` 表
- `POST /api/review-requests`
- `GET /api/review-requests/student`
- `GET /api/review-requests/teacher`
- `GET /api/review-requests/{id}`
- `POST /api/review-requests/{id}/accept`
- `POST /api/review-requests/{id}/decline`
- 学生可在简历编辑页选择指定教师或随机分配
- 指定教师支持一次选择多位教师
- 随机分配第一版只分配 1 位已审核且开启接收请求的教师
- 防止同一源简历重复发送给同一教师
- 教师可查看自己的审核请求
- 教师可接受或拒绝 PENDING 请求
- 教师拒绝请求时必须填写理由和建议
- 学生可查看自己的审核请求状态

未进入下一阶段的内容：

- 未实现学生撤回申请和管理员处理
- 未实现教师端隐藏审核记录

## 第三阶段完成情况

已完成：

- `resume` 新增版本来源字段：
  - `source_resume_id`
  - `generated_by_teacher_id`
  - `version_type`
- 新增 `review_record` 表
- `POST /api/review-requests/{id}/return`
- `GET /api/review-records/student`
- `GET /api/review-records/teacher`
- `GET /api/resume/{id}/versions`
- 教师可对 `ACCEPTED` 请求返回新简历
- 返回新简历时创建新的 `resume` 实体
- 返回的新简历：
  - `source_resume_id = 源简历 ID`
  - `generated_by_teacher_id = 教师 ID`
  - `version_type = TEACHER_RETURNED`
  - `status = DRAFT`
- 原简历不被覆盖
- 审核请求状态变为 `COMPLETED`
- 生成 `review_record`
- 学生可查看教师返回版本并继续编辑
- 学生可在审核请求页查看审核记录

未进入下一阶段的内容：

- 未实现市场化能力
- 未实现评价系统
- 未实现付费订单

## 第四阶段完成情况

已完成：

- `review_request` 新增撤回和管理员处理字段：
  - `withdraw_reason`
  - `admin_decision`
  - `admin_comment`
  - `admin_id`
  - `admin_time`
- `resume` 新增冻结字段：
  - `frozen`
  - `freeze_reason`
- `POST /api/review-requests/{id}/withdraw`
- `GET /api/admin/withdraw-requests`
- `POST /api/admin/withdraw-requests/{id}/approve`
- `POST /api/admin/withdraw-requests/{id}/reject`
- `POST /api/review-records/{id}/hide`
- 学生可对 `ACCEPTED` 请求申请撤回
- 撤回申请必须填写原因
- 撤回申请后请求状态变为 `WITHDRAW_PENDING`
- 撤回处理中源简历被冻结，不能编辑、删除、提交或再次发起审核请求
- 管理员同意撤回后请求状态变为 `WITHDRAWN`，源简历解冻
- 管理员拒绝撤回后请求恢复 `ACCEPTED`，源简历解冻
- 教师可在教师端隐藏自己的审核记录
- 教师隐藏记录不删除服务器数据，也不影响管理员后续查看能力

未进入后续扩展的内容：

- 未实现市场化能力
- 未实现评价系统
- 未实现付费订单
