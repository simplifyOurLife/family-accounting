const state = {
  token: '',
  userInfo: null
}

const mutations = {
  SET_TOKEN (state, token) {
    state.token = token
  },
  SET_USER_INFO (state, userInfo) {
    state.userInfo = userInfo
  },
  CLEAR_USER (state) {
    state.token = ''
    state.userInfo = null
  }
}

const actions = {
  setToken ({ commit }, token) {
    commit('SET_TOKEN', token)
  },
  setUserInfo ({ commit }, userInfo) {
    commit('SET_USER_INFO', userInfo)
  },
  logout ({ commit }) {
    commit('CLEAR_USER')
  }
}

const getters = {
  isLoggedIn: state => !!state.token,
  userInfo: state => state.userInfo
}

export default {
  namespaced: true,
  state,
  mutations,
  actions,
  getters
}
