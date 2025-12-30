import request from '../request'

export default {
  // 日统计
  getDaily (date) {
    return request.get('/statistics/daily', { params: { date } })
  },

  // 周统计
  getWeekly (date) {
    return request.get('/statistics/weekly', { params: { date } })
  },

  // 月统计
  getMonthly (year, month) {
    return request.get('/statistics/monthly', { params: { year, month } })
  },

  // 年统计
  getYearly (year) {
    return request.get('/statistics/yearly', { params: { year } })
  },

  // 分类统计
  getByCategory (params) {
    return request.get('/statistics/category', { params })
  }
}
