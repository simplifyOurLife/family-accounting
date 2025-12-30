<template>
  <div class="login-page">
    <div class="login-header">
      <h1 class="title">家庭记账</h1>
      <p class="subtitle">记录生活每一笔</p>
    </div>

    <van-form @submit="onSubmit" class="login-form">
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
        placeholder="请输入密码"
        :type="showPassword ? 'text' : 'password'"
        :right-icon="showPassword ? 'eye-o' : 'closed-eye'"
        @click-right-icon="showPassword = !showPassword"
        :rules="[
          { required: true, message: '请输入密码' },
          { validator: validatePassword, message: '密码长度至少6位' }
        ]"
      />
      <div class="login-actions">
        <van-button
          round
          block
          type="primary"
          native-type="submit"
          :loading="loading"
          loading-text="登录中..."
        >
          登录
        </van-button>
      </div>
    </van-form>

    <div class="login-footer">
      <span>还没有账号？</span>
      <router-link to="/register" class="register-link">立即注册</router-link>
    </div>
  </div>
</template>

<script>
import { mapActions } from 'vuex'
import authApi from '@/api/modules/auth'
import { familyApi } from '@/api'
import { Toast } from 'vant'

export default {
  name: 'Login',
  data () {
    return {
      form: {
        phone: '',
        password: ''
      },
      showPassword: false,
      loading: false
    }
  },
  methods: {
    ...mapActions('user', ['setToken', 'setUserInfo']),
    ...mapActions('family', ['setFamilyInfo']),
    validatePassword (val) {
      return val && val.length >= 6
    },
    async onSubmit () {
      this.loading = true
      try {
        const res = await authApi.login(this.form)
        if (res.code === 200) {
          this.setToken(res.data.token)
          // 获取用户信息
          const userRes = await authApi.getInfo()
          if (userRes.code === 200) {
            this.setUserInfo(userRes.data)
          }
          // 获取家庭信息
          try {
            const familyRes = await familyApi.getInfo()
            if (familyRes.code === 200 && familyRes.data) {
              this.setFamilyInfo(familyRes.data)
            }
          } catch (error) {
            // 用户可能还没有家庭，这是正常的
            console.log('User has no family yet')
          }
          Toast.success('登录成功')
          this.$router.push('/')
        } else {
          Toast.fail(res.message || '登录失败')
        }
      } catch (error) {
        Toast.fail(error.message || '登录失败，请稍后重试')
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  padding: 60px 24px 24px;
  background: linear-gradient(180deg, #f0f7ff 0%, #ffffff 100%);
}

.login-header {
  text-align: center;
  margin-bottom: 48px;

  .title {
    font-size: 28px;
    font-weight: 600;
    color: #323233;
    margin: 0 0 8px;
  }

  .subtitle {
    font-size: 14px;
    color: #969799;
    margin: 0;
  }
}

.login-form {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.login-actions {
  margin-top: 24px;
  padding: 0 16px;
}

.login-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #969799;

  .register-link {
    color: #1989fa;
    text-decoration: none;
    margin-left: 4px;
  }
}
</style>
