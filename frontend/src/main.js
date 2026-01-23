import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'

// Vant 组件按需引入
import {
  Badge,
  Button,
  Cell,
  CellGroup,
  Field,
  Form,
  Icon,
  Image,
  NavBar,
  Tabbar,
  TabbarItem,
  Tag,
  Toast,
  Dialog,
  Popup,
  Picker,
  DatetimePicker,
  List,
  PullRefresh,
  Tab,
  Tabs,
  ActionSheet,
  SwipeCell,
  Empty,
  Loading,
  Overlay,
  Search,
  Radio,
  RadioGroup
} from 'vant'

// 注册 Vant 组件
Vue.use(Badge)
Vue.use(Button)
Vue.use(Cell)
Vue.use(CellGroup)
Vue.use(Field)
Vue.use(Form)
Vue.use(Icon)
Vue.use(Image)
Vue.use(NavBar)
Vue.use(Tabbar)
Vue.use(TabbarItem)
Vue.use(Tag)
Vue.use(Toast)
Vue.use(Dialog)
Vue.use(Popup)
Vue.use(Picker)
Vue.use(DatetimePicker)
Vue.use(List)
Vue.use(PullRefresh)
Vue.use(Tab)
Vue.use(Tabs)
Vue.use(ActionSheet)
Vue.use(SwipeCell)
Vue.use(Empty)
Vue.use(Loading)
Vue.use(Overlay)
Vue.use(Search)
Vue.use(Radio)
Vue.use(RadioGroup)

Vue.config.productionTip = false

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')
