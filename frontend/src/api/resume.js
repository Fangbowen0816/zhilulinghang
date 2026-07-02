import request from "../utils/request"

export function getMyResumeApi() {
  return request({
    url: "/resume/me",
    method: "get"
  })
}

export function getMyResumesApi() {
  return request({
    url: "/resume/my",
    method: "get"
  })
}

export function getResumeByIdApi(id) {
  return request({
    url: `/resume/${id}`,
    method: "get"
  })
}

export function getResumeVersionsApi(id) {
  return request({
    url: `/resume/${id}/versions`,
    method: "get"
  })
}

export function getResumePreviewApi(id) {
  return request({
    url: `/resume/${id}/preview`,
    method: "get"
  })
}

export function getResumeExportApi(id) {
  return request({
    url: `/resume/${id}/export`,
    method: "get"
  })
}

export function getResumeTemplatesApi() {
  return request({
    url: "/resume-templates",
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

export function polishResumeApi(data) {
  return request({
    url: "/resume/polish",
    method: "post",
    data,
    timeout: 60000
  })
}

export function deleteResumeApi(id) {
  return request({
    url: `/resume/${id}`,
    method: "delete"
  })
}

export function getResumeFeedbackApi() {
  return request({
    url: "/resume/feedback",
    method: "get"
  })
}
