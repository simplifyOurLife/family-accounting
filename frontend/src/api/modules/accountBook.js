import request from '../request'

export default {
  // 获取账本列表
  getList () {
    return request.get('/account-book')
  },

  // 创建账本
  create (data) {
    return request.post('/account-book', data)
  },

  // 更新账本
  update (id, data) {
    return request.put(`/account-book/${id}`, data)
  },

  // 删除账本
  delete (id) {
    return request.delete(`/account-book/${id}`)
  },

  // 设为默认账本
  setDefault (id) {
    return request.put(`/account-book/${id}/default`)
  }
}
