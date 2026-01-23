<template>
  <van-popup
    v-model="visible"
    position="bottom"
    :style="{ height: '80%' }"
    closeable
    round
  >
    <div class="filter-panel">
      <div class="filter-header">
        <h3>高级筛选</h3>
      </div>

      <div class="filter-content">
        <!-- 日期范围 -->
        <van-cell-group title="日期范围">
          <van-field
            v-model="localFilters.startDate"
            label="开始日期"
            placeholder="选择开始日期"
            readonly
            @click="showDatePicker('start')"
          />
          <van-field
            v-model="localFilters.endDate"
            label="结束日期"
            placeholder="选择结束日期"
            readonly
            @click="showDatePicker('end')"
          />
        </van-cell-group>

        <!-- 金额范围 -->
        <van-cell-group title="金额范围">
          <van-field
            v-model="localFilters.minAmount"
            type="number"
            label="最小金额"
            placeholder="请输入最小金额"
          />
          <van-field
            v-model="localFilters.maxAmount"
            type="number"
            label="最大金额"
            placeholder="请输入最大金额"
          />
        </van-cell-group>

        <!-- 交易类型 -->
        <van-cell-group title="交易类型">
          <van-radio-group v-model="localFilters.type">
            <van-cell title="全部" clickable @click="localFilters.type = null">
              <template #right-icon>
                <van-radio :name="null" />
              </template>
            </van-cell>
            <van-cell title="支出" clickable @click="localFilters.type = 1">
              <template #right-icon>
                <van-radio :name="1" />
              </template>
            </van-cell>
            <van-cell title="收入" clickable @click="localFilters.type = 2">
              <template #right-icon>
                <van-radio :name="2" />
              </template>
            </van-cell>
          </van-radio-group>
        </van-cell-group>

        <!-- 分类筛选 -->
        <van-cell-group title="分类">
          <van-field
            :value="selectedCategoriesText"
            label="选择分类"
            placeholder="点击选择分类"
            readonly
            is-link
            @click="showCategoryPicker = true"
          />
        </van-cell-group>

        <!-- 成员筛选 -->
        <van-cell-group title="成员">
          <van-field
            :value="selectedMembersText"
            label="选择成员"
            placeholder="点击选择成员"
            readonly
            is-link
            @click="showMemberPicker = true"
          />
        </van-cell-group>

        <!-- 保存筛选条件 -->
        <van-cell-group title="保存筛选">
          <van-field
            v-model="filterName"
            label="筛选名称"
            placeholder="输入名称以保存当前筛选条件"
          />
          <van-button
            type="primary"
            size="small"
            block
            @click="saveCurrentFilter"
          >
            保存筛选条件
          </van-button>
        </van-cell-group>

        <!-- 常用筛选 -->
        <van-cell-group v-if="savedFilters.length > 0" title="常用筛选">
          <van-cell
            v-for="filter in savedFilters"
            :key="filter.id"
            :title="filter.name"
            is-link
            @click="applySavedFilter(filter.id)"
          >
            <template #right-icon>
              <van-icon
                name="delete"
                class="delete-icon"
                @click.stop="deleteSavedFilter(filter.id)"
              />
            </template>
          </van-cell>
        </van-cell-group>
      </div>

      <div class="filter-footer">
        <van-button block @click="resetFilters">重置</van-button>
        <van-button type="primary" block @click="applyFilters">应用筛选</van-button>
      </div>
    </div>

    <!-- 日期选择器 -->
    <van-popup v-model="showDatePickerPopup" position="bottom">
      <van-datetime-picker
        v-model="currentDate"
        type="date"
        title="选择日期"
        @confirm="onDateConfirm"
        @cancel="showDatePickerPopup = false"
      />
    </van-popup>

    <!-- 分类选择器 -->
    <van-popup v-model="showCategoryPicker" position="bottom">
      <van-picker
        :columns="categoryColumns"
        @confirm="onCategoryConfirm"
        @cancel="showCategoryPicker = false"
      />
    </van-popup>

    <!-- 成员选择器 -->
    <van-popup v-model="showMemberPicker" position="bottom">
      <van-picker
        :columns="memberColumns"
        @confirm="onMemberConfirm"
        @cancel="showMemberPicker = false"
      />
    </van-popup>
  </van-popup>
</template>

<script>
import { getSavedFilters, saveFilter, applyFilter, deleteFilter } from '@/api/modules/search'
import categoryApi from '@/api/modules/category'
import familyApi from '@/api/modules/family'

export default {
  name: 'FilterPanel',
  props: {
    value: {
      type: Boolean,
      default: false
    },
    filters: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      visible: this.value,
      localFilters: {
        startDate: null,
        endDate: null,
        minAmount: null,
        maxAmount: null,
        type: null,
        categoryIds: [],
        memberIds: []
      },
      filterName: '',
      savedFilters: [],
      showDatePickerPopup: false,
      datePickerType: 'start',
      currentDate: new Date(),
      showCategoryPicker: false,
      categoryColumns: [],
      categories: [],
      showMemberPicker: false,
      memberColumns: [],
      members: []
    }
  },
  computed: {
    selectedCategoriesText() {
      if (!this.localFilters.categoryIds || this.localFilters.categoryIds.length === 0) {
        return '全部分类'
      }
      return `已选择 ${this.localFilters.categoryIds.length} 个分类`
    },
    selectedMembersText() {
      if (!this.localFilters.memberIds || this.localFilters.memberIds.length === 0) {
        return '全部成员'
      }
      return `已选择 ${this.localFilters.memberIds.length} 个成员`
    }
  },
  watch: {
    value(val) {
      this.visible = val
      if (val) {
        this.loadData()
      }
    },
    visible(val) {
      this.$emit('input', val)
    },
    filters: {
      handler(val) {
        this.localFilters = { ...val }
      },
      deep: true,
      immediate: true
    }
  },
  methods: {
    // 加载数据
    async loadData() {
      await Promise.all([
        this.loadSavedFilters(),
        this.loadCategories(),
        this.loadMembers()
      ])
    },
    // 加载保存的筛选条件
    async loadSavedFilters() {
      try {
        const res = await getSavedFilters()
        if (res.code === 200) {
          this.savedFilters = res.data || []
        }
      } catch (error) {
        console.error('加载筛选条件失败:', error)
      }
    },
    // 加载分类列表
    async loadCategories() {
      try {
        // 获取支出和收入分类
        const [expenseRes, incomeRes] = await Promise.all([
          categoryApi.getTree(1),
          categoryApi.getTree(2)
        ])
        
        const categories = []
        if (expenseRes.code === 200 && expenseRes.data) {
          categories.push(...expenseRes.data)
        }
        if (incomeRes.code === 200 && incomeRes.data) {
          categories.push(...incomeRes.data)
        }
        
        this.categories = categories
        this.categoryColumns = categories.map(cat => ({
          text: cat.name,
          value: cat.id
        }))
      } catch (error) {
        console.error('加载分类失败:', error)
      }
    },
    // 加载成员列表
    async loadMembers() {
      try {
        const res = await familyApi.getMembers()
        if (res.code === 200) {
          this.members = res.data || []
          this.memberColumns = this.members.map(member => ({
            text: member.nickname || member.phone,
            value: member.userId
          }))
        }
      } catch (error) {
        console.error('加载成员失败:', error)
      }
    },
    // 显示日期选择器
    showDatePicker(type) {
      this.datePickerType = type
      this.showDatePickerPopup = true
    },
    // 确认日期选择
    onDateConfirm(value) {
      const date = this.formatDate(value)
      if (this.datePickerType === 'start') {
        this.localFilters.startDate = date
      } else {
        this.localFilters.endDate = date
      }
      this.showDatePickerPopup = false
    },
    // 格式化日期
    formatDate(date) {
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    },
    // 确认分类选择
    onCategoryConfirm(value) {
      if (!this.localFilters.categoryIds) {
        this.localFilters.categoryIds = []
      }
      if (!this.localFilters.categoryIds.includes(value.value)) {
        this.localFilters.categoryIds.push(value.value)
      }
      this.showCategoryPicker = false
    },
    // 确认成员选择
    onMemberConfirm(value) {
      if (!this.localFilters.memberIds) {
        this.localFilters.memberIds = []
      }
      if (!this.localFilters.memberIds.includes(value.value)) {
        this.localFilters.memberIds.push(value.value)
      }
      this.showMemberPicker = false
    },
    // 保存当前筛选条件
    async saveCurrentFilter() {
      if (!this.filterName) {
        this.$toast('请输入筛选条件名称')
        return
      }
      
      try {
        const res = await saveFilter({
          name: this.filterName,
          filterCondition: this.localFilters
        })
        if (res.code === 200) {
          this.$toast.success('保存成功')
          this.filterName = ''
          await this.loadSavedFilters()
        }
      } catch (error) {
        this.$toast.fail('保存失败')
      }
    },
    // 应用保存的筛选条件
    async applySavedFilter(id) {
      try {
        const res = await applyFilter(id)
        if (res.code === 200) {
          this.localFilters = res.data
          this.$toast.success('已应用筛选条件')
        }
      } catch (error) {
        this.$toast.fail('应用失败')
      }
    },
    // 删除保存的筛选条件
    async deleteSavedFilter(id) {
      try {
        await this.$dialog.confirm({
          message: '确定删除该筛选条件吗？'
        })
        
        const res = await deleteFilter(id)
        if (res.code === 200) {
          this.$toast.success('删除成功')
          await this.loadSavedFilters()
        }
      } catch (error) {
        if (error !== 'cancel') {
          this.$toast.fail('删除失败')
        }
      }
    },
    // 重置筛选条件
    resetFilters() {
      this.localFilters = {
        startDate: null,
        endDate: null,
        minAmount: null,
        maxAmount: null,
        type: null,
        categoryIds: [],
        memberIds: []
      }
    },
    // 应用筛选条件
    applyFilters() {
      this.$emit('apply', this.localFilters)
      this.visible = false
    }
  }
}
</script>

<style scoped lang="scss">
.filter-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.filter-header {
  padding: 16px;
  border-bottom: 1px solid #ebedf0;
  
  h3 {
    margin: 0;
    font-size: 18px;
    font-weight: 500;
  }
}

.filter-content {
  flex: 1;
  overflow-y: auto;
  padding-bottom: 60px;
  
  .van-cell-group {
    margin-bottom: 12px;
  }
  
  .delete-icon {
    color: #ee0a24;
    font-size: 18px;
    margin-left: 8px;
  }
}

.filter-footer {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  gap: 12px;
  padding: 12px 16px;
  background: #fff;
  border-top: 1px solid #ebedf0;
  
  .van-button {
    flex: 1;
  }
}
</style>
