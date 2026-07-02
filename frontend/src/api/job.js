import request from "../utils/request"

export function getJobsApi(params) {
  return request({
    url: "/jobs",
    method: "get",
    params
  })
}

export function getJobByIdApi(id) {
  return request({
    url: `/jobs/${id}`,
    method: "get"
  })
}

export function applyJobApi(id, resumeId) {
  return request({
    url: `/jobs/${id}/apply`,
    method: "post",
    data: { resumeId }
  })
}
