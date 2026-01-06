<template>
  <div class="category-manage-page">
    <van-nav-bar
      title="分类管理"
      left-arrow
      @click-left="$router.back()"
    >
      <template #right>
        <van-icon name="plus" size="20" @click="showAddDialog(null)" />
      </template>
    </van-nav-bar>

    <!-- 类型切换 -->
    <van-tabs v-model="activeType" @change="loadCategories">
      <van-tab title="支出" name="1" />
      <van-tab title="收入" name="2" />
    </van-tabs>

    <!-- 滑动操作提醒卡片 -->
    <div v-if="showSwipeTip" class="swipe-tip-card">
      <div class="tip-content">
        <van-icon name="bulb-o" class="tip-icon" />
        <span class="tip-text">💡 小贴士：向左滑动分类可进行编辑、删除或添加子分类操作</span>
      </div>
      <van-icon name="cross" class="close-icon" @click="hideSwipeTip" />
    </div>

    <!-- 分类列表 -->
    <div class="category-list">
      <van-empty v-if="!loading && categories.length === 0" description="暂无分类" />

      <div v-else class="category-tree">
        <div
          v-for="category in categories"
          :key="category.id"
          class="category-item"
        >
          <!-- 父分类 -->
          <van-swipe-cell>
            <van-cell
              :title="category.name"
              :icon="getDisplayIcon(category.icon)"
              is-link
              @click="toggleExpand(category)"
            >
              <template #right-icon>
                <van-icon
                  v-if="category.children && category.children.length"
                  :name="expandedIds.includes(category.id) ? 'arrow-down' : 'arrow'"
                />
              </template>
            </van-cell>
            <template #right>
              <van-button
                square
                type="primary"
                text="添加子分类"
                class="swipe-btn"
                @click="showAddDialog(category)"
              />
              <van-button
                square
                type="info"
                text="编辑"
                class="swipe-btn"
                @click="showEditDialog(category)"
              />
              <van-button
                square
                type="danger"
                text="删除"
                class="swipe-btn"
                @click="handleDelete(category)"
              />
            </template>
          </van-swipe-cell>

          <!-- 子分类 -->
          <div
            v-if="category.children && category.children.length && expandedIds.includes(category.id)"
            class="sub-categories"
          >
            <van-swipe-cell
              v-for="child in category.children"
              :key="child.id"
            >
              <van-cell
                :title="child.name"
                :icon="getDisplayIcon(child.icon)"
                class="sub-category-cell"
              />
              <template #right>
                <van-button
                  square
                  type="info"
                  text="编辑"
                  class="swipe-btn"
                  @click="showEditDialog(child)"
                />
                <van-button
                  square
                  type="danger"
                  text="删除"
                  class="swipe-btn"
                  @click="handleDelete(child)"
                />
              </template>
            </van-swipe-cell>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加/编辑分类弹窗 -->
    <van-dialog
      v-model="showDialog"
      :title="dialogTitle"
      show-cancel-button
      :before-close="handleDialogConfirm"
    >
      <van-field
        v-model="categoryForm.name"
        label="分类名称"
        placeholder="请输入分类名称"
        :rules="[{ required: true, message: '请输入分类名称' }]"
      />
      <van-cell
        title="选择图标"
        is-link
        @click="showIconPicker = true"
      >
        <template #default>
          <div class="icon-preview">
            <van-icon :name="getDisplayIcon(categoryForm.icon)" />
            <span class="icon-label">{{ categoryForm.icon ? '已选择' : '默认图标' }}</span>
          </div>
        </template>
      </van-cell>
    </van-dialog>

    <!-- 图标选择器 -->
    <icon-picker
      v-model="showIconPicker"
      :current-icon="categoryForm.icon"
      @select="handleIconSelect"
    />
  </div>
</template>

<script>
import { categoryApi } from '@/api'
import IconPicker from '@/components/IconPicker.vue'

export default {
  name: 'CategoryManage',
  components: {
    IconPicker
  },
  data () {
    return {
      loading: false,
      activeType: '1',
      categories: [],
      expandedIds: [],
      showDialog: false,
      showIconPicker: false,
      dialogMode: 'add', // add or edit
      parentCategory: null,
      showSwipeTip: true,
      categoryForm: {
        id: null,
        name: '',
        type: 1,
        parentId: null,
        icon: ''
      }
    }
  },
  computed: {
    dialogTitle () {
      if (this.dialogMode === 'edit') {
        return '编辑分类'
      }
      return this.parentCategory ? '添加子分类' : '添加分类'
    }
  },
  created () {
    // 检查是否已关闭过提示
    const tipClosed = localStorage.getItem('categorySwipeTipClosed')
    if (tipClosed === 'true') {
      this.showSwipeTip = false
    }
    this.loadCategories()
  },
  methods: {
    hideSwipeTip () {
      this.showSwipeTip = false
      localStorage.setItem('categorySwipeTipClosed', 'true')
    },
    async loadCategories () {
      this.loading = true
      try {
        const res = await categoryApi.getTree(this.activeType)
        if (res.code === 200) {
          this.categories = res.data || []
          // 更新store
          this.$store.dispatch('family/setCategories', this.categories)
        }
      } catch (error) {
        this.$toast.fail('加载失败')
      } finally {
        this.loading = false
      }
    },
    toggleExpand (category) {
      const index = this.expandedIds.indexOf(category.id)
      if (index > -1) {
        this.expandedIds.splice(index, 1)
      } else {
        this.expandedIds.push(category.id)
      }
    },
    showAddDialog (parent) {
      this.dialogMode = 'add'
      this.parentCategory = parent
      this.categoryForm = {
        id: null,
        name: '',
        type: parseInt(this.activeType),
        parentId: parent ? parent.id : null,
        icon: ''
      }
      this.showDialog = true
    },
    showEditDialog (category) {
      this.dialogMode = 'edit'
      this.parentCategory = null
      this.categoryForm = {
        id: category.id,
        name: category.name,
        type: category.type,
        parentId: category.parentId,
        icon: category.icon || ''
      }
      this.showDialog = true
    },
    async handleDialogConfirm (action, done) {
      if (action === 'confirm') {
        if (!this.categoryForm.name.trim()) {
          this.$toast('请输入分类名称')
          done(false)
          return
        }
        try {
          let res
          if (this.dialogMode === 'add') {
            res = await categoryApi.create({
              name: this.categoryForm.name,
              type: this.categoryForm.type,
              parentId: this.categoryForm.parentId,
              icon: this.categoryForm.icon || null
            })
          } else {
            res = await categoryApi.update(this.categoryForm.id, {
              name: this.categoryForm.name,
              icon: this.categoryForm.icon || null
            })
          }
          if (res.code === 200) {
            this.$toast.success(this.dialogMode === 'add' ? '添加成功' : '修改成功')
            done()
            await this.loadCategories()
          } else {
            this.$toast.fail(res.message || '操作失败')
            done(false)
          }
        } catch (error) {
          this.$toast.fail('操作失败')
          done(false)
        }
      } else {
        done()
      }
    },
    async handleDelete (category) {
      try {
        await this.$dialog.confirm({
          title: '提示',
          message: `确定要删除分类"${category.name}"吗？`
        })
        const res = await categoryApi.delete(category.id)
        if (res.code === 200) {
          this.$toast.success('删除成功')
          await this.loadCategories()
        } else {
          this.$toast.fail(res.message || '删除失败')
        }
      } catch (error) {
        // 用户取消或请求失败
        if (error !== 'cancel' && error.message !== 'cancel') {
          this.$toast.fail('删除失败')
        }
      }
    },
    handleIconSelect (iconId) {
      this.categoryForm.icon = iconId
    },
    getDisplayIcon (iconId) {
      if (!iconId) {
        return 'label-o'
      }
      // 自定义图标映射到Vant图标
      const customIconMap = {
        // 餐饮类
        food: 'shop-o',
        breakfast: 'hot-o',
        lunch: 'shop-o',
        dinner: 'shop-o',
        snack: 'gift-o',
        drink: 'hot-o',
        coffee: 'hot-o',
        cake: 'gift-o',
        hotpot: 'fire-o',
        fruit: 'flower-o',
        noodle: 'shop-o',
        rice: 'shop-o',
        bbq: 'fire-o',
        seafood: 'shop-o',
        fastfood: 'shop-o',
        // 交通类
        transport: 'logistics',
        bus: 'logistics',
        subway: 'logistics',
        taxi: 'logistics',
        fuel: 'fire-o',
        parking: 'location-o',
        car: 'logistics',
        bike: 'logistics',
        train: 'logistics',
        plane: 'logistics',
        // 购物类
        shopping: 'cart-o',
        daily: 'cart-o',
        clothing: 'bag-o',
        digital: 'tv-o',
        appliance: 'tv-o',
        cosmetic: 'gift-o',
        jewelry: 'diamond-o',
        // 居住类
        home: 'wap-home-o',
        rent: 'wap-home-o',
        utility: 'bulb-o',
        property: 'wap-home-o',
        repair: 'setting-o',
        furniture: 'wap-home-o',
        decoration: 'brush-o',
        cleaning: 'brush-o',
        security: 'shield-o',
        // 娱乐类
        entertainment: 'play-circle-o',
        movie: 'video-o',
        game: 'play-circle-o',
        travel: 'map-marked',
        sport: 'medal-o',
        reading: 'notes-o',
        ktv: 'volume-o',
        bar: 'hot-o',
        // 医疗类
        medical: 'tosend',
        medicine: 'tosend',
        registration: 'orders-o',
        checkup: 'search',
        healthcare: 'tosend',
        hospital: 'tosend',
        dental: 'smile-o',
        eye: 'eye-o',
        vaccine: 'tosend',
        insurance: 'shield-o',
        surgery: 'tosend',
        therapy: 'tosend',
        tcm: 'tosend',
        firstaid: 'tosend',
        pregnancy: 'friends-o',
        // 教育类
        education: 'notes-o',
        tuition: 'gold-coin-o',
        book: 'notes-o',
        training: 'notes-o',
        stationery: 'edit',
        course: 'notes-o',
        exam: 'todo-list-o',
        certificate: 'certificate',
        school: 'wap-home-o',
        library: 'notes-o',
        // 通讯类
        communication: 'phone-o',
        internet: 'cluster-o',
        membership: 'vip-card-o',
        sms: 'chat-o',
        email: 'envelop-o',
        cloud: 'cluster-o',
        software: 'apps-o',
        subscription: 'vip-card-o',
        // 收入类
        salary: 'gold-coin-o',
        bonus: 'gold-coin-o',
        investment: 'chart-trending-o',
        parttime: 'gold-coin-o',
        dividend: 'gold-coin-o',
        interest: 'gold-coin-o',
        rental: 'wap-home-o',
        refund: 'gold-coin-o',
        'gift-income': 'gift-o',
        lottery: 'gift-o',
        // 其他
        other: 'label-o'
      }
      // 如果是Vant图标（包含-o后缀），直接返回
      if (iconId.includes('-o') || iconId.includes('-')) {
        return iconId
      }
      return customIconMap[iconId] || 'label-o'
    }
  }
}
</script>

<style lang="scss" scoped>
.category-manage-page {
  min-height: 100%;
  background-color: #f7f8fa;

  .swipe-tip-card {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin: 12px;
    padding: 12px 16px;
    background: linear-gradient(135deg, #e8f4fd 0%, #f0f7ff 100%);
    border-radius: 8px;
    border: 1px solid #d4e8f7;
    animation: slideIn 0.3s ease-out;

    .tip-content {
      display: flex;
      align-items: center;
      flex: 1;

      .tip-icon {
        font-size: 18px;
        color: #1989fa;
        margin-right: 8px;
      }

      .tip-text {
        font-size: 13px;
        color: #666;
        line-height: 1.4;
      }
    }

    .close-icon {
      font-size: 16px;
      color: #999;
      padding: 4px;
      cursor: pointer;

      &:active {
        color: #666;
      }
    }
  }

  @keyframes slideIn {
    from {
      opacity: 0;
      transform: translateY(-10px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  .category-list {
    padding-top: 12px;
  }

  .category-tree {
    .category-item {
      margin-bottom: 1px;
    }

    .sub-categories {
      background-color: #f7f8fa;

      .sub-category-cell {
        padding-left: 40px;
        background-color: #fff;
      }
    }
  }

  .swipe-btn {
    height: 100%;
  }

  .icon-preview {
    display: flex;
    align-items: center;
    color: #969799;

    .van-icon {
      font-size: 20px;
      margin-right: 4px;
      color: #323233;
    }

    .icon-label {
      font-size: 14px;
    }
  }
}
</style>
