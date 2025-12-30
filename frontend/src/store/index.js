import Vue from 'vue'
import Vuex from 'vuex'
import createPersistedState from 'vuex-persistedstate'
import user from './modules/user'
import family from './modules/family'
import transaction from './modules/transaction'

Vue.use(Vuex)

export default new Vuex.Store({
  modules: {
    user,
    family,
    transaction
  },
  plugins: [
    createPersistedState({
      key: 'family-accounting',
      paths: ['user.token', 'user.userInfo']
    })
  ]
})
