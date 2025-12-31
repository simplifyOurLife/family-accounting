<template>
  <div class="category-picker">
    <van-loading v-if="loading" size="24px" vertical>加载中...</van-loading>
    <van-empty v-else-if="!categories.length" description="暂无分类" />
    <div v-else class="category-grid">
      <div
        v-for="category in categories"
        :key="category.id"
        class="category-item"
        :class="{
          active: selectedId === category.id,
          expanded: expandedIds.includes(category.id)
        }"
        @click="onCategoryClick(category)"
      >
        <div class="category-main">
          <div class="category-icon">
            <van-icon :name="getCategoryIcon(category)" />
          </div>
          <div class="category-name">{{ category.name }}</div>
          <div v-if="category.children && category.children.length" class="expand-hint">
            <span class="hint-text">点击展开</span>
            <van-icon
              :name="expandedIds.includes(category.id) ? 'arrow-up' : 'arrow-down'"
              class="expand-icon"
            />
          </div>
        </div>
        <!-- 子分类 -->
        <div
          v-if="category.children && category.children.length && expandedIds.includes(category.id)"
          class="sub-categories"
        >
          <div
            v-for="child in category.children"
            :key="child.id"
            class="sub-category-item"
            :class="{ active: selectedId === child.id }"
            @click.stop="onSubCategoryClick(child)"
          >
            <span class="sub-category-name">{{ child.name }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import categoryApi from '@/api/modules/category'

export default {
  name: 'CategoryPicker',
  props: {
    type: {
      type: String,
      default: 'expense',
      validator: val => ['expense', 'income'].includes(val)
    },
    selectedId: {
      type: [Number, String],
      default: null
    }
  },
  data () {
    return {
      categories: [],
      expandedIds: [],
      loading: false
    }
  },
  watch: {
    type: {
      immediate: true,
      handler () {
        this.loadCategories()
      }
    }
  },
  methods: {
    async loadCategories () {
      this.loading = true
      try {
        const typeValue = this.type === 'expense' ? 1 : 2
        const res = await categoryApi.getTree(typeValue)
        this.categories = res.data || []
        this.expandedIds = []
      } catch (error) {
        console.error('Failed to load categories:', error)
        this.categories = []
      } finally {
        this.loading = false
      }
    },
    onCategoryClick (category) {
      if (category.children && category.children.length) {
        // 有子分类的情况下，展开显示子分类
        this.toggleExpand(category.id)
        // 同时选择父分类（可选，根据需求决定）
        // this.$emit('select', category)
      } else {
        // 没有子分类，直接选择
        this.$emit('select', category)
      }
    },
    onSubCategoryClick (child) {
      // 选择子分类
      this.$emit('select', child)
    },
    toggleExpand (categoryId) {
      const index = this.expandedIds.indexOf(categoryId)
      if (index > -1) {
        this.expandedIds.splice(index, 1)
      } else {
        this.expandedIds.push(categoryId)
      }
    },
    getCategoryIcon (category) {
      // 根据分类名称返回对应图标
      const iconMap = {
        餐饮: 'coupon-o',
        交通: 'logistics',
        购物: 'shopping-cart-o',
        娱乐: 'music-o',
        医疗: 'add-o', // 使用 add-o 图标，更符合医疗加号标志
        教育: 'bookmark-o',
        居住: 'home-o',
        通讯: 'phone-o',
        工资: 'gold-coin-o',
        奖金: 'gift-o',
        投资: 'chart-trending-o',
        其他: 'ellipsis'
      }
      return iconMap[category.name] || 'label-o'
    }
  }
}
</script>

<style lang="scss" scoped>
.category-picker {
  min-height: 100px;
}

.category-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.category-item {
  width: calc(25% - 6px);
  background-color: #f7f8fa;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.2s;

  &.active {
    .category-main {
      background-color: #e6f4ff;

      .category-icon {
        color: #1989fa;
      }

      .category-name {
        color: #1989fa;
      }
    }
  }

  &.expanded {
    width: 100%;

    .category-main {
      border-bottom: 1px solid #ebedf0;
    }
  }

  .category-main {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 12px 8px;
    position: relative;
    cursor: pointer;

    &:active {
      background-color: #f0f0f0;
    }

    .category-icon {
      font-size: 24px;
      color: #646566;
      margin-bottom: 4px;
    }

    .category-name {
      font-size: 12px;
      color: #323233;
      text-align: center;
      word-break: break-all;
      margin-bottom: 2px;
    }

    .expand-hint {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 2px;

      .hint-text {
        font-size: 10px;
        color: #969799;
      }

      .expand-icon {
        font-size: 10px;
        color: #969799;
      }
    }
  }

  .sub-categories {
    display: flex;
    flex-wrap: wrap;
    padding: 8px;
    gap: 8px;
    background-color: #fff;

    .sub-category-item {
      padding: 6px 12px;
      background-color: #f7f8fa;
      border-radius: 4px;
      font-size: 12px;
      color: #323233;
      cursor: pointer;

      &:active {
        background-color: #e8e8e8;
      }

      &.active {
        background-color: #e6f4ff;
        color: #1989fa;
      }
    }
  }
}
</style>
