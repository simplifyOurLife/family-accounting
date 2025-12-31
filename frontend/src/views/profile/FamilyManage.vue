<template>
  <div class="family-manage-page">
    <van-nav-bar
      title="家庭管理"
      left-arrow
      @click-left="$router.back()"
    />

    <!-- 无家庭状态 -->
    <div v-if="!hasFamily" class="no-family">
      <van-empty description="您还没有加入家庭">
        <van-button type="primary" @click="showCreateDialog = true">创建家庭</van-button>
      </van-empty>
    </div>

    <!-- 有家庭状态 -->
    <div v-else class="family-content">
      <!-- 家庭信息 -->
      <van-cell-group title="家庭信息">
        <van-cell title="家庭名称" :value="familyInfo.name" />
        <van-cell title="创建时间" :value="formatDate(familyInfo.createdAt)" />
        <van-cell title="成员数量" :value="members.length + '人'" />
      </van-cell-group>

      <!-- 成员列表 -->
      <van-cell-group title="家庭成员">
        <van-cell
          v-for="member in members"
          :key="member.id"
          :title="member.nickname || member.phone"
          :label="member.phone"
          center
        >
          <template #icon>
            <van-image
              round
              width="40"
              height="40"
              :src="member.avatar || defaultAvatar"
              style="margin-right: 12px;"
            />
          </template>
          <template #right-icon>
            <div class="member-actions">
              <van-tag v-if="member.userId === familyInfo.adminId" type="primary">管理员</van-tag>
              <template v-else-if="isAdmin">
                <van-button size="mini" @click="editNickname(member)">设置昵称</van-button>
                <van-button size="mini" type="danger" @click="removeMember(member)">移除</van-button>
              </template>
            </div>
          </template>
        </van-cell>
      </van-cell-group>

      <!-- 邀请成员（仅管理员可见） -->
      <div v-if="isAdmin" class="invite-section">
        <van-cell-group title="邀请成员">
          <van-field
            v-model="invitePhone"
            label="手机号"
            placeholder="请输入被邀请人手机号"
            type="tel"
            maxlength="11"
          />
          <div class="invite-btn-wrapper">
            <van-button type="primary" block @click="handleInvite" :loading="inviting">
              发送邀请
            </van-button>
          </div>
        </van-cell-group>
      </div>
    </div>

    <!-- 创建家庭弹窗 -->
    <van-dialog
      v-model="showCreateDialog"
      title="创建家庭"
      show-cancel-button
      :before-close="handleCreateFamily"
    >
      <van-field
        v-model="familyName"
        label="家庭名称"
        placeholder="请输入家庭名称"
        :rules="[{ required: true, message: '请输入家庭名称' }]"
      />
    </van-dialog>

    <!-- 设置昵称弹窗 -->
    <van-dialog
      v-model="showNicknameDialog"
      title="设置昵称"
      show-cancel-button
      :before-close="handleSetNickname"
    >
      <van-field
        v-model="memberNickname"
        label="昵称"
        placeholder="请输入成员昵称"
      />
    </van-dialog>
  </div>
</template>

<script>
import { mapGetters, mapActions } from 'vuex'
import { familyApi } from '@/api'

export default {
  name: 'FamilyManage',
  data () {
    return {
      defaultAvatar: 'https://img.yzcdn.cn/vant/cat.jpeg',
      members: [],
      showCreateDialog: false,
      familyName: '',
      invitePhone: '',
      inviting: false,
      showNicknameDialog: false,
      memberNickname: '',
      currentMember: null
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
    ...mapActions('family', ['setFamilyInfo', 'setMembers']),
    async loadData () {
      // 首先尝试获取家庭信息
      if (!this.hasFamily) {
        try {
          const res = await familyApi.getInfo()
          if (res.code === 200 && res.data) {
            this.setFamilyInfo(res.data)
          }
        } catch (error) {
          // 用户确实没有家庭
          console.log('User has no family')
        }
      }

      if (this.hasFamily) {
        await this.loadMembers()
      }
    },
    async loadMembers () {
      try {
        const res = await familyApi.getMembers()
        if (res.code === 200) {
          this.members = res.data || []
          this.setMembers(this.members)
        }
      } catch (error) {
        this.$toast.fail('加载成员失败')
      }
    },
    formatDate (dateStr) {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
    },
    async handleCreateFamily (action, done) {
      if (action === 'confirm') {
        if (!this.familyName.trim()) {
          this.$toast('请输入家庭名称')
          done(false)
          return
        }
        try {
          const res = await familyApi.create({ name: this.familyName })
          if (res.code === 200) {
            this.$toast.success('创建成功')
            this.setFamilyInfo(res.data)
            this.familyName = ''
            done()
            await this.loadMembers()
          } else {
            this.$toast.fail(res.message || '创建失败')
            done(false)
          }
        } catch (error) {
          this.$toast.fail('创建失败')
          done(false)
        }
      } else {
        done()
      }
    },
    async handleInvite () {
      if (!this.invitePhone.trim()) {
        this.$toast('请输入手机号')
        return
      }
      if (!/^1\d{10}$/.test(this.invitePhone)) {
        this.$toast('请输入正确的手机号')
        return
      }
      this.inviting = true
      try {
        const res = await familyApi.invite({ phone: this.invitePhone })
        if (res.code === 200) {
          this.$toast.success('邀请已发送')
          this.invitePhone = ''
        } else {
          this.$toast.fail(res.message || '邀请失败')
        }
      } catch (error) {
        this.$toast.fail('邀请失败')
      } finally {
        this.inviting = false
      }
    },
    editNickname (member) {
      this.currentMember = member
      this.memberNickname = member.nickname || ''
      this.showNicknameDialog = true
    },
    async handleSetNickname (action, done) {
      if (action === 'confirm') {
        try {
          const res = await familyApi.setMemberNickname(this.currentMember.id, {
            nickname: this.memberNickname
          })
          if (res.code === 200) {
            this.$toast.success('设置成功')
            done()
            await this.loadMembers()
          } else {
            this.$toast.fail(res.message || '设置失败')
            done(false)
          }
        } catch (error) {
          this.$toast.fail('设置失败')
          done(false)
        }
      } else {
        done()
      }
    },
    async removeMember (member) {
      try {
        await this.$dialog.confirm({
          title: '提示',
          message: `确定要移除成员 ${member.nickname || member.phone} 吗？`
        })
        const res = await familyApi.removeMember(member.id)
        if (res.code === 200) {
          this.$toast.success('移除成功')
          await this.loadMembers()
        } else {
          this.$toast.fail(res.message || '移除失败')
        }
      } catch (error) {
        // 用户取消
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.family-manage-page {
  min-height: 100%;
  background-color: #f7f8fa;

  .no-family {
    padding-top: 100px;
  }

  .family-content {
    padding-bottom: 20px;

    .van-cell-group {
      margin-bottom: 12px;
    }

    .member-actions {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .invite-section {
      .invite-btn-wrapper {
        padding: 12px 16px;
      }
    }
  }
}
</style>
