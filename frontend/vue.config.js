const { defineConfig } = require('@vue/cli-service')

module.exports = defineConfig({
  transpileDependencies: true,
  lintOnSave: false,
  devServer: {
    port: 6010,
    proxy: {
      '/api': {
        target: 'http://0.0.0.0:6009',
        changeOrigin: true,
        pathRewrite: {
          '^/api': '/api'
        }
      }
    }
  },
  css: {
    loaderOptions: {
      sass: {
        additionalData: '@import "@/assets/styles/variables.scss";'
      }
    }
  }
})
