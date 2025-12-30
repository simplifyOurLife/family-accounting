import axios from 'axios'
import store from '@/store'
import router from '@/router'
import { Toast } from 'vant'

// 创建 axios 实例
const service = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    const token = store.state.user.token
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      Toast.fail(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  error => {
    console.error('Response error:', error)
    if (error.response) {
      const { status, data } = error.response
      if (status === 401) {
        Toast.fail('登录已过期，请重新登录')
        store.dispatch('user/logout')
        router.push('/login')
      } else if (status === 403) {
        Toast.fail('没有权限执行此操作')
      } else {
        Toast.fail(data?.message || '服务器错误')
      }
    } else {
      Toast.fail('网络连接失败')
    }
    return Promise.reject(error)
  }
)

export default service
