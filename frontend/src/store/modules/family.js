const state = {
  familyInfo: null,
  members: [],
  categories: [],
  accountBooks: []
}

const mutations = {
  SET_FAMILY_INFO (state, familyInfo) {
    state.familyInfo = familyInfo
  },
  SET_MEMBERS (state, members) {
    state.members = members
  },
  SET_CATEGORIES (state, categories) {
    state.categories = categories
  },
  SET_ACCOUNT_BOOKS (state, accountBooks) {
    state.accountBooks = accountBooks
  },
  CLEAR_FAMILY (state) {
    state.familyInfo = null
    state.members = []
    state.categories = []
    state.accountBooks = []
  }
}

const actions = {
  setFamilyInfo ({ commit }, familyInfo) {
    commit('SET_FAMILY_INFO', familyInfo)
  },
  setMembers ({ commit }, members) {
    commit('SET_MEMBERS', members)
  },
  setCategories ({ commit }, categories) {
    commit('SET_CATEGORIES', categories)
  },
  setAccountBooks ({ commit }, accountBooks) {
    commit('SET_ACCOUNT_BOOKS', accountBooks)
  },
  clearFamily ({ commit }) {
    commit('CLEAR_FAMILY')
  }
}

const getters = {
  hasFamily: state => !!state.familyInfo,
  defaultAccountBook: state => state.accountBooks.find(book => book.isDefault) || state.accountBooks[0]
}

export default {
  namespaced: true,
  state,
  mutations,
  actions,
  getters
}
