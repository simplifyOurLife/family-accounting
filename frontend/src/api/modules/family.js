import request from '../request'

export default {
  // 创建家庭
  create (data) {
    return request.post('/family', data)
  },

  // 获取当前家庭信息
  getInfo () {
    return request.get('/family')
  },

  // 更新家庭信息
  update (data) {
    return request.put('/family', data)
  },

  // 邀请成员
  invite (data) {
    return request.post('/family/invite', data)
  },

  // 获取邀请列表
  getInvitations () {
    return request.get('/family/invitations')
  },

  // 接受邀请
  acceptInvitation (id) {
    return request.post(`/family/invitation/${id}/accept`)
  },

  // 拒绝邀请
  declineInvitation (id) {
    return request.post(`/family/invitation/${id}/decline`)
  },

  // 获取家庭成员列表
  getMembers () {
    return request.get('/family/members')
  },

  // 设置成员昵称
  setMemberNickname (id, data) {
    return request.put(`/family/member/${id}/nickname`, data)
  },

  // 移除成员
  removeMember (id) {
    return request.delete(`/family/member/${id}`)
  }
}
