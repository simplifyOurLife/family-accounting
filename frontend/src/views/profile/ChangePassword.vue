<template>
  <div class="change-password-page">
    <van-nav-bar
      title="修改密码"
      left-arrow
      @click-left="$router.back()"
    />

    <van-form @submit="onSubmit" class="password-form">
      <van-cell-group inset>
        <van-field
          v-model="form.oldPassword"
          type="password"
          name="oldPassword"
          label="旧密码"
          placeholder="请输入旧密码"
          :rules="[{ required: true, message: '请输入旧密码' }]"
        />
        <van-field
          v-model="form.newPassword"
          type="password"
          name="newPassword"
          label="新密码"
          placeholder="请输入新密码（6-20位）"
          :rules="[
            { required: true, message: '请输入新密码' },
            { pattern: /^.{6,20}$/, message: '密码长度为6-20位' }
          ]"
        />
        <van-field
          v-model="form.confirmPassword"
          type="password"
          name="confirmPassword"
          label="确认密码"
          placeholder="请再次输入新密码"
          :rules="[
            { required: true, message: '请确认新密码' },
            { validator: validateConfirmPassword, message: '两次输入的密码不一致' }
          ]"
        />
      </van-cell-group>

      <div class="submit-btn">
        <van-button round block type="primary" native-type="submit" :loading="submitting">
          确认修改
        </van-button>
      </div>
    </van-form>

    <div class="tips">
      <p>温馨提示：</p>
      <ul>
        <li>密码长度为6-20位</li>
        <li>修改密码后需要重新登录</li>
      </ul>
    </div>
  </div>
</template>

<script>
import { mapActions } from 'vuex'
import { userApi } from '@/api'

export default {
  name: 'ChangePassword',
  data () {
    return {
      form: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      submitting: false
    }
  },
  methods: {
    ...mapActions('user', ['logout']),
    ...mapActions('family', ['clearFamily']),
    validateConfirmPassword (value) {
      return value === this.form.newPassword
    },
    async onSubmit () {
      this.submitting = true
      try {
        const res = await userApi.updatePassword({
          oldPassword: this.form.oldPassword,
          newPassword: this.form.newPassword
        })
        if (res.code === 200) {
          this.$toast.success('密码修改成功，请重新登录')
          // 清除登录状态
          this.logout()
          this.clearFamily()
          localStorage.removeItem('token')
          // 跳转到登录页
          setTimeout(() => {
            this.$router.push('/login')
          }, 1500)
        } else {
          this.$toast.fail(res.message || '修改失败')
        }
      } catch (error) {
        this.$toast.fail('修改失败')
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.change-password-page {
  min-height: 100%;
  background-color: #f7f8fa;

  .password-form {
    padding-top: 20px;
  }

  .submit-btn {
    padding: 20px 16px;
  }

  .tips {
    padding: 0 16px;
    color: #969799;
    font-size: 12px;

    p {
      margin-bottom: 8px;
    }

    ul {
      padding-left: 16px;
      margin: 0;

      li {
        line-height: 1.8;
      }
    }
  }
}
</style>
