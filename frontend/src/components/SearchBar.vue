<template>
  <div class="search-bar">
    <!-- 搜索输入框 -->
    <van-search
      v-model="searchKeyword"
      placeholder="搜索交易备注"
      show-action
      @search="onSearch"
      @focus="showSuggestions = true"
      @clear="onClear"
    >
      <template #action>
        <div @click="onSearch">搜索</div>
      </template>
    </van-search>

    <!-- 搜索建议弹出层 -->
    <van-popup
      v-model="showSuggestions"
      position="top"
      :style="{ top: '54px', width: '100%' }"
      :overlay="false"
    >
      <div class="suggestions-panel">
        <div v-if="suggestions.length > 0" class="suggestions-list">
          <div class="suggestions-title">搜索建议</div>
          <van-cell
            v-for="(item, index) in suggestions"
            :key="index"
            :title="item"
            is-link
            @click="selectSuggestion(item)"
          />
        </div>
        <van-empty v-else description="暂无搜索建议" />
      </div>
    </van-popup>

    <!-- 活跃筛选标签 -->
    <div v-if="hasActiveFilters" class="active-filters">
      <van-tag
        v-if="activeFilters.dateRange"
        closeable
        type="primary"
        size="medium"
        @close="removeFilter('dateRange')"
      >
        {{ activeFilters.dateRange }}
      </van-tag>
      <van-tag
        v-if="activeFilters.amountRange"
        closeable
        type="primary"
        size="medium"
        @close="removeFilter('amountRange')"
      >
        {{ activeFilters.amountRange }}
      </van-tag>
      <van-tag
        v-if="activeFilters.category"
        closeable
        type="primary"
        size="medium"
        @close="removeFilter('category')"
      >
        {{ activeFilters.category }}
      </van-tag>
      <van-tag
        v-if="activeFilters.member"
        closeable
        type="primary"
        size="medium"
        @close="removeFilter('member')"
      >
        {{ activeFilters.member }}
      </van-tag>
      <van-tag
        v-if="activeFilters.type"
        closeable
        type="primary"
        size="medium"
        @close="removeFilter('type')"
      >
        {{ activeFilters.type }}
      </van-tag>
      <van-button
        size="small"
        type="default"
        @click="clearAllFilters"
      >
        清空筛选
      </van-button>
    </div>
  </div>
</template>

<script>
import { getSearchSuggestions } from '@/api/modules/search'

export default {
  name: 'SearchBar',
  props: {
    // 当前筛选条件
    filters: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      searchKeyword: '',
      showSuggestions: false,
      suggestions: []
    }
  },
  computed: {
    // 是否有活跃的筛选条件
    hasActiveFilters() {
      return Object.keys(this.activeFilters).length > 0
    },
    // 活跃的筛选条件标签
    activeFilters() {
      const filters = {}
      
      // 日期范围
      if (this.filters.startDate || this.filters.endDate) {
        const start = this.filters.startDate || '开始'
        const end = this.filters.endDate || '结束'
        filters.dateRange = `日期: ${start} ~ ${end}`
      }
      
      // 金额范围
      if (this.filters.minAmount || this.filters.maxAmount) {
        const min = this.filters.minAmount || '0'
        const max = this.filters.maxAmount || '∞'
        filters.amountRange = `金额: ${min} ~ ${max}`
      }
      
      // 分类
      if (this.filters.categoryIds && this.filters.categoryIds.length > 0) {
        filters.category = `分类: ${this.filters.categoryIds.length}个`
      }
      
      // 成员
      if (this.filters.memberIds && this.filters.memberIds.length > 0) {
        filters.member = `成员: ${this.filters.memberIds.length}个`
      }
      
      // 类型
      if (this.filters.type) {
        filters.type = this.filters.type === 1 ? '类型: 支出' : '类型: 收入'
      }
      
      return filters
    }
  },
  watch: {
    searchKeyword(val) {
      if (val && this.showSuggestions) {
        this.loadSuggestions()
      }
    }
  },
  methods: {
    // 执行搜索
    onSearch() {
      this.showSuggestions = false
      this.$emit('search', this.searchKeyword)
    },
    // 清空搜索
    onClear() {
      this.searchKeyword = ''
      this.$emit('search', '')
    },
    // 加载搜索建议
    async loadSuggestions() {
      try {
        const res = await getSearchSuggestions(10)
        if (res.code === 200) {
          this.suggestions = res.data || []
        }
      } catch (error) {
        console.error('加载搜索建议失败:', error)
      }
    },
    // 选择搜索建议
    selectSuggestion(keyword) {
      this.searchKeyword = keyword
      this.showSuggestions = false
      this.$emit('search', keyword)
    },
    // 移除单个筛选条件
    removeFilter(filterType) {
      this.$emit('remove-filter', filterType)
    },
    // 清空所有筛选条件
    clearAllFilters() {
      this.$emit('clear-filters')
    }
  }
}
</script>

<style scoped lang="scss">
.search-bar {
  background: #fff;
}

.suggestions-panel {
  background: #fff;
  max-height: 300px;
  overflow-y: auto;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.suggestions-title {
  padding: 12px 16px;
  font-size: 14px;
  color: #969799;
}

.suggestions-list {
  .van-cell {
    padding: 12px 16px;
  }
}

.active-filters {
  padding: 8px 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  background: #f7f8fa;
  
  .van-tag {
    margin: 0;
  }
  
  .van-button {
    height: 24px;
    line-height: 24px;
    padding: 0 8px;
  }
}
</style>
