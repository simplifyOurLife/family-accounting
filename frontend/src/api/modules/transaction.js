import request from '../request'

export default {
  // 获取交易列表
  getList (params) {
    return request.get('/transaction', { params })
  },

  // 创建交易记录
  create (data) {
    return request.post('/transaction', data)
  },

  // 更新交易记录
  update (id, data) {
    return request.put(`/transaction/${id}`, data)
  },

  // 删除交易记录
  delete (id) {
    return request.delete(`/transaction/${id}`)
  }
}
