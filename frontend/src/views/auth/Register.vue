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
        <van-field
          v-model="form.captchaCode"
          name="captchaCode"
          label="验证码"
          placeholder="请输入图片验证码"
          maxlength="4"
          :rules="[
            { required: true, message: '请输入验证码' },
            { pattern: /^[a-zA-Z0-9]{4}$/, message: '验证码为4位字母或数字' }
          ]"
        >
          <template #button>
            <div class="captcha-wrapper" @click="refreshCaptcha">
              <img
                v-if="captchaImage"
                :src="captchaImage"
                alt="验证码"
                class="captcha-image"
              />
              <van-loading v-else size="20" class="captcha-loading" />
              <span class="captcha-tip">点击刷新</span>
            </div>
          </template>
        </van-field>
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
import { Toast, Dialog } from 'vant'

export default {
  name: 'Register',
  data () {
    return {
      form: {
        phone: '',
        password: '',
        confirmPassword: '',
        captchaCode: ''
      },
      captchaKey: '',
      captchaImage: '',
      showPassword: false,
      showConfirmPassword: false,
      loading: false
    }
  },
  mounted () {
    this.refreshCaptcha()
  },
  methods: {
    validatePassword (val) {
      return val && val.length >= 6
    },
    validateConfirmPassword (val) {
      return val === this.form.password
    },
    async refreshCaptcha () {
      this.captchaImage = ''
      try {
        const res = await authApi.getCaptcha()
        if (res.code === 200 && res.data) {
          this.captchaKey = res.data.captchaKey
          this.captchaImage = res.data.captchaImage
        } else {
          Toast.fail('获取验证码失败')
        }
      } catch (error) {
        Toast.fail('获取验证码失败，请重试')
      }
    },
    handleErrorResponse (res) {
      const errorCode = res.code
      switch (errorCode) {
        case 1001:
          Toast.fail('验证码错误，请重新输入')
          this.refreshCaptcha()
          this.form.captchaCode = ''
          break
        case 1002:
          Toast.fail('验证码已过期，请重新获取')
          this.refreshCaptcha()
          this.form.captchaCode = ''
          break
        case 1004:
          Dialog.alert({
            title: '账户已锁定',
            message: '由于多次操作失败，您的账户已被临时锁定。请30分钟后再试。',
            confirmButtonText: '我知道了'
          })
          break
        case 1005:
          Dialog.alert({
            title: '访问受限',
            message: '您的请求过于频繁，系统已暂时限制访问。请稍后再试。',
            confirmButtonText: '我知道了'
          })
          break
        case 1006:
          Toast.fail('该手机号已注册，请直接登录')
          break
        default:
          Toast.fail(res.message || '操作失败')
      }
    },
    handleError (error) {
      if (error.response && error.response.data) {
        this.handleErrorResponse(error.response.data)
      } else {
        Toast.fail(error.message || '网络错误，请稍后重试')
      }
      // 刷新验证码
      this.refreshCaptcha()
      this.form.captchaCode = ''
    },
    async onSubmit () {
      if (!this.captchaKey) {
        Toast.fail('请先获取验证码')
        this.refreshCaptcha()
        return
      }
      this.loading = true
      try {
        const res = await authApi.register({
          phone: this.form.phone,
          password: this.form.password,
          captchaKey: this.captchaKey,
          captchaCode: this.form.captchaCode
        })
        if (res.code === 200) {
          Toast.success('注册成功')
          this.$router.push('/login')
        } else {
          this.handleErrorResponse(res)
        }
      } catch (error) {
        this.handleError(error)
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

.captcha-wrapper {
  display: flex;
  align-items: center;
  cursor: pointer;
  min-width: 100px;
  height: 36px;
  
  .captcha-image {
    width: 100px;
    height: 36px;
    border-radius: 4px;
    border: 1px solid #ebedf0;
  }
  
  .captcha-loading {
    width: 100px;
    height: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f7f8fa;
    border-radius: 4px;
  }
  
  .captcha-tip {
    font-size: 12px;
    color: #969799;
    margin-left: 8px;
    white-space: nowrap;
  }
}
</style>
