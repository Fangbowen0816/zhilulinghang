import request from "../utils/request"

export function getReviewListApi() {
  return request({
    url: "/review/list",
    method: "get"
  })
}

export function getReviewDetailApi(id) {
  return request({
    url: `/review/${id}`,
    method: "get"
  })
}

export function submitReviewApi(id, data) {
  return request({
    url: `/review/${id}`,
    method: "post",
    data
  })
}
