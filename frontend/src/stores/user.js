import { defineStore } from "pinia"

export const useUserStore = defineStore("user", {
  state: () => ({
    token: localStorage.getItem("token") || "",
    username: localStorage.getItem("username") || "",
    role: localStorage.getItem("role") || ""
  }),

  actions: {
    login(username, token, role) {
      this.username = username
      this.token = token
      this.role = role

      localStorage.setItem("username", username)
      localStorage.setItem("token", token)
      localStorage.setItem("role", role)
    },

    logout() {
      this.username = ""
      this.token = ""
      this.role = ""

      localStorage.removeItem("username")
      localStorage.removeItem("token")
      localStorage.removeItem("role")
    }
  }
})
