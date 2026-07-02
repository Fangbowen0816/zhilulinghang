import request from "../utils/request"

export function getMyApplicationsApi() {
  return request({
    url: "/applications/student",
    method: "get"
  })
}

export function getApplicationByIdApi(id) {
  return request({
    url: `/applications/${id}`,
    method: "get"
  })
}

export function updateApplicationStatusApi(id, status) {
  return request({
    url: `/applications/${id}/status`,
    method: "post",
    data: { status }
  })
}

export function getApplicationExperiencesApi(id) {
  return request({
    url: `/applications/${id}/experiences`,
    method: "get"
  })
}

export function createApplicationExperienceApi(id, data) {
  return request({
    url: `/applications/${id}/experiences`,
    method: "post",
    data
  })
}

export function updateApplicationExperienceApi(id, data) {
  return request({
    url: `/applications/experiences/${id}`,
    method: "put",
    data
  })
}

export function deleteApplicationExperienceApi(id) {
  return request({
    url: `/applications/experiences/${id}`,
    method: "delete"
  })
}

export function getApplicationRemindersApi(id) {
  return request({
    url: `/applications/${id}/reminders`,
    method: "get"
  })
}

export function createApplicationReminderApi(id, data) {
  return request({
    url: `/applications/${id}/reminders`,
    method: "post",
    data
  })
}

export function markApplicationReminderDoneApi(id) {
  return request({
    url: `/applications/reminders/${id}/done`,
    method: "post"
  })
}

export function deleteApplicationReminderApi(id) {
  return request({
    url: `/applications/reminders/${id}`,
    method: "delete"
  })
}

export function getPendingApplicationRemindersApi() {
  return request({
    url: "/applications/student/reminders/pending",
    method: "get"
  })
}
