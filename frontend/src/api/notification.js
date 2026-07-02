import request from "../utils/request"

export function getNotificationsApi() {
  return request({
    url: "/notifications",
    method: "get"
  })
}

export function getUnreadNotificationCountApi() {
  return request({
    url: "/notifications/unread-count",
    method: "get"
  })
}

export function markNotificationReadApi(id) {
  return request({
    url: `/notifications/${id}/read`,
    method: "post"
  })
}

export function markAllNotificationsReadApi() {
  return request({
    url: "/notifications/read-all",
    method: "post"
  })
}
