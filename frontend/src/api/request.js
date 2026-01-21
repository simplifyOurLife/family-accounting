import axios from 'axios'
import store from '@/store'
import router from '@/router'
import { Toast, Dialog } from 'vant'

// 安全相关错误码
const SECURITY_ERROR_CODES = {
  CAPTCHA_ERROR: 1001,
  CAPTCHA_EXPIRED: 1002,
  ACCOUNT_LOCKED: 1003,
  IP_RATE_LIMIT: 1004,
  PHONE_ALREADY_REGISTERED: 1005,
  INVALID_CREDENTIALS: 1006
}

// 安全错误消息映射
const SECURITY_ERROR_MESSAGES = {
  [SECURITY_ERROR_CODES.CAPTCHA_ERROR]: '验证码错误，请重新输入',
  [SECURITY_ERROR_CODES.CAPTCHA_EXPIRED]: '验证码已过期，请重新获取',
  [SECURITY_ERROR_CODES.ACCOUNT_LOCKED]: '账户已被锁定，请30分钟后再试',
  [SECURITY_ERROR_CODES.IP_RATE_LIMIT]: '请求过于频繁，请稍后再试',
  [SECURITY_ERROR_CODES.PHONE_ALREADY_REGISTERED]: '该手机号已注册，请直接登录',
  [SECURITY_ERROR_CODES.INVALID_CREDENTIALS]: '手机号或密码错误'
}

// 创建 axios 实例
const service = axios.create({
  baseURL: '/family-accounting/api',
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

// 处理安全相关错误
function handleSecurityError (code, message) {
  const securityMessage = SECURITY_ERROR_MESSAGES[code]

  if (securityMessage) {
    // 对于账户锁定，显示详细的对话框提示
    if (code === SECURITY_ERROR_CODES.ACCOUNT_LOCKED) {
      Dialog.alert({
        title: '账户已锁定',
        message: '由于多次登录失败，您的账户已被临时锁定。请30分钟后再试，或联系客服解锁。',
        confirmButtonText: '我知道了'
      })
      return true
    }

    // 对于IP限制，显示详细的对话框提示
    if (code === SECURITY_ERROR_CODES.IP_RATE_LIMIT) {
      Dialog.alert({
        title: '访问受限',
        message: '您的请求过于频繁，系统已暂时限制访问。请稍后再试。',
        confirmButtonText: '我知道了'
      })
      return true
    }

    // 验证码错误和过期使用Toast提示
    if (code === SECURITY_ERROR_CODES.CAPTCHA_ERROR || code === SECURITY_ERROR_CODES.CAPTCHA_EXPIRED) {
      Toast.fail({
        message: securityMessage,
        duration: 2000
      })
      return true
    }

    // 其他安全错误使用普通Toast提示
    Toast.fail(securityMessage)
    return true
  }

  return false
}

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      // 先检查是否是安全相关错误
      if (!handleSecurityError(res.code, res.message)) {
        Toast.fail(res.message || '请求失败')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  error => {
    console.error('Response error:', error)
    if (error.response) {
      const { status, data } = error.response

      // 先检查是否是安全相关错误
      if (data && data.code && handleSecurityError(data.code, data.message)) {
        return Promise.reject(error)
      }

      if (status === 401) {
        Toast.fail('登录已过期，请重新登录')
        store.dispatch('user/logout')
        // 避免重复导航，只有当前路由不是登录页时才跳转
        if (router.currentRoute.path !== '/login') {
          router.replace('/login')
        }
      } else if (status === 403) {
        Toast.fail('没有权限执行此操作')
      } else if (status === 429) {
        // HTTP 429 Too Many Requests
        Dialog.alert({
          title: '请求受限',
          message: '您的请求过于频繁，请稍后再试。',
          confirmButtonText: '我知道了'
        })
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
