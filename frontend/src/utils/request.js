import axios from "axios"
import { ElMessage } from "element-plus"


const request = axios.create({
    baseURL: "http://localhost:8080/api",
    timeout: 5000
})

request.interceptors.request.use(
  config => {
    const token = localStorage.getItem("token")
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

request.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    const message = error.response?.data?.message || "请求失败，请检查后端服务"
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default request
