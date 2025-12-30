const state = {
  transactions: [],
  currentPage: 1,
  totalPages: 0,
  loading: false
}

const mutations = {
  SET_TRANSACTIONS (state, transactions) {
    state.transactions = transactions
  },
  APPEND_TRANSACTIONS (state, transactions) {
    state.transactions = [...state.transactions, ...transactions]
  },
  SET_CURRENT_PAGE (state, page) {
    state.currentPage = page
  },
  SET_TOTAL_PAGES (state, totalPages) {
    state.totalPages = totalPages
  },
  SET_LOADING (state, loading) {
    state.loading = loading
  },
  CLEAR_TRANSACTIONS (state) {
    state.transactions = []
    state.currentPage = 1
    state.totalPages = 0
  }
}

const actions = {
  setTransactions ({ commit }, transactions) {
    commit('SET_TRANSACTIONS', transactions)
  },
  appendTransactions ({ commit }, transactions) {
    commit('APPEND_TRANSACTIONS', transactions)
  },
  setCurrentPage ({ commit }, page) {
    commit('SET_CURRENT_PAGE', page)
  },
  setTotalPages ({ commit }, totalPages) {
    commit('SET_TOTAL_PAGES', totalPages)
  },
  setLoading ({ commit }, loading) {
    commit('SET_LOADING', loading)
  },
  clearTransactions ({ commit }) {
    commit('CLEAR_TRANSACTIONS')
  }
}

const getters = {
  hasMore: state => state.currentPage < state.totalPages
}

export default {
  namespaced: true,
  state,
  mutations,
  actions,
  getters
}
