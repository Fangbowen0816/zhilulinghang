import request from "../utils/request"

export function getAvailableTeachersApi() {
  return request({
    url: "/teachers",
    method: "get"
  })
}

export function getMyTeacherProfileApi() {
  return request({
    url: "/teacher/profile",
    method: "get"
  })
}

export function updateMyTeacherProfileApi(data) {
  return request({
    url: "/teacher/profile",
    method: "put",
    data
  })
}

export function getAdminTeacherProfilesApi() {
  return request({
    url: "/admin/teacher-profiles",
    method: "get"
  })
}

export function getPendingTeacherProfilesApi() {
  return request({
    url: "/admin/teacher-profiles/pending",
    method: "get"
  })
}

export function approveTeacherProfileApi(id) {
  return request({
    url: `/admin/teacher-profiles/${id}/approve`,
    method: "post"
  })
}

export function rejectTeacherProfileApi(id, approvalComment) {
  return request({
    url: `/admin/teacher-profiles/${id}/reject`,
    method: "post",
    data: { approvalComment }
  })
}
