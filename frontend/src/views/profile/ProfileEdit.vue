<template>
  <div class="profile-edit-page">
    <van-nav-bar
      title="编辑资料"
      left-arrow
      @click-left="$router.back()"
    />

    <div class="avatar-section">
      <div class="avatar-wrapper" @click="handleAvatarClick">
        <van-image
          round
          width="80"
          height="80"
          :src="form.avatar || defaultAvatar"
          fit="cover"
        />
        <div class="avatar-edit-icon">
          <van-icon name="photograph" />
        </div>
      </div>
      <p class="avatar-tip">点击更换头像</p>
    </div>

    <van-form @submit="onSubmit" class="profile-form">
      <van-cell-group inset>
        <van-field
          v-model="form.nickname"
          name="nickname"
          label="昵称"
          placeholder="请输入昵称"
          :rules="[{ required: true, message: '请输入昵称' }]"
          maxlength="20"
          show-word-limit
        />
        <van-field
          :value="userInfo.phone"
          name="phone"
          label="手机号"
          readonly
          disabled
        />
      </van-cell-group>

      <div class="submit-btn">
        <van-button round block type="primary" native-type="submit" :loading="submitting">
          保存
        </van-button>
      </div>
    </van-form>

    <!-- 头像上传（隐藏的input） -->
    <input
      ref="avatarInput"
      type="file"
      accept="image/*"
      style="display: none;"
      @change="handleAvatarChange"
    />
  </div>
</template>

<script>
import { mapGetters, mapActions } from 'vuex'
import { userApi } from '@/api'

export default {
  name: 'ProfileEdit',
  data () {
    return {
      defaultAvatar: 'https://img.yzcdn.cn/vant/cat.jpeg',
      form: {
        nickname: '',
        avatar: ''
      },
      submitting: false
    }
  },
  computed: {
    ...mapGetters('user', ['userInfo'])
  },
  created () {
    this.initForm()
  },
  methods: {
    ...mapActions('user', ['setUserInfo']),
    initForm () {
      if (this.userInfo) {
        this.form.nickname = this.userInfo.nickname || ''
        this.form.avatar = this.userInfo.avatar || ''
      }
    },
    handleAvatarClick () {
      this.$refs.avatarInput.click()
    },
    handleAvatarChange (event) {
      const file = event.target.files[0]
      if (!file) return

      // 检查文件大小（限制2MB）
      if (file.size > 2 * 1024 * 1024) {
        this.$toast('图片大小不能超过2MB')
        return
      }

      // 将图片转为base64预览（实际项目中应上传到服务器）
      const reader = new FileReader()
      reader.onload = (e) => {
        this.form.avatar = e.target.result
      }
      reader.readAsDataURL(file)

      // 清空input，允许重复选择同一文件
      event.target.value = ''
    },
    async onSubmit () {
      this.submitting = true
      try {
        const res = await userApi.updateProfile({
          nickname: this.form.nickname,
          avatar: this.form.avatar
        })
        if (res.code === 200) {
          this.$toast.success('保存成功')
          // 更新store中的用户信息
          this.setUserInfo({
            ...this.userInfo,
            nickname: this.form.nickname,
            avatar: this.form.avatar
          })
          this.$router.back()
        } else {
          this.$toast.fail(res.message || '保存失败')
        }
      } catch (error) {
        this.$toast.fail('保存失败')
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.profile-edit-page {
  min-height: 100%;
  background-color: #f7f8fa;

  .avatar-section {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 30px 0;
    background-color: #fff;
    margin-bottom: 12px;

    .avatar-wrapper {
      position: relative;
      cursor: pointer;

      .avatar-edit-icon {
        position: absolute;
        bottom: 0;
        right: 0;
        width: 24px;
        height: 24px;
        background-color: #1989fa;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
        font-size: 14px;
      }
    }

    .avatar-tip {
      margin-top: 8px;
      font-size: 12px;
      color: #969799;
    }
  }

  .profile-form {
    .submit-btn {
      padding: 20px 16px;
    }
  }
}
</style>
