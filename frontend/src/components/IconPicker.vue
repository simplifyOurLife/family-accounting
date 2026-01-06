<template>
  <van-popup
    v-model="visible"
    position="bottom"
    round
    :style="{ height: '70%' }"
    @close="handleClose"
  >
    <div class="icon-picker">
      <div class="picker-header">
        <span class="title">选择图标</span>
        <van-icon name="cross" class="close-btn" @click="handleClose" />
      </div>

      <!-- 分类标签页 -->
      <van-tabs v-model="activeTab" swipeable animated>
        <van-tab
          v-for="category in iconCategories"
          :key="category.category"
          :title="category.category"
        >
          <div class="icon-grid">
            <!-- 默认图标选项 -->
            <div
              v-if="category.category === '其他'"
              class="icon-item"
              :class="{ active: selectedIcon === '' }"
              @click="selectIcon('')"
            >
              <van-icon name="label-o" class="icon" />
              <span class="icon-name">默认</span>
            </div>
            <!-- 图标列表 -->
            <div
              v-for="icon in category.icons"
              :key="icon.id"
              class="icon-item"
              :class="{ active: selectedIcon === icon.id }"
              @click="selectIcon(icon.id)"
            >
              <van-icon :name="getIconName(icon)" class="icon" />
              <span class="icon-name">{{ icon.name }}</span>
            </div>
          </div>
        </van-tab>
      </van-tabs>

      <!-- 确认按钮 -->
      <div class="picker-footer">
        <van-button type="primary" block round @click="confirmSelect">
          确认选择
        </van-button>
      </div>
    </div>
  </van-popup>
</template>

<script>
import { categoryApi } from '@/api'

export default {
  name: 'IconPicker',
  props: {
    value: {
      type: Boolean,
      default: false
    },
    currentIcon: {
      type: String,
      default: ''
    }
  },
  data () {
    return {
      visible: false,
      activeTab: 0,
      iconCategories: [],
      selectedIcon: '',
      loading: false
    }
  },
  watch: {
    value (val) {
      this.visible = val
      if (val) {
        this.selectedIcon = this.currentIcon || ''
        this.loadIcons()
      }
    },
    visible (val) {
      this.$emit('input', val)
    }
  },
  methods: {
    async loadIcons () {
      if (this.iconCategories.length > 0) return
      
      this.loading = true
      try {
        const res = await categoryApi.getIcons()
        if (res.code === 200) {
          this.iconCategories = res.data || []
        }
      } catch (error) {
        this.$toast.fail('加载图标失败')
      } finally {
        this.loading = false
      }
    },
    getIconName (icon) {
      // Vant图标直接使用，自定义图标使用映射
      if (icon.type === 'vant') {
        return icon.id
      }
      // 自定义图标映射到Vant图标或使用默认
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
      return customIconMap[icon.id] || 'label-o'
    },
    selectIcon (iconId) {
      this.selectedIcon = iconId
    },
    confirmSelect () {
      this.$emit('select', this.selectedIcon)
      this.handleClose()
    },
    handleClose () {
      this.visible = false
    }
  }
}
</script>

<style lang="scss" scoped>
.icon-picker {
  height: 100%;
  display: flex;
  flex-direction: column;

  .picker-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px;
    border-bottom: 1px solid #ebedf0;

    .title {
      font-size: 16px;
      font-weight: 500;
      color: #323233;
    }

    .close-btn {
      font-size: 20px;
      color: #969799;
      cursor: pointer;
    }
  }

  .van-tabs {
    flex: 1;
    overflow: hidden;

    ::v-deep .van-tabs__content {
      height: calc(100% - 44px);
      overflow-y: auto;
    }
  }

  .icon-grid {
    display: grid;
    grid-template-columns: repeat(5, 1fr);
    gap: 8px;
    padding: 16px;

    .icon-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 12px 4px;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.2s;

      &:active {
        background-color: #f2f3f5;
      }

      &.active {
        background-color: #e8f4ff;
        border: 1px solid #1989fa;

        .icon {
          color: #1989fa;
        }

        .icon-name {
          color: #1989fa;
        }
      }

      .icon {
        font-size: 24px;
        color: #323233;
        margin-bottom: 4px;
      }

      .icon-name {
        font-size: 11px;
        color: #646566;
        text-align: center;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        max-width: 100%;
      }
    }
  }

  .picker-footer {
    padding: 12px 16px;
    border-top: 1px solid #ebedf0;
  }
}
</style>
