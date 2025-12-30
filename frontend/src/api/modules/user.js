import request from '../request'

export default {
  // 修改密码
  updatePassword (data) {
    return request.put('/user/password', data)
  },

  // 更新个人信息
  updateProfile (data) {
    return request.put('/user/profile', data)
  }
}
