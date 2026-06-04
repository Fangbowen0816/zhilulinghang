import { createRouter, createWebHistory } from "vue-router"

function getHomePathByRole(role) {
  if (role === "admin") return "/admin/home"
  if (role === "student") return "/student/home"
  if (role === "teacher") return "/teacher/home"
  return "/login"
}

const routes = [
  {
    path: "/",
    redirect: () => {
      const token = localStorage.getItem("token")
      const role = localStorage.getItem("role")

      if (!token) return "/login"
      return getHomePathByRole(role)
    }
  },
  {
    path: "/login",
    component: () => import("../views/login/LoginView.vue")
  },
  {
    path: "/admin",
    component: () => import("../layout/AdminLayout.vue"),
    redirect: "/admin/home",
    children: [
      {
        path: "home",
        component: () => import("../views/admin/AdminHomeView.vue")
      },
      {
        path: "users",
        component: () => import("../views/admin/AdminUsersView.vue")
      },
      {
        path: "resumes",
        component: () => import("../views/admin/AdminResumeView.vue")
      }
    ]
  },
  {
    path: "/student",
    component: () => import("../layout/StudentLayout.vue"),
    redirect: "/student/home",
    children: [
      {
        path: "home",
        component: () => import("../views/student/StudentHomeView.vue")
      },
      {
        path: "resume/edit",
        component: () => import("../views/student/ResumeEditView.vue")
      },
      {
        path: "resume/feedback",
        component: () => import("../views/student/ResumeFeedbackView.vue")
      }
    ]
  },
  {
    path: "/teacher",
    component: () => import("../layout/TeacherLayout.vue"),
    redirect: "/teacher/home",
    children: [
      {
        path: "home",
        component: () => import("../views/teacher/TeacherHomeView.vue")
      },
      {
        path: "review",
        component: () => import("../views/teacher/ReviewListView.vue")
      },
      {
        path: "review/:id",
        component: () => import("../views/teacher/ReviewDetailView.vue")
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem("token")
  const role = localStorage.getItem("role")

  if (!token && to.path !== "/login") {
    next("/login")
    return
  }

  if (token && to.path === "/login") {
    next(getHomePathByRole(role))
    return
  }

  if (token) {
    const roleRoot = `/${role}`
    const isRolePage = to.path.startsWith("/admin") || to.path.startsWith("/student") || to.path.startsWith("/teacher")
    if (isRolePage && !to.path.startsWith(roleRoot)) {
      next(getHomePathByRole(role))
      return
    }
  }

  next()
})

export default router
