import request from '../request'

/**
 * 搜索交易记录
 * @param {Object} data 搜索条件
 * @returns {Promise}
 */
export function searchTransactions(data) {
  return request({
    url: '/api/search/transactions',
    method: 'post',
    data
  })
}

/**
 * 获取搜索建议
 * @param {Number} limit 返回数量限制
 * @returns {Promise}
 */
export function getSearchSuggestions(limit = 10) {
  return request({
    url: '/api/search/suggestions',
    method: 'get',
    params: { limit }
  })
}

/**
 * 获取搜索历史
 * @returns {Promise}
 */
export function getSearchHistory() {
  return request({
    url: '/api/search/history',
    method: 'get'
  })
}

/**
 * 删除搜索历史
 * @param {Number} id 搜索历史ID
 * @returns {Promise}
 */
export function deleteSearchHistory(id) {
  return request({
    url: `/api/search/history/${id}`,
    method: 'delete'
  })
}

/**
 * 清空搜索历史
 * @returns {Promise}
 */
export function clearSearchHistory() {
  return request({
    url: '/api/search/history',
    method: 'delete'
  })
}

/**
 * 保存筛选条件
 * @param {Object} data 筛选条件数据
 * @returns {Promise}
 */
export function saveFilter(data) {
  return request({
    url: '/api/filter/save',
    method: 'post',
    data
  })
}

/**
 * 更新筛选条件
 * @param {Number} id 筛选条件ID
 * @param {Object} data 筛选条件数据
 * @returns {Promise}
 */
export function updateFilter(id, data) {
  return request({
    url: `/api/filter/${id}`,
    method: 'put',
    data
  })
}

/**
 * 获取保存的筛选条件列表
 * @returns {Promise}
 */
export function getSavedFilters() {
  return request({
    url: '/api/filter/saved',
    method: 'get'
  })
}

/**
 * 获取筛选条件详情
 * @param {Number} id 筛选条件ID
 * @returns {Promise}
 */
export function getFilterById(id) {
  return request({
    url: `/api/filter/${id}`,
    method: 'get'
  })
}

/**
 * 应用筛选条件
 * @param {Number} id 筛选条件ID
 * @returns {Promise}
 */
export function applyFilter(id) {
  return request({
    url: `/api/filter/${id}/apply`,
    method: 'get'
  })
}

/**
 * 删除筛选条件
 * @param {Number} id 筛选条件ID
 * @returns {Promise}
 */
export function deleteFilter(id) {
  return request({
    url: `/api/filter/${id}`,
    method: 'delete'
  })
}
