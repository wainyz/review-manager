import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue')
  },
  {
    path: '/home',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/exam',
    name: 'Exam',
    component: () => import('../views/PaperConfigView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/paper/config',
    name: 'PaperConfig',
    component: () => import('../views/PaperConfigView.vue')
  },
  {
    path: '/paper/edit/:id?', // 添加可选参数id，方便后续编辑已有试卷
    name: 'PaperEdit',
    component: () => import('../views/PaperEditView.vue'),
    props: true
  },
  {
    path: '/chat/:friendId',
    name: 'Chat',
    component: () => import('../views/ChatView.vue'),
    props: true,
    meta: { requiresAuth: true }
  },
  {
    path: '/class/chat/:classId',
    name: 'ClassChat',
    component: () => import('../views/ClassChatView.vue'),
    props: true,
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory('/review/'),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  
  if (to.path === '/login' && token) {
    next('/home')
    return
  }
  
  if (to.meta.requiresAuth && !token) {
    next('/login')
    return
  }
  
  next()
})

export default router