<template>
  <div class="invitations-page">
    <van-nav-bar
      title="邀请处理"
      left-arrow
      @click-left="$router.back()"
    />

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <!-- 空状态 -->
      <van-empty v-if="!loading && invitations.length === 0" description="暂无邀请" />

      <!-- 邀请列表 -->
      <div v-else class="invitation-list">
        <van-cell-group>
          <van-cell
            v-for="invitation in invitations"
            :key="invitation.id"
            :title="invitation.familyName"
            :label="'邀请人: ' + (invitation.inviterNickname || invitation.inviterPhone)"
            center
          >
            <template #right-icon>
              <div class="invitation-actions">
                <!-- 待处理状态 -->
                <template v-if="invitation.status === 0">
                  <van-button
                    size="small"
                    type="primary"
                    :loading="processingId === invitation.id && processingAction === 'accept'"
                    @click="handleAccept(invitation)"
                  >
                    接受
                  </van-button>
                  <van-button
                    size="small"
                    :loading="processingId === invitation.id && processingAction === 'decline'"
                    @click="handleDecline(invitation)"
                  >
                    拒绝
                  </van-button>
                </template>
                <!-- 已处理状态 -->
                <template v-else>
                  <van-tag :type="getStatusType(invitation.status)">
                    {{ getStatusText(invitation.status) }}
                  </van-tag>
                </template>
              </div>
            </template>
            <template #label>
              <div class="invitation-info">
                <span>邀请人: {{ invitation.inviterNickname || invitation.inviterPhone }}</span>
                <span class="time">{{ formatTime(invitation.createdAt) }}</span>
              </div>
            </template>
          </van-cell>
        </van-cell-group>
      </div>
    </van-pull-refresh>
  </div>
</template>

<script>
import { familyApi } from '@/api'

export default {
  name: 'Invitations',
  data () {
    return {
      loading: false,
      refreshing: false,
      invitations: [],
      processingId: null,
      processingAction: null
    }
  },
  created () {
    this.loadInvitations()
  },
  methods: {
    async loadInvitations () {
      this.loading = true
      try {
        const res = await familyApi.getInvitations()
        if (res.code === 200) {
          this.invitations = res.data || []
        }
      } catch (error) {
        this.$toast.fail('加载失败')
      } finally {
        this.loading = false
      }
    },
    async onRefresh () {
      await this.loadInvitations()
      this.refreshing = false
    },
    async handleAccept (invitation) {
      this.processingId = invitation.id
      this.processingAction = 'accept'
      try {
        const res = await familyApi.acceptInvitation(invitation.id)
        if (res.code === 200) {
          this.$toast.success('已加入家庭')
          // 更新家庭信息
          const familyRes = await familyApi.getInfo()
          if (familyRes.code === 200) {
            this.$store.dispatch('family/setFamilyInfo', familyRes.data)
          }
          await this.loadInvitations()
        } else {
          this.$toast.fail(res.message || '操作失败')
        }
      } catch (error) {
        this.$toast.fail('操作失败')
      } finally {
        this.processingId = null
        this.processingAction = null
      }
    },
    async handleDecline (invitation) {
      try {
        await this.$dialog.confirm({
          title: '提示',
          message: '确定要拒绝这个邀请吗？'
        })
        this.processingId = invitation.id
        this.processingAction = 'decline'
        const res = await familyApi.declineInvitation(invitation.id)
        if (res.code === 200) {
          this.$toast.success('已拒绝')
          await this.loadInvitations()
        } else {
          this.$toast.fail(res.message || '操作失败')
        }
      } catch (error) {
        // 用户取消或请求失败
        if (error !== 'cancel') {
          this.$toast.fail('操作失败')
        }
      } finally {
        this.processingId = null
        this.processingAction = null
      }
    },
    getStatusType (status) {
      const types = {
        1: 'success',
        2: 'default',
        3: 'warning'
      }
      return types[status] || 'default'
    },
    getStatusText (status) {
      const texts = {
        0: '待处理',
        1: '已接受',
        2: '已拒绝',
        3: '已过期'
      }
      return texts[status] || '未知'
    },
    formatTime (dateStr) {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      const now = new Date()
      const diff = now - date
      const days = Math.floor(diff / (1000 * 60 * 60 * 24))
      if (days === 0) {
        return '今天'
      } else if (days === 1) {
        return '昨天'
      } else if (days < 7) {
        return `${days}天前`
      } else {
        return `${date.getMonth() + 1}月${date.getDate()}日`
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.invitations-page {
  min-height: 100%;
  background-color: #f7f8fa;

  .invitation-list {
    padding-top: 12px;
  }

  .invitation-actions {
    display: flex;
    gap: 8px;
  }

  .invitation-info {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 12px;
    color: #969799;

    .time {
      margin-left: 12px;
    }
  }
}
</style>
