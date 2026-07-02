import request from "../utils/request"

export function getAdminDashboardApi() {
  return request({
    url: "/admin/dashboard",
    method: "get"
  })
}

export function getAdminUsersApi() {
  return request({
    url: "/admin/manage/users",
    method: "get"
  })
}

export function setAdminUserEnabledApi(id, enabled) {
  return request({
    url: `/admin/manage/users/${id}/enabled`,
    method: "post",
    data: { enabled }
  })
}

export function resetAdminUserPasswordApi(id, newPassword) {
  return request({
    url: `/admin/manage/users/${id}/password`,
    method: "post",
    data: { newPassword }
  })
}

export function getAdminTeachersApi() {
  return request({
    url: "/admin/manage/teachers",
    method: "get"
  })
}

export function closeTeacherAvailableApi(id) {
  return request({
    url: `/admin/manage/teachers/${id}/close`,
    method: "post"
  })
}

export function setTeacherAvailableApi(id, available) {
  return request({
    url: `/admin/manage/teachers/${id}/available`,
    method: "post",
    data: { available }
  })
}

export function getAdminResumesApi() {
  return request({
    url: "/admin/manage/resumes",
    method: "get"
  })
}

export function getAdminReviewRequestsApi() {
  return request({
    url: "/admin/manage/review-requests",
    method: "get"
  })
}

export function getAdminReviewRecordsApi() {
  return request({
    url: "/admin/manage/review-records",
    method: "get"
  })
}

export function getAdminResumeAnnotationsApi() {
  return request({
    url: "/admin/manage/resume-annotations",
    method: "get"
  })
}

export function getAdminResumeScoresApi() {
  return request({
    url: "/admin/manage/resume-scores",
    method: "get"
  })
}

export function getAdminSettingsApi() {
  return request({
    url: "/admin/manage/settings",
    method: "get"
  })
}

export function createAdminSettingApi(data) {
  return request({
    url: "/admin/manage/settings",
    method: "post",
    data
  })
}

export function updateAdminSettingApi(id, data) {
  return request({
    url: `/admin/manage/settings/${id}`,
    method: "post",
    data
  })
}

export function getAdminActionLogsApi() {
  return request({
    url: "/admin/manage/action-logs",
    method: "get"
  })
}
