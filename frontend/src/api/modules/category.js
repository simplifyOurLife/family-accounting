import request from '../request'

export default {
  // 获取分类树
  getTree (type) {
    return request.get('/category', { params: { type } })
  },

  // 创建分类
  create (data) {
    return request.post('/category', data)
  },

  // 更新分类
  update (id, data) {
    return request.put(`/category/${id}`, data)
  },

  // 删除分类
  delete (id) {
    return request.delete(`/category/${id}`)
  }
}
