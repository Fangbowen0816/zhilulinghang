import request from "../utils/request"

export function getStudentReviewRecordsApi() {
  return request({
    url: "/review-records/student",
    method: "get"
  })
}

export function getTeacherReviewRecordsApi() {
  return request({
    url: "/review-records/teacher",
    method: "get"
  })
}

export function hideReviewRecordApi(id) {
  return request({
    url: `/review-records/${id}/hide`,
    method: "post"
  })
}
