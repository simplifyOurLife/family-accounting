<template>
  <div class="transaction-list">
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="onLoad"
      >
        <van-empty v-if="!loading && !groupedTransactions.length" description="暂无记录" />
        <template v-else>
          <div
            v-for="group in groupedTransactions"
            :key="group.date"
            class="date-group"
          >
            <div class="date-header">
              <span class="date">{{ formatDateHeader(group.date) }}</span>
              <span class="summary">
                <span v-if="group.income > 0" class="income">收入 ¥{{ group.income.toFixed(2) }}</span>
                <span v-if="group.expense > 0" class="expense">支出 ¥{{ group.expense.toFixed(2) }}</span>
              </span>
            </div>
            <van-swipe-cell
              v-for="item in group.items"
              :key="item.id"
              :before-close="beforeClose"
            >
              <div class="transaction-item" @click="onItemClick(item)">
                <div class="item-left">
                  <div class="category-icon">
                    <van-icon :name="getCategoryIcon(item.categoryName)" />
                  </div>
                  <div class="item-info">
                    <div class="category-name">{{ item.categoryName }}</div>
                    <div v-if="item.note" class="note" v-html="highlightKeyword(item.note)"></div>
                  </div>
                </div>
                <div class="item-right">
                  <span :class="['amount', item.type === 1 ? 'expense' : 'income']">
                    {{ item.type === 1 ? '-' : '+' }}¥{{ item.amount.toFixed(2) }}
                  </span>
                </div>
              </div>
              <template #right>
                <van-button
                  square
                  type="primary"
                  text="编辑"
                  class="swipe-btn"
                  @click="onEdit(item)"
                />
                <van-button
                  square
                  type="danger"
                  text="删除"
                  class="swipe-btn"
                  @click="onDelete(item)"
                />
              </template>
            </van-swipe-cell>
          </div>
        </template>
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<script>
import transactionApi from '@/api/modules/transaction'
import { searchTransactions } from '@/api/modules/search'
import { Toast, Dialog } from 'vant'

export default {
  name: 'TransactionList',
  props: {
    // 搜索参数
    searchParams: {
      type: Object,
      default: null
    }
  },
  data () {
    return {
      transactions: [],
      loading: false,
      finished: false,
      refreshing: false,
      page: 1,
      pageSize: 20
    }
  },
  computed: {
    groupedTransactions () {
      const groups = {}
      this.transactions.forEach(item => {
        const date = item.transactionDate
        if (!groups[date]) {
          groups[date] = {
            date,
            items: [],
            income: 0,
            expense: 0
          }
        }
        groups[date].items.push(item)
        if (item.type === 1) {
          groups[date].expense += item.amount
        } else {
          groups[date].income += item.amount
        }
      })
      // 按日期降序排序
      return Object.values(groups).sort((a, b) =>
        new Date(b.date) - new Date(a.date)
      )
    }
  },
  watch: {
    searchParams: {
      handler () {
        this.refresh()
      },
      deep: true
    }
  },
  methods: {
    async loadData () {
      try {
        console.log('Loading transaction data, page:', this.page, 'pageSize:', this.pageSize)
        
        let res
        // 如果有搜索参数，使用搜索接口
        if (this.searchParams) {
          const searchData = {
            ...this.searchParams,
            page: this.page,
            size: this.pageSize
          }
          res = await searchTransactions(searchData)
        } else {
          // 否则使用普通列表接口
          res = await transactionApi.getList({
            page: this.page,
            size: this.pageSize
          })
        }
        
        console.log('Transaction API response:', res)
        const data = res.data || {}
        const list = data.list || []
        console.log('Transaction list:', list)

        if (this.page === 1) {
          this.transactions = list
        } else {
          this.transactions = [...this.transactions, ...list]
        }

        this.finished = list.length < this.pageSize || this.page >= (data.pages || data.totalPages || 1)
        console.log('Transactions loaded:', this.transactions.length, 'finished:', this.finished)
      } catch (error) {
        console.error('Failed to load transactions:', error)
        this.finished = true
      }
    },
    async onLoad () {
      await this.loadData()
      this.loading = false
      this.page++
    },
    async onRefresh () {
      this.page = 1
      this.finished = false
      await this.loadData()
      this.refreshing = false
      this.page++
    },
    refresh () {
      this.page = 1
      this.finished = false
      this.transactions = []
      this.onLoad()
    },
    formatDateHeader (dateStr) {
      const date = new Date(dateStr)
      const today = new Date()
      const yesterday = new Date(today)
      yesterday.setDate(yesterday.getDate() - 1)

      const isToday = date.toDateString() === today.toDateString()
      const isYesterday = date.toDateString() === yesterday.toDateString()

      const month = date.getMonth() + 1
      const day = date.getDate()
      const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
      const weekDay = weekDays[date.getDay()]

      if (isToday) {
        return `${month}月${day}日 今天`
      } else if (isYesterday) {
        return `${month}月${day}日 昨天`
      } else {
        return `${month}月${day}日 ${weekDay}`
      }
    },
    getCategoryIcon (categoryName) {
      const iconMap = {
        餐饮: 'coupon-o',
        交通: 'logistics',
        购物: 'shopping-cart-o',
        娱乐: 'music-o',
        医疗: 'first-aid-o',
        教育: 'bookmark-o',
        居住: 'home-o',
        通讯: 'phone-o',
        工资: 'gold-coin-o',
        奖金: 'gift-o',
        投资: 'chart-trending-o',
        其他: 'ellipsis'
      }
      return iconMap[categoryName] || 'label-o'
    },
    onItemClick (item) {
      this.$emit('edit', item)
    },
    onEdit (item) {
      this.$emit('edit', item)
    },
    async onDelete (item) {
      try {
        await Dialog.confirm({
          title: '确认删除',
          message: '确定要删除这条记录吗？'
        })
        await transactionApi.delete(item.id)
        Toast.success('删除成功')
        this.refresh()
      } catch (error) {
        if (error !== 'cancel') {
          Toast.fail(error.message || '删除失败')
        }
      }
    },
    beforeClose ({ position }) {
      return position !== 'right'
    },
    // 高亮搜索关键词
    highlightKeyword (text) {
      if (!this.searchParams || !this.searchParams.keyword) {
        return text
      }
      const keyword = this.searchParams.keyword
      const regex = new RegExp(`(${keyword})`, 'gi')
      return text.replace(regex, '<span class="highlight">$1</span>')
    }
  }
}
</script>

<style lang="scss" scoped>
.transaction-list {
  background-color: #fff;
}

.date-group {
  .date-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    background-color: #f7f8fa;
    font-size: 13px;

    .date {
      color: #323233;
    }

    .summary {
      .income {
        color: #07c160;
        margin-right: 12px;
      }

      .expense {
        color: #ee0a24;
      }
    }
  }
}

.transaction-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background-color: #fff;
  border-bottom: 1px solid #f5f5f5;

  .item-left {
    display: flex;
    align-items: center;
    flex: 1;
    min-width: 0;

    .category-icon {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      background-color: #f7f8fa;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 12px;
      flex-shrink: 0;

      .van-icon {
        font-size: 20px;
        color: #646566;
      }
    }

    .item-info {
      flex: 1;
      min-width: 0;

      .category-name {
        font-size: 15px;
        color: #323233;
        margin-bottom: 2px;
      }

      .note {
        font-size: 12px;
        color: #969799;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        
        ::v-deep .highlight {
          color: #1989fa;
          background-color: #e8f4ff;
          padding: 0 2px;
          border-radius: 2px;
        }
      }
    }
  }

  .item-right {
    flex-shrink: 0;
    margin-left: 12px;

    .amount {
      font-size: 16px;
      font-weight: 500;

      &.expense {
        color: #323233;
      }

      &.income {
        color: #07c160;
      }
    }
  }
}

.swipe-btn {
  height: 100%;
}
</style>
