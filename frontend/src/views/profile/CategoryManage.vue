<template>
  <div class="category-manage-page">
    <van-nav-bar
      title="分类管理"
      left-arrow
      @click-left="$router.back()"
    >
      <template #right>
        <van-icon name="plus" size="20" @click="showAddDialog(null)" />
      </template>
    </van-nav-bar>

    <!-- 类型切换 -->
    <van-tabs v-model="activeType" @change="loadCategories">
      <van-tab title="支出" name="1" />
      <van-tab title="收入" name="2" />
    </van-tabs>

    <!-- 分类列表 -->
    <div class="category-list">
      <van-empty v-if="!loading && categories.length === 0" description="暂无分类" />

      <div v-else class="category-tree">
        <div
          v-for="category in categories"
          :key="category.id"
          class="category-item"
        >
          <!-- 父分类 -->
          <van-swipe-cell>
            <van-cell
              :title="category.name"
              :icon="category.icon"
              is-link
              @click="toggleExpand(category)"
            >
              <template #right-icon>
                <van-icon
                  v-if="category.children && category.children.length"
                  :name="expandedIds.includes(category.id) ? 'arrow-down' : 'arrow'"
                />
              </template>
            </van-cell>
            <template #right>
              <van-button
                square
                type="primary"
                text="添加子分类"
                class="swipe-btn"
                @click="showAddDialog(category)"
              />
              <van-button
                square
                type="info"
                text="编辑"
                class="swipe-btn"
                @click="showEditDialog(category)"
              />
              <van-button
                square
                type="danger"
                text="删除"
                class="swipe-btn"
                @click="handleDelete(category)"
              />
            </template>
          </van-swipe-cell>

          <!-- 子分类 -->
          <div
            v-if="category.children && category.children.length && expandedIds.includes(category.id)"
            class="sub-categories"
          >
            <van-swipe-cell
              v-for="child in category.children"
              :key="child.id"
            >
              <van-cell
                :title="child.name"
                :icon="child.icon"
                class="sub-category-cell"
              />
              <template #right>
                <van-button
                  square
                  type="info"
                  text="编辑"
                  class="swipe-btn"
                  @click="showEditDialog(child)"
                />
                <van-button
                  square
                  type="danger"
                  text="删除"
                  class="swipe-btn"
                  @click="handleDelete(child)"
                />
              </template>
            </van-swipe-cell>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加/编辑分类弹窗 -->
    <van-dialog
      v-model="showDialog"
      :title="dialogTitle"
      show-cancel-button
      :before-close="handleDialogConfirm"
    >
      <van-field
        v-model="categoryForm.name"
        label="分类名称"
        placeholder="请输入分类名称"
        :rules="[{ required: true, message: '请输入分类名称' }]"
      />
    </van-dialog>
  </div>
</template>

<script>
import { categoryApi } from '@/api'

export default {
  name: 'CategoryManage',
  data () {
    return {
      loading: false,
      activeType: '1',
      categories: [],
      expandedIds: [],
      showDialog: false,
      dialogMode: 'add', // add or edit
      parentCategory: null,
      categoryForm: {
        id: null,
        name: '',
        type: 1,
        parentId: null
      }
    }
  },
  computed: {
    dialogTitle () {
      if (this.dialogMode === 'edit') {
        return '编辑分类'
      }
      return this.parentCategory ? '添加子分类' : '添加分类'
    }
  },
  created () {
    this.loadCategories()
  },
  methods: {
    async loadCategories () {
      this.loading = true
      try {
        const res = await categoryApi.getTree(this.activeType)
        if (res.code === 200) {
          this.categories = res.data || []
          // 更新store
          this.$store.dispatch('family/setCategories', this.categories)
        }
      } catch (error) {
        this.$toast.fail('加载失败')
      } finally {
        this.loading = false
      }
    },
    toggleExpand (category) {
      const index = this.expandedIds.indexOf(category.id)
      if (index > -1) {
        this.expandedIds.splice(index, 1)
      } else {
        this.expandedIds.push(category.id)
      }
    },
    showAddDialog (parent) {
      this.dialogMode = 'add'
      this.parentCategory = parent
      this.categoryForm = {
        id: null,
        name: '',
        type: parseInt(this.activeType),
        parentId: parent ? parent.id : null
      }
      this.showDialog = true
    },
    showEditDialog (category) {
      this.dialogMode = 'edit'
      this.parentCategory = null
      this.categoryForm = {
        id: category.id,
        name: category.name,
        type: category.type,
        parentId: category.parentId
      }
      this.showDialog = true
    },
    async handleDialogConfirm (action, done) {
      if (action === 'confirm') {
        if (!this.categoryForm.name.trim()) {
          this.$toast('请输入分类名称')
          done(false)
          return
        }
        try {
          let res
          if (this.dialogMode === 'add') {
            res = await categoryApi.create({
              name: this.categoryForm.name,
              type: this.categoryForm.type,
              parentId: this.categoryForm.parentId
            })
          } else {
            res = await categoryApi.update(this.categoryForm.id, {
              name: this.categoryForm.name
            })
          }
          if (res.code === 200) {
            this.$toast.success(this.dialogMode === 'add' ? '添加成功' : '修改成功')
            done()
            await this.loadCategories()
          } else {
            this.$toast.fail(res.message || '操作失败')
            done(false)
          }
        } catch (error) {
          this.$toast.fail('操作失败')
          done(false)
        }
      } else {
        done()
      }
    },
    async handleDelete (category) {
      try {
        await this.$dialog.confirm({
          title: '提示',
          message: `确定要删除分类"${category.name}"吗？`
        })
        const res = await categoryApi.delete(category.id)
        if (res.code === 200) {
          this.$toast.success('删除成功')
          await this.loadCategories()
        } else {
          this.$toast.fail(res.message || '删除失败')
        }
      } catch (error) {
        // 用户取消或请求失败
        if (error !== 'cancel' && error.message !== 'cancel') {
          this.$toast.fail('删除失败')
        }
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.category-manage-page {
  min-height: 100%;
  background-color: #f7f8fa;

  .category-list {
    padding-top: 12px;
  }

  .category-tree {
    .category-item {
      margin-bottom: 1px;
    }

    .sub-categories {
      background-color: #f7f8fa;

      .sub-category-cell {
        padding-left: 40px;
        background-color: #fff;
      }
    }
  }

  .swipe-btn {
    height: 100%;
  }
}
</style>
