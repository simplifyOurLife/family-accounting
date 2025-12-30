import Vue from 'vue'
import VueRouter from 'vue-router'
import store from '@/store'

Vue.use(VueRouter)

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/views/layout/Layout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: '/transaction'
      },
      {
        path: '/transaction',
        name: 'Transaction',
        component: () => import('@/views/transaction/Transaction.vue'),
        meta: { title: '记账' }
      },
      {
        path: '/statistics',
        name: 'Statistics',
        component: () => import('@/views/statistics/Statistics.vue'),
        meta: { title: '统计' }
      },
      {
        path: '/profile',
        name: 'Profile',
        component: () => import('@/views/profile/Profile.vue'),
        meta: { title: '我的' }
      },
      {
        path: '/profile/edit',
        name: 'ProfileEdit',
        component: () => import('@/views/profile/ProfileEdit.vue'),
        meta: { title: '编辑资料' }
      },
      {
        path: '/profile/family',
        name: 'FamilyManage',
        component: () => import('@/views/profile/FamilyManage.vue'),
        meta: { title: '家庭管理' }
      },
      {
        path: '/profile/invitations',
        name: 'Invitations',
        component: () => import('@/views/profile/Invitations.vue'),
        meta: { title: '邀请处理' }
      },
      {
        path: '/profile/categories',
        name: 'CategoryManage',
        component: () => import('@/views/profile/CategoryManage.vue'),
        meta: { title: '分类管理' }
      },
      {
        path: '/profile/account-books',
        name: 'AccountBookManage',
        component: () => import('@/views/profile/AccountBookManage.vue'),
        meta: { title: '账本管理' }
      },
      {
        path: '/profile/password',
        name: 'ChangePassword',
        component: () => import('@/views/profile/ChangePassword.vue'),
        meta: { title: '修改密码' }
      }
    ]
  },
  {
    path: '*',
    redirect: '/'
  }
]

const router = new VueRouter({
  mode: 'hash',
  base: process.env.BASE_URL,
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = store.state.user.token
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth !== false)

  if (requiresAuth && !token) {
    next('/login')
  } else if (!requiresAuth && token && (to.path === '/login' || to.path === '/register')) {
    next('/')
  } else {
    next()
  }
})

export default router
