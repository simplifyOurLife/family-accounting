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
import { Toast, Dialog } from 'vant'

export default {
  name: 'Login',
  data () {
    return {
      form: {
        phone: '',
        password: '',
        captchaCode: ''
      },
      captchaKey: '',
      captchaImage: '',
      showPassword: false,
      loading: false
    }
  },
  mounted () {
    this.refreshCaptcha()
  },
  methods: {
    ...mapActions('user', ['setToken', 'setUserInfo']),
    ...mapActions('family', ['setFamilyInfo']),
    validatePassword (val) {
      return val && val.length >= 6
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
            message: '由于多次登录失败，您的账户已被临时锁定。请30分钟后再试，或联系客服解锁。',
            confirmButtonText: '我知道了'
          })
          this.refreshCaptcha()
          this.form.captchaCode = ''
          break
        case 1005:
          Dialog.alert({
            title: '访问受限',
            message: '您的请求过于频繁，系统已暂时限制访问。请稍后再试。',
            confirmButtonText: '我知道了'
          })
          break
        case 1007:
          Toast.fail('手机号或密码错误')
          this.refreshCaptcha()
          this.form.captchaCode = ''
          break
        default:
          Toast.fail(res.message || '操作失败')
          this.refreshCaptcha()
          this.form.captchaCode = ''
      }
    },
    handleError (error) {
      if (error.response && error.response.data) {
        this.handleErrorResponse(error.response.data)
      } else {
        Toast.fail(error.message || '网络错误，请稍后重试')
        // 刷新验证码
        this.refreshCaptcha()
        this.form.captchaCode = ''
      }
    },
    async onSubmit () {
      if (!this.captchaKey) {
        Toast.fail('请先获取验证码')
        this.refreshCaptcha()
        return
      }
      this.loading = true
      try {
        const res = await authApi.login({
          phone: this.form.phone,
          password: this.form.password,
          captchaKey: this.captchaKey,
          captchaCode: this.form.captchaCode
        })
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
