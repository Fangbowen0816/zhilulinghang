# 企业端服务计划

## 目标

本计划用于规划企业端服务，但当前阶段只保存设计方案，不实现企业端代码。

企业端的定位是把当前“本地岗位库 + 学生投递记录”扩展为真实的招聘服务闭环，使企业用户能够维护企业资料、发布职位、接收学生投递、查看简历并处理申请状态。

第一版企业端仍以本地数据库和可演示流程为主，不接入真实招聘平台、不做支付交易、不做复杂面试排期系统。

## 当前项目基础

已完成的企业端前置能力：

```text
job 表
job_application 表
学生岗位浏览
学生岗位筛选
学生岗位详情
学生选择简历投递岗位
学生投递记录
学生手动维护投递状态
求职经验记录
求职提醒
```

尚未完成的企业端能力：

```text
企业账号注册/登录
企业资料维护
企业认证审核
企业发布岗位
企业编辑/关闭岗位
企业查看投递列表
企业查看学生简历
企业处理投递状态
企业端工作台
企业端操作审计
```

## 设计原则

1. 企业端作为独立阶段开发，不打断当前 `docs/next_development_plan.md` 中的教师批注与评分阶段。
2. 第一版只实现招聘业务闭环，不实现支付、交易、合同、复杂市场机制。
3. 延续当前项目模式：Spring Boot Controller + MyBatis Mapper + Vue 页面 + Element Plus。
4. 企业只能访问本企业资料、本企业岗位、本企业收到的投递。
5. 学生投递后，企业处理状态应写入同一条 `job_application` 记录，避免形成两套进度系统。
6. 管理员保留监管入口，可审核企业、查看企业岗位、禁用企业用户或关闭违规岗位。

## 角色与权限

建议新增角色：

```text
COMPANY
```

企业用户权限：

```text
维护自己的企业资料
提交企业认证申请
查看自己的认证状态
发布、编辑、关闭自己的岗位
查看投递到本企业岗位的申请
查看申请对应的简历预览
更新申请状态
```

管理员权限：

```text
审核企业资料
强制启用/禁用企业账号
强制关闭企业岗位
查看企业操作记录
监管企业收到的投递处理情况
```

学生权限保持：

```text
浏览开放岗位
投递开放岗位
查看自己的投递状态
维护自己的经验与提醒
```

## 第一阶段：企业账号与企业资料

### 目标

实现企业用户注册、登录后的资料维护与管理员认证审核。

### 数据库设计

新增表：`company_profile`

```text
id
user_id
company_name
industry
city
address
contact_person
contact_phone
contact_email
description
license_no
logo_url
verify_status
verify_remark
available
create_time
update_time
```

字段说明：

```text
verify_status:
  PENDING   待审核
  APPROVED  已通过
  REJECTED  已拒绝

available:
  true      企业账号可正常使用企业端功能
  false     企业端功能关闭
```

`user.role` 新增：

```text
COMPANY
```

新增迁移脚本建议：

```text
database/14_company_profile.sql
```

### 后端接口

企业端：

```text
POST /api/auth/register/company
GET  /api/company/profile
POST /api/company/profile
POST /api/company/profile/submit-verify
```

管理员端：

```text
GET  /api/admin/companies
GET  /api/admin/companies/{id}
POST /api/admin/companies/{id}/approve
POST /api/admin/companies/{id}/reject
POST /api/admin/companies/{id}/available
```

### 前端页面

新增企业端布局：

```text
CompanyLayout.vue
```

新增页面：

```text
/company/home
/company/profile
/company/verify
```

管理员端增强：

```text
/admin/companies
```

### 业务规则

1. 企业注册后默认 `verify_status = PENDING`。
2. 企业未通过审核前不能发布岗位。
3. 管理员可以拒绝企业认证，并填写原因。
4. 企业被禁用或 `available = false` 后，不能发布岗位和处理投递。
5. 企业资料变更后如涉及核心认证字段，可重新进入待审核状态。

## 第二阶段：企业岗位管理

### 目标

让企业维护自己的岗位，将当前 mock 岗位库逐步升级为企业真实发布岗位。

### 数据库改动

建议扩展 `job` 表：

```text
company_user_id
company_profile_id
publish_status
work_type
education_requirement
experience_requirement
deadline
```

字段说明：

```text
publish_status:
  DRAFT      草稿
  OPEN       招聘中
  CLOSED     已关闭
  REMOVED    管理员下架
```

新增迁移脚本建议：

```text
database/15_company_job_management.sql
```

### 后端接口

企业端：

```text
GET  /api/company/jobs
GET  /api/company/jobs/{id}
POST /api/company/jobs
PUT  /api/company/jobs/{id}
POST /api/company/jobs/{id}/open
POST /api/company/jobs/{id}/close
```

管理员端：

```text
GET  /api/admin/company-jobs
POST /api/admin/company-jobs/{id}/remove
POST /api/admin/company-jobs/{id}/restore
```

学生端已有岗位接口需要调整：

```text
GET /api/jobs 只展示 OPEN 状态岗位
GET /api/jobs/{id} 对学生隐藏管理字段
```

### 前端页面

企业端新增：

```text
/company/jobs
/company/jobs/new
/company/jobs/:id/edit
```

页面能力：

```text
岗位列表
新建岗位
编辑岗位
发布岗位
关闭岗位
查看岗位投递数量
```

### 业务规则

1. 企业只能管理自己发布的岗位。
2. 未审核通过企业不能发布岗位。
3. 管理员下架的岗位不能被企业自行重新开放。
4. 岗位关闭后，学生端不再展示为可投递岗位。
5. 岗位已有投递时，允许编辑描述类字段，但关键字段变更需要保留更新时间。

## 第三阶段：企业处理投递

### 目标

让企业查看投递到本企业岗位的申请，并更新申请状态，形成企业反馈闭环。

### 数据库改动

建议扩展 `job_application`：

```text
company_user_id
company_remark
last_company_action_time
```

可选新增表：`job_application_status_log`

```text
id
application_id
operator_id
operator_role
from_status
to_status
remark
create_time
```

新增迁移脚本建议：

```text
database/16_company_application_review.sql
```

### 后端接口

企业端：

```text
GET  /api/company/applications
GET  /api/company/applications/{id}
GET  /api/company/applications/{id}/resume-preview
POST /api/company/applications/{id}/status
POST /api/company/applications/{id}/remark
```

学生端可复用已有投递记录接口，但状态来源需要兼容企业更新。

管理员端：

```text
GET /api/admin/company-applications
GET /api/admin/company-applications/{id}
```

### 前端页面

企业端新增：

```text
/company/applications
/company/applications/:id
```

页面能力：

```text
按岗位筛选投递
按申请状态筛选投递
查看学生投递简历
查看简历模板化预览
更新投递状态
填写企业备注
查看状态流转记录
```

### 状态流转建议

沿用当前 `job_application.status`：

```text
APPLIED       已投递
SCREENING     筛选中
WRITTEN_TEST  笔试
INTERVIEW     面试
OFFER         Offer
REJECTED      已拒绝
CLOSED        已结束
```

企业可执行的状态流转：

```text
APPLIED -> SCREENING
SCREENING -> WRITTEN_TEST
SCREENING -> INTERVIEW
WRITTEN_TEST -> INTERVIEW
INTERVIEW -> OFFER
任意进行中状态 -> REJECTED
任意进行中状态 -> CLOSED
```

### 业务规则

1. 企业只能查看投递到自己岗位的申请。
2. 企业只能查看申请中使用的那份简历，不允许查看学生其他简历。
3. 企业更新状态后，学生端投递记录同步显示。
4. 每次状态变更应记录状态日志。
5. 企业备注对学生是否可见需要单独字段控制；第一版建议默认仅企业和管理员可见。

## 第四阶段：企业端通知与审计

### 目标

把企业端纳入站内通知和管理员审计体系。

### 通知事件

企业端需要产生或接收通知：

```text
企业认证通过
企业认证拒绝
岗位被管理员下架
学生投递企业岗位
企业更新投递状态
```

依赖：

```text
docs/next_development_plan.md 阶段五：站内通知
```

### 审计事件

建议记录：

```text
企业注册
企业提交认证
管理员审核企业
企业发布岗位
企业编辑岗位
企业关闭岗位
管理员下架岗位
企业查看申请详情
企业更新申请状态
```

## 路由与导航建议

企业端主导航：

```text
首页
企业资料
岗位管理
收到的投递
通知
```

企业首页建议展示：

```text
企业认证状态
开放岗位数
待处理投递数
最近投递
最近状态变更
```

## 与现有模块的关系

### 与学生端岗位模块

企业发布的 `OPEN` 岗位进入学生端岗位列表。

学生投递后写入 `job_application`，企业端从同一张表读取。

### 与简历模块

企业查看的是学生投递时绑定的 `resume_id`。

建议企业端使用已有简历预览能力：

```text
GET /api/resume/{id}/preview
```

但需要新增企业权限校验：企业只有在存在对应投递记录时才能访问该简历预览。

### 与管理员模块

管理员负责企业认证、岗位监管、企业用户禁用、异常投递记录查看。

### 与通知模块

企业端最好在站内通知阶段之后再做完整通知接入。

如果企业端先于通知模块开发，则先保留通知服务调用点，后续补接。

## 推荐实施顺序

企业端建议在当前主线完成以下内容之后启动：

```text
1. 教师批注与评分
2. 站内通知
3. 密码加密与基础权限增强
4. 企业端账号与资料
5. 企业岗位管理
6. 企业投递处理
```

如课程展示时间不足，可采用企业端最小演示版：

```text
企业注册登录
企业资料审核
企业发布岗位
学生投递岗位
企业查看投递并更新状态
学生看到状态变化
```

## 验收标准

企业端完整第一版的验收标准：

1. 企业可以注册并登录。
2. 企业可以维护资料并提交认证。
3. 管理员可以审核企业认证。
4. 审核通过企业可以发布、编辑、关闭岗位。
5. 学生可以看到企业发布的开放岗位。
6. 学生可以投递企业岗位。
7. 企业可以查看收到的投递。
8. 企业只能查看投递到本企业岗位的简历。
9. 企业可以更新投递状态。
10. 学生端投递记录能看到企业更新后的状态。
11. 管理员可以监管企业、岗位和投递。
12. 后端测试与前端构建通过。

## 暂不实现内容

第一版不做：

```text
真实企业工商认证
真实招聘平台同步
企业付费发布岗位
学生与企业聊天
面试时间预约系统
在线视频面试
简历下载水印
复杂企业信用评分
多企业员工子账号
```

这些能力可以作为后续市场化扩展阶段再拆分。

## 对现有文档的影响

本计划不替代 `docs/next_development_plan.md`。

当前主线仍建议继续执行：

```text
阶段四：教师批注与评分
阶段五：站内通知
阶段六：安全与非功能增强
```

企业端服务应作为后续独立大阶段推进，避免影响当前已形成的学生、教师、管理员闭环。
