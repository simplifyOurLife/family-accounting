<template>
  <div class="transaction-page">
    <!-- 顶部类型切换 -->
    <van-tabs v-model="activeType" @change="onTypeChange" class="type-tabs">
      <van-tab title="支出" name="expense" />
      <van-tab title="收入" name="income" />
    </van-tabs>

    <!-- 金额显示区域 -->
    <div class="amount-display">
      <span class="currency">¥</span>
      <span class="amount">{{ displayAmount }}</span>
    </div>

    <!-- 分类选择区域 -->
    <div class="category-section">
      <div class="section-title">选择分类</div>
      <category-picker
        :type="activeType"
        :selected-id="form.categoryId"
        @select="onCategorySelect"
      />
    </div>

    <!-- 日期和备注 -->
    <van-cell-group class="form-group">
      <van-cell
        title="日期"
        :value="displayDate"
        is-link
        @click="showDatePicker = true"
      />
      <van-field
        v-model="form.note"
        label="备注"
        placeholder="添加备注（可选）"
        maxlength="200"
      />
    </van-cell-group>

    <!-- 数字键盘 -->
    <div class="keyboard-section">
      <div class="keyboard">
        <div class="keyboard-row">
          <div class="key" @click="inputNumber('7')">7</div>
          <div class="key" @click="inputNumber('8')">8</div>
          <div class="key" @click="inputNumber('9')">9</div>
          <div class="key date-key" @click="showDatePicker = true">
            <van-icon name="calendar-o" />
            <span>{{ shortDate }}</span>
          </div>
        </div>
        <div class="keyboard-row">
          <div class="key" @click="inputNumber('4')">4</div>
          <div class="key" @click="inputNumber('5')">5</div>
          <div class="key" @click="inputNumber('6')">6</div>
          <div class="key operator" @click="inputOperator('+')">+</div>
        </div>
        <div class="keyboard-row">
          <div class="key" @click="inputNumber('1')">1</div>
          <div class="key" @click="inputNumber('2')">2</div>
          <div class="key" @click="inputNumber('3')">3</div>
          <div class="key operator" @click="inputOperator('-')">-</div>
        </div>
        <div class="keyboard-row">
          <div class="key" @click="inputNumber('.')">.</div>
          <div class="key" @click="inputNumber('0')">0</div>
          <div class="key" @click="backspace">
            <van-icon name="arrow-left" />
          </div>
          <div class="key submit" @click="handleSubmit">
            {{ hasOperator ? '=' : '完成' }}
          </div>
        </div>
      </div>
    </div>

    <!-- 日期选择器弹窗 -->
    <van-popup v-model="showDatePicker" position="bottom">
      <van-datetime-picker
        v-model="currentDate"
        type="date"
        title="选择日期"
        :max-date="maxDate"
        @confirm="onDateConfirm"
        @cancel="showDatePicker = false"
      />
    </van-popup>

    <!-- 交易列表 -->
    <div class="transaction-list-section">
      <div class="section-header">
        <span class="section-title">最近记录</span>
      </div>
      <transaction-list
        ref="transactionList"
        @edit="onEditTransaction"
      />
    </div>
  </div>
</template>

<script>
import CategoryPicker from '@/components/CategoryPicker.vue'
import TransactionList from '@/components/TransactionList.vue'
import transactionApi from '@/api/modules/transaction'
import { Toast } from 'vant'

export default {
  name: 'Transaction',
  components: {
    CategoryPicker,
    TransactionList
  },
  data () {
    return {
      activeType: 'expense',
      amountStr: '',
      form: {
        categoryId: null,
        categoryName: '',
        note: '',
        transactionDate: new Date()
      },
      showDatePicker: false,
      currentDate: new Date(),
      maxDate: new Date(),
      editingId: null
    }
  },
  computed: {
    displayAmount () {
      return this.amountStr || '0.00'
    },
    displayDate () {
      const date = this.form.transactionDate
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    },
    shortDate () {
      const date = this.form.transactionDate
      return `${date.getMonth() + 1}/${date.getDate()}`
    },
    hasOperator () {
      return this.amountStr.includes('+') || this.amountStr.includes('-')
    }
  },
  methods: {
    onTypeChange () {
      this.form.categoryId = null
      this.form.categoryName = ''
    },
    onCategorySelect (category) {
      this.form.categoryId = category.id
      this.form.categoryName = category.name
    },
    inputNumber (num) {
      // 限制小数点后两位
      if (num === '.') {
        const parts = this.amountStr.split(/[+-]/)
        const lastPart = parts[parts.length - 1]
        if (lastPart.includes('.')) return
      }

      // 检查小数位数
      const parts = this.amountStr.split(/[+-]/)
      const lastPart = parts[parts.length - 1]
      if (lastPart.includes('.')) {
        const decimalPart = lastPart.split('.')[1]
        if (decimalPart && decimalPart.length >= 2 && num !== '.') return
      }

      // 限制整数部分长度
      if (!lastPart.includes('.') && lastPart.length >= 10 && num !== '.') return

      this.amountStr += num
    },
    inputOperator (op) {
      if (!this.amountStr || this.amountStr.endsWith('+') || this.amountStr.endsWith('-')) return
      if (this.hasOperator) {
        this.calculateResult()
      }
      this.amountStr += op
    },
    backspace () {
      this.amountStr = this.amountStr.slice(0, -1)
    },
    calculateResult () {
      if (!this.hasOperator) return
      try {
        // eslint-disable-next-line no-eval
        const result = eval(this.amountStr)
        this.amountStr = Math.max(0, result).toFixed(2).replace(/\.?0+$/, '')
      } catch (e) {
        // 计算错误时不处理
      }
    },
    onDateConfirm (date) {
      this.form.transactionDate = date
      this.showDatePicker = false
    },
    async handleSubmit () {
      if (this.hasOperator) {
        this.calculateResult()
        return
      }

      const amount = parseFloat(this.amountStr)
      if (!amount || amount <= 0) {
        Toast('请输入金额')
        return
      }

      if (!this.form.categoryId) {
        Toast('请选择分类')
        return
      }

      const data = {
        type: this.activeType === 'expense' ? 1 : 2,
        amount: amount,
        categoryId: this.form.categoryId,
        transactionDate: this.displayDate,
        note: this.form.note || ''
      }

      try {
        if (this.editingId) {
          await transactionApi.update(this.editingId, data)
          Toast.success('修改成功')
          this.editingId = null
        } else {
          await transactionApi.create(data)
          Toast.success('记账成功')
        }
        this.resetForm()
        this.$refs.transactionList && this.$refs.transactionList.refresh()
      } catch (error) {
        Toast.fail(error.message || '操作失败')
      }
    },
    resetForm () {
      this.amountStr = ''
      this.form.categoryId = null
      this.form.categoryName = ''
      this.form.note = ''
      this.form.transactionDate = new Date()
      this.currentDate = new Date()
      this.editingId = null
    },
    onEditTransaction (transaction) {
      this.editingId = transaction.id
      this.activeType = transaction.type === 1 ? 'expense' : 'income'
      this.amountStr = String(transaction.amount)
      this.form.categoryId = transaction.categoryId
      this.form.categoryName = transaction.categoryName
      this.form.note = transaction.note || ''
      this.form.transactionDate = new Date(transaction.transactionDate)
      this.currentDate = new Date(transaction.transactionDate)
      // 滚动到顶部
      window.scrollTo({ top: 0, behavior: 'smooth' })
    }
  }
}
</script>

<style lang="scss" scoped>
.transaction-page {
  min-height: 100%;
  background-color: #f7f8fa;
  padding-bottom: 60px;
}

.type-tabs {
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

.amount-display {
  background-color: #fff;
  padding: 20px 16px;
  text-align: right;
  border-bottom: 1px solid #ebedf0;

  .currency {
    font-size: 20px;
    color: #323233;
    margin-right: 4px;
  }

  .amount {
    font-size: 36px;
    font-weight: 500;
    color: #323233;
  }
}

.category-section {
  background-color: #fff;
  margin-top: 10px;
  padding: 12px 16px;

  .section-title {
    font-size: 14px;
    color: #969799;
    margin-bottom: 12px;
  }
}

.form-group {
  margin-top: 10px;
}

.keyboard-section {
  position: fixed;
  bottom: 50px;
  left: 0;
  right: 0;
  background-color: #f2f3f5;
  padding: 6px;
  z-index: 100;
}

.keyboard {
  display: flex;
  flex-direction: column;
  gap: 6px;

  .keyboard-row {
    display: flex;
    gap: 6px;

    .key {
      flex: 1;
      height: 48px;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #fff;
      border-radius: 4px;
      font-size: 20px;
      color: #323233;
      cursor: pointer;
      user-select: none;

      &:active {
        background-color: #e8e8e8;
      }

      &.date-key {
        flex-direction: column;
        font-size: 12px;
        color: #969799;

        .van-icon {
          font-size: 16px;
          margin-bottom: 2px;
        }
      }

      &.operator {
        background-color: #f0f0f0;
        color: #1989fa;
        font-size: 24px;
      }

      &.submit {
        background-color: #1989fa;
        color: #fff;
        font-size: 16px;
      }
    }
  }
}

.transaction-list-section {
  margin-top: 10px;
  padding-bottom: 220px;

  .section-header {
    padding: 12px 16px;
    background-color: #fff;
    border-bottom: 1px solid #ebedf0;

    .section-title {
      font-size: 14px;
      color: #323233;
      font-weight: 500;
    }
  }
}
</style>
