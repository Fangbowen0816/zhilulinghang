import request from "../utils/request"

export function createReviewRequestsApi(data) {
  return request({
    url: "/review-requests",
    method: "post",
    data
  })
}

export function getStudentReviewRequestsApi() {
  return request({
    url: "/review-requests/student",
    method: "get"
  })
}

export function getTeacherReviewRequestsApi() {
  return request({
    url: "/review-requests/teacher",
    method: "get"
  })
}

export function getReviewRequestDetailApi(id) {
  return request({
    url: `/review-requests/${id}`,
    method: "get"
  })
}

export function acceptReviewRequestApi(id, teacherReply) {
  return request({
    url: `/review-requests/${id}/accept`,
    method: "post",
    data: { teacherReply }
  })
}

export function declineReviewRequestApi(id, data) {
  return request({
    url: `/review-requests/${id}/decline`,
    method: "post",
    data
  })
}

export function returnReviewRequestApi(id, data) {
  return request({
    url: `/review-requests/${id}/return`,
    method: "post",
    data
  })
}

export function withdrawReviewRequestApi(id, reason) {
  return request({
    url: `/review-requests/${id}/withdraw`,
    method: "post",
    data: { reason }
  })
}

export function getReviewAnnotationsApi(id) {
  return request({
    url: `/review-requests/${id}/annotations`,
    method: "get"
  })
}

export function createReviewAnnotationApi(id, data) {
  return request({
    url: `/review-requests/${id}/annotations`,
    method: "post",
    data
  })
}

export function deleteReviewAnnotationApi(id) {
  return request({
    url: `/resume-annotations/${id}`,
    method: "delete"
  })
}

export function getReviewScoreApi(id) {
  return request({
    url: `/review-requests/${id}/score`,
    method: "get"
  })
}

export function saveReviewScoreApi(id, data) {
  return request({
    url: `/review-requests/${id}/score`,
    method: "post",
    data
  })
}

export function getAdminWithdrawRequestsApi() {
  return request({
    url: "/admin/withdraw-requests",
    method: "get"
  })
}

export function approveWithdrawRequestApi(id, adminComment) {
  return request({
    url: `/admin/withdraw-requests/${id}/approve`,
    method: "post",
    data: { adminComment }
  })
}

export function rejectWithdrawRequestApi(id, adminComment) {
  return request({
    url: `/admin/withdraw-requests/${id}/reject`,
    method: "post",
    data: { adminComment }
  })
}
