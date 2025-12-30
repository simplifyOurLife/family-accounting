<template>
  <div class="profile-page">
    <!-- 用户信息卡片 -->
    <div class="user-card">
      <div class="avatar">
        <van-image
          round
          width="60"
          height="60"
          :src="userInfo.avatar || defaultAvatar"
          fit="cover"
        />
      </div>
      <div class="user-info">
        <div class="nickname">{{ userInfo.nickname || userInfo.phone }}</div>
        <div class="phone">{{ userInfo.phone }}</div>
      </div>
      <van-icon name="arrow" class="arrow" @click="goToEditProfile" />
    </div>

    <!-- 家庭信息 -->
    <van-cell-group class="menu-group" title="家庭">
      <van-cell
        v-if="hasFamily"
        :title="familyInfo.name"
        is-link
        @click="goToFamily"
      >
        <template #label>
          <span class="family-role">{{ isAdmin ? '管理员' : '成员' }}</span>
        </template>
      </van-cell>
      <van-cell
        v-else
        title="创建/加入家庭"
        is-link
        @click="goToFamily"
      />
      <van-cell
        title="待处理邀请"
        is-link
        :value="pendingInvitationsCount > 0 ? pendingInvitationsCount + '条' : ''"
        @click="goToInvitations"
      >
        <template #right-icon>
          <van-badge :content="pendingInvitationsCount > 0 ? pendingInvitationsCount : ''" :show-zero="false">
            <van-icon name="arrow" />
          </van-badge>
        </template>
      </van-cell>
    </van-cell-group>

    <!-- 账本和分类管理 -->
    <van-cell-group class="menu-group" title="管理" v-if="hasFamily">
      <van-cell title="分类管理" is-link @click="goToCategories" />
      <van-cell title="账本管理" is-link @click="goToAccountBooks" />
    </van-cell-group>

    <!-- 设置 -->
    <van-cell-group class="menu-group" title="设置">
      <van-cell title="修改密码" is-link @click="goToChangePassword" />
      <van-cell title="退出登录" is-link @click="handleLogout" />
    </van-cell-group>
  </div>
</template>

<script>
import { mapGetters, mapActions } from 'vuex'
import { authApi, familyApi } from '@/api'

export default {
  name: 'Profile',
  data () {
    return {
      defaultAvatar: 'https://img.yzcdn.cn/vant/cat.jpeg',
      pendingInvitationsCount: 0
    }
  },
  computed: {
    ...mapGetters('user', ['userInfo']),
    ...mapGetters('family', ['hasFamily']),
    familyInfo () {
      return this.$store.state.family.familyInfo || {}
    },
    isAdmin () {
      return this.familyInfo.adminId === this.userInfo?.id
    }
  },
  created () {
    this.loadData()
  },
  methods: {
    ...mapActions('user', ['logout']),
    ...mapActions('family', ['clearFamily']),
    async loadData () {
      try {
        // 获取用户信息
        const userRes = await authApi.getInfo()
        if (userRes.code === 200) {
          this.$store.dispatch('user/setUserInfo', userRes.data)
        }
        // 获取家庭信息
        const familyRes = await familyApi.getInfo()
        if (familyRes.code === 200 && familyRes.data) {
          this.$store.dispatch('family/setFamilyInfo', familyRes.data)
        }
        // 获取待处理邀请数量
        await this.loadInvitations()
      } catch (error) {
        console.error('加载数据失败', error)
      }
    },
    async loadInvitations () {
      try {
        const res = await familyApi.getInvitations()
        if (res.code === 200) {
          this.pendingInvitationsCount = (res.data || []).filter(inv => inv.status === 0).length
        }
      } catch (error) {
        console.error('加载邀请失败', error)
      }
    },
    goToEditProfile () {
      this.$router.push('/profile/edit')
    },
    goToFamily () {
      this.$router.push('/profile/family')
    },
    goToInvitations () {
      this.$router.push('/profile/invitations')
    },
    goToCategories () {
      this.$router.push('/profile/categories')
    },
    goToAccountBooks () {
      this.$router.push('/profile/account-books')
    },
    goToChangePassword () {
      this.$router.push('/profile/password')
    },
    async handleLogout () {
      try {
        await this.$dialog.confirm({
          title: '提示',
          message: '确定要退出登录吗？'
        })
        await authApi.logout()
        this.logout()
        this.clearFamily()
        localStorage.removeItem('token')
        this.$router.push('/login')
      } catch (error) {
        // 用户取消
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.profile-page {
  min-height: 100%;
  background-color: #f7f8fa;
  padding-bottom: 20px;

  .user-card {
    display: flex;
    align-items: center;
    padding: 20px 16px;
    background-color: #fff;
    margin-bottom: 12px;

    .avatar {
      margin-right: 12px;
    }

    .user-info {
      flex: 1;

      .nickname {
        font-size: 18px;
        font-weight: 500;
        color: #323233;
        margin-bottom: 4px;
      }

      .phone {
        font-size: 14px;
        color: #969799;
      }
    }

    .arrow {
      color: #969799;
      font-size: 16px;
    }
  }

  .menu-group {
    margin-bottom: 12px;

    .family-role {
      font-size: 12px;
      color: #1989fa;
    }
  }
}
</style>
