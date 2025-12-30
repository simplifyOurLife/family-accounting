<template>
  <div class="layout">
    <div class="content">
      <router-view />
    </div>
    <van-tabbar v-model="active" route>
      <van-tabbar-item to="/transaction" icon="edit">记账</van-tabbar-item>
      <van-tabbar-item to="/statistics" icon="chart-trending-o">统计</van-tabbar-item>
      <van-tabbar-item to="/profile" icon="user-o">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script>
export default {
  name: 'Layout',
  data () {
    return {
      active: 0
    }
  }
}
</script>

<style lang="scss" scoped>
.layout {
  height: 100%;
  display: flex;
  flex-direction: column;

  .content {
    flex: 1;
    overflow-y: auto;
    padding-bottom: 50px;
  }
}
</style>
