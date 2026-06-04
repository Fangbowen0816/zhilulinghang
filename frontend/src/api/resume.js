import request from "../utils/request"

export function getMyResumeApi() {
  return request({
    url: "/resume/me",
    method: "get"
  })
}

export function createResumeApi(data) {
  return request({
    url: "/resume",
    method: "post",
    data
  })
}

export function updateResumeApi(id, data) {
  return request({
    url: `/resume/${id}`,
    method: "put",
    data
  })
}

export function submitResumeApi(id) {
  return request({
    url: `/resume/${id}/submit`,
    method: "post"
  })
}

export function getResumeFeedbackApi() {
  return request({
    url: "/resume/feedback",
    method: "get"
  })
}
