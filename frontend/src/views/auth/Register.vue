<template>
  <div class="register-page">
    <van-nav-bar
      title="注册账号"
      left-arrow
      @click-left="$router.back()"
      class="register-nav"
    />

    <div class="register-content">
      <van-form @submit="onSubmit" class="register-form">
        <van-field
          v-model="form.phone"
          name="phone"
          label="手机号"
          placeholder="请输入手机号"
          type="tel"
          maxlength="11"
          :rules="[
            { required: true, message: '请输入手机号' },
            { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确' }
          ]"
        />
        <van-field
          v-model="form.password"
          name="password"
          label="密码"
          placeholder="请输入密码（至少6位）"
          :type="showPassword ? 'text' : 'password'"
          :right-icon="showPassword ? 'eye-o' : 'closed-eye'"
          @click-right-icon="showPassword = !showPassword"
          :rules="[
            { required: true, message: '请输入密码' },
            { validator: validatePassword, message: '密码长度至少6位' }
          ]"
        />
        <van-field
          v-model="form.confirmPassword"
          name="confirmPassword"
          label="确认密码"
          placeholder="请再次输入密码"
          :type="showConfirmPassword ? 'text' : 'password'"
          :right-icon="showConfirmPassword ? 'eye-o' : 'closed-eye'"
          @click-right-icon="showConfirmPassword = !showConfirmPassword"
          :rules="[
            { required: true, message: '请确认密码' },
            { validator: validateConfirmPassword, message: '两次输入的密码不一致' }
          ]"
        />
        <div class="register-actions">
          <van-button
            round
            block
            type="primary"
            native-type="submit"
            :loading="loading"
            loading-text="注册中..."
          >
            注册
          </van-button>
        </div>
      </van-form>

      <div class="register-footer">
        <span>已有账号？</span>
        <router-link to="/login" class="login-link">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script>
import authApi from '@/api/modules/auth'
import { Toast } from 'vant'

export default {
  name: 'Register',
  data () {
    return {
      form: {
        phone: '',
        password: '',
        confirmPassword: ''
      },
      showPassword: false,
      showConfirmPassword: false,
      loading: false
    }
  },
  methods: {
    validatePassword (val) {
      return val && val.length >= 6
    },
    validateConfirmPassword (val) {
      return val === this.form.password
    },
    async onSubmit () {
      this.loading = true
      try {
        const res = await authApi.register({
          phone: this.form.phone,
          password: this.form.password
        })
        if (res.code === 200) {
          Toast.success('注册成功')
          this.$router.push('/login')
        } else {
          Toast.fail(res.message || '注册失败')
        }
      } catch (error) {
        Toast.fail(error.message || '注册失败，请稍后重试')
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.register-page {
  min-height: 100vh;
  background: #f7f8fa;
}

.register-nav {
  background: #fff;
}

.register-content {
  padding: 24px;
}

.register-form {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.register-actions {
  margin-top: 24px;
  padding: 0 16px;
}

.register-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #969799;

  .login-link {
    color: #1989fa;
    text-decoration: none;
    margin-left: 4px;
  }
}
</style>
