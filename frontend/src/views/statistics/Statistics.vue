<template>
  <div class="statistics-page">
    <!-- 时间维度切换 -->
    <van-tabs v-model="activeTab" @change="onTabChange" class="period-tabs">
      <van-tab title="日" name="daily" />
      <van-tab title="周" name="weekly" />
      <van-tab title="月" name="monthly" />
      <van-tab title="年" name="yearly" />
    </van-tabs>

    <!-- 日期选择器 -->
    <div class="date-selector" @click="showDatePicker = true">
      <van-icon name="arrow-left" class="arrow" @click.stop="prevPeriod" />
      <span class="date-text">{{ displayDateText }}</span>
      <van-icon name="arrow" class="arrow" @click.stop="nextPeriod" />
    </div>

    <!-- 统计概览卡片 -->
    <div class="summary-card">
      <div class="summary-item">
        <div class="label">收入</div>
        <div class="value income">¥{{ formatAmount(statistics.totalIncome) }}</div>
      </div>
      <div class="summary-item">
        <div class="label">支出</div>
        <div class="value expense">¥{{ formatAmount(statistics.totalExpense) }}</div>
      </div>
      <div class="summary-item">
        <div class="label">结余</div>
        <div class="value" :class="balanceClass">¥{{ formatAmount(balance) }}</div>
      </div>
    </div>

    <!-- 收支类型切换 -->
    <div class="type-switch">
      <div
        :class="['type-btn', { active: chartType === 'expense' }]"
        @click="chartType = 'expense'"
      >
        支出
      </div>
      <div
        :class="['type-btn', { active: chartType === 'income' }]"
        @click="chartType = 'income'"
      >
        收入
      </div>
    </div>

    <!-- 饼图区域 -->
    <div class="chart-section">
      <div v-if="hasChartData" ref="chartRef" class="pie-chart"></div>
      <van-empty v-else :description="chartType === 'expense' ? '暂无支出记录' : '暂无收入记录'" />
    </div>

    <!-- 分类明细列表 -->
    <div class="category-list">
      <div class="list-header">分类明细</div>
      <div v-if="categoryList.length" class="list-content">
        <div
          v-for="(item, index) in categoryList"
          :key="item.categoryId"
          class="category-item"
        >
          <div class="item-left">
            <div class="rank" :class="getRankClass(index)">{{ index + 1 }}</div>
            <div class="category-info">
              <div class="category-name">{{ item.categoryName }}</div>
              <div class="category-count">{{ item.count }}笔</div>
            </div>
          </div>
          <div class="item-right">
            <div class="amount">¥{{ formatAmount(item.amount) }}</div>
            <div class="percent">{{ item.percent }}%</div>
          </div>
        </div>
      </div>
      <van-empty v-else description="暂无数据" />
    </div>

    <!-- 日期选择弹窗 -->
    <van-popup v-model="showDatePicker" position="bottom" round>
      <van-datetime-picker
        v-if="activeTab === 'daily'"
        v-model="currentDate"
        type="date"
        title="选择日期"
        :max-date="maxDate"
        @confirm="onDateConfirm"
        @cancel="showDatePicker = false"
      />
      <van-datetime-picker
        v-else-if="activeTab === 'monthly'"
        v-model="currentDate"
        type="year-month"
        title="选择月份"
        :max-date="maxDate"
        @confirm="onDateConfirm"
        @cancel="showDatePicker = false"
      />
      <van-picker
        v-else-if="activeTab === 'yearly'"
        :columns="yearColumns"
        :default-index="yearDefaultIndex"
        title="选择年份"
        @confirm="onYearConfirm"
        @cancel="showDatePicker = false"
      />
      <van-datetime-picker
        v-else
        v-model="currentDate"
        type="date"
        title="选择周"
        :max-date="maxDate"
        @confirm="onDateConfirm"
        @cancel="showDatePicker = false"
      />
    </van-popup>
  </div>
</template>

<script>
import statisticsApi from '@/api/modules/statistics'
import * as echarts from 'echarts/core'
import { PieChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

// 注册 ECharts 组件
echarts.use([PieChart, TitleComponent, TooltipComponent, LegendComponent, CanvasRenderer])

export default {
  name: 'Statistics',
  data () {
    return {
      activeTab: 'monthly',
      chartType: 'expense',
      currentDate: new Date(),
      maxDate: new Date(),
      showDatePicker: false,
      statistics: {
        totalIncome: 0,
        totalExpense: 0,
        categoryStats: []
      },
      chart: null,
      loading: false
    }
  },
  computed: {
    balance () {
      return this.statistics.totalIncome - this.statistics.totalExpense
    },
    balanceClass () {
      if (this.balance > 0) return 'income'
      if (this.balance < 0) return 'expense'
      return ''
    },
    displayDateText () {
      const date = this.currentDate
      const year = date.getFullYear()
      const month = date.getMonth() + 1
      const day = date.getDate()

      switch (this.activeTab) {
        case 'daily':
          return `${year}年${month}月${day}日`
        case 'weekly':
          return this.getWeekRange(date)
        case 'monthly':
          return `${year}年${month}月`
        case 'yearly':
          return `${year}年`
        default:
          return ''
      }
    },
    yearColumns () {
      const currentYear = new Date().getFullYear()
      const years = []
      for (let i = currentYear; i >= currentYear - 10; i--) {
        years.push(i.toString())
      }
      return years
    },
    yearDefaultIndex () {
      const currentYear = new Date().getFullYear()
      const selectedYear = this.currentDate.getFullYear()
      return currentYear - selectedYear
    },
    categoryList () {
      const stats = this.statistics.categoryStats || []
      const type = this.chartType === 'expense' ? 1 : 2
      const filtered = stats.filter(item => item.type === type)
      const total = filtered.reduce((sum, item) => sum + item.amount, 0)

      return filtered
        .map(item => ({
          ...item,
          percent: total > 0 ? ((item.amount / total) * 100).toFixed(1) : '0.0'
        }))
        .sort((a, b) => b.amount - a.amount)
    },
    hasChartData () {
      return this.categoryList.length > 0
    }
  },
  watch: {
    chartType () {
      this.renderChart()
    },
    categoryList: {
      handler () {
        this.$nextTick(() => {
          this.renderChart()
        })
      },
      deep: true
    }
  },
  mounted () {
    this.loadStatistics()
  },
  beforeDestroy () {
    if (this.chart) {
      this.chart.dispose()
      this.chart = null
    }
  },
  methods: {
    formatAmount (amount) {
      if (amount === null || amount === undefined) return '0.00'
      return Number(amount).toFixed(2)
    },
    getWeekRange (date) {
      const d = new Date(date)
      const day = d.getDay()
      const diff = d.getDate() - day + (day === 0 ? -6 : 1)
      const monday = new Date(d.setDate(diff))
      const sunday = new Date(monday)
      sunday.setDate(monday.getDate() + 6)

      const formatDate = (dt) => `${dt.getMonth() + 1}/${dt.getDate()}`
      return `${monday.getFullYear()}年 ${formatDate(monday)} - ${formatDate(sunday)}`
    },
    getRankClass (index) {
      if (index === 0) return 'rank-1'
      if (index === 1) return 'rank-2'
      if (index === 2) return 'rank-3'
      return ''
    },
    onTabChange () {
      this.loadStatistics()
    },
    prevPeriod () {
      const date = new Date(this.currentDate)
      switch (this.activeTab) {
        case 'daily':
          date.setDate(date.getDate() - 1)
          break
        case 'weekly':
          date.setDate(date.getDate() - 7)
          break
        case 'monthly':
          date.setMonth(date.getMonth() - 1)
          break
        case 'yearly':
          date.setFullYear(date.getFullYear() - 1)
          break
      }
      this.currentDate = date
      this.loadStatistics()
    },
    nextPeriod () {
      const date = new Date(this.currentDate)
      const now = new Date()

      switch (this.activeTab) {
        case 'daily':
          date.setDate(date.getDate() + 1)
          break
        case 'weekly':
          date.setDate(date.getDate() + 7)
          break
        case 'monthly':
          date.setMonth(date.getMonth() + 1)
          break
        case 'yearly':
          date.setFullYear(date.getFullYear() + 1)
          break
      }

      if (date <= now) {
        this.currentDate = date
        this.loadStatistics()
      }
    },
    onDateConfirm (date) {
      this.currentDate = date
      this.showDatePicker = false
      this.loadStatistics()
    },
    onYearConfirm (year) {
      const date = new Date(this.currentDate)
      date.setFullYear(parseInt(year))
      this.currentDate = date
      this.showDatePicker = false
      this.loadStatistics()
    },
    async loadStatistics () {
      if (this.loading) return
      this.loading = true

      try {
        const date = this.currentDate
        const year = date.getFullYear()
        const month = date.getMonth() + 1
        const day = date.getDate()
        const dateStr = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`

        let res
        switch (this.activeTab) {
          case 'daily':
            res = await statisticsApi.getDaily(dateStr)
            break
          case 'weekly':
            res = await statisticsApi.getWeekly(dateStr)
            break
          case 'monthly':
            res = await statisticsApi.getMonthly(year, month)
            break
          case 'yearly':
            res = await statisticsApi.getYearly(year)
            break
        }

        if (res && res.data) {
          this.statistics = res.data
        }
      } catch (error) {
        console.error('Failed to load statistics:', error)
      } finally {
        this.loading = false
      }
    },
    renderChart () {
      if (!this.hasChartData) {
        if (this.chart) {
          this.chart.dispose()
          this.chart = null
        }
        return
      }

      this.$nextTick(() => {
        const chartDom = this.$refs.chartRef
        if (!chartDom) return

        if (!this.chart) {
          this.chart = echarts.init(chartDom)
        }

        const data = this.categoryList.map(item => ({
          name: item.categoryName,
          value: item.amount
        }))

        const option = {
          tooltip: {
            trigger: 'item',
            formatter: '{b}: ¥{c} ({d}%)'
          },
          legend: {
            orient: 'horizontal',
            bottom: 10,
            left: 'center',
            itemWidth: 10,
            itemHeight: 10,
            textStyle: {
              fontSize: 12,
              color: '#666'
            }
          },
          series: [
            {
              type: 'pie',
              radius: ['40%', '65%'],
              center: ['50%', '40%'],
              avoidLabelOverlap: true,
              itemStyle: {
                borderRadius: 4,
                borderColor: '#fff',
                borderWidth: 2
              },
              label: {
                show: false
              },
              emphasis: {
                label: {
                  show: true,
                  fontSize: 14,
                  fontWeight: 'bold'
                }
              },
              labelLine: {
                show: false
              },
              data: data
            }
          ],
          color: [
            '#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de',
            '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc', '#48b8d0'
          ]
        }

        this.chart.setOption(option)
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.statistics-page {
  min-height: 100%;
  background-color: #f7f8fa;
  padding-bottom: 70px;
}

.period-tabs {
  ::v-deep .van-tabs__nav {
    background-color: #fff;
  }
  ::v-deep .van-tab--active {
    color: #1989fa;
  }
  ::v-deep .van-tabs__line {
    background-color: #1989fa;
  }
}

.date-selector {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px 16px;
  background-color: #fff;
  border-bottom: 1px solid #ebedf0;

  .arrow {
    padding: 8px;
    color: #969799;
    font-size: 14px;

    &:active {
      color: #1989fa;
    }
  }

  .date-text {
    margin: 0 16px;
    font-size: 15px;
    color: #323233;
    font-weight: 500;
  }
}

.summary-card {
  display: flex;
  margin: 12px;
  padding: 16px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  .summary-item {
    flex: 1;
    text-align: center;

    .label {
      font-size: 13px;
      color: #969799;
      margin-bottom: 8px;
    }

    .value {
      font-size: 18px;
      font-weight: 600;
      color: #323233;

      &.income {
        color: #07c160;
      }

      &.expense {
        color: #ee0a24;
      }
    }
  }
}

.type-switch {
  display: flex;
  margin: 0 12px 12px;
  background-color: #f2f3f5;
  border-radius: 8px;
  padding: 4px;

  .type-btn {
    flex: 1;
    text-align: center;
    padding: 8px 0;
    font-size: 14px;
    color: #969799;
    border-radius: 6px;
    transition: all 0.2s;

    &.active {
      background-color: #fff;
      color: #323233;
      font-weight: 500;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
    }
  }
}

.chart-section {
  margin: 0 12px 12px;
  padding: 16px;
  background-color: #fff;
  border-radius: 8px;

  .pie-chart {
    width: 100%;
    height: 280px;
  }

  ::v-deep .van-empty {
    padding: 40px 0;
  }
}

.category-list {
  margin: 0 12px 12px;
  background-color: #fff;
  border-radius: 8px;
  overflow: hidden;

  .list-header {
    padding: 12px 16px;
    font-size: 14px;
    font-weight: 500;
    color: #323233;
    border-bottom: 1px solid #f5f5f5;
  }

  .list-content {
    .category-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 12px 16px;
      border-bottom: 1px solid #f5f5f5;

      &:last-child {
        border-bottom: none;
      }

      .item-left {
        display: flex;
        align-items: center;

        .rank {
          width: 20px;
          height: 20px;
          border-radius: 4px;
          background-color: #f2f3f5;
          color: #969799;
          font-size: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 12px;

          &.rank-1 {
            background-color: #ff976a;
            color: #fff;
          }

          &.rank-2 {
            background-color: #ffc107;
            color: #fff;
          }

          &.rank-3 {
            background-color: #07c160;
            color: #fff;
          }
        }

        .category-info {
          .category-name {
            font-size: 14px;
            color: #323233;
            margin-bottom: 2px;
          }

          .category-count {
            font-size: 12px;
            color: #969799;
          }
        }
      }

      .item-right {
        text-align: right;

        .amount {
          font-size: 15px;
          font-weight: 500;
          color: #323233;
          margin-bottom: 2px;
        }

        .percent {
          font-size: 12px;
          color: #969799;
        }
      }
    }
  }

  ::v-deep .van-empty {
    padding: 40px 0;
  }
}
</style>
