<template>
  <div class="account-book-manage-page">
    <van-nav-bar
      title="账本管理"
      left-arrow
      @click-left="$router.back()"
    >
      <template #right>
        <van-icon name="plus" size="20" @click="showAddDialog" />
      </template>
    </van-nav-bar>

    <!-- 账本列表 -->
    <div class="account-book-list">
      <van-empty v-if="!loading && accountBooks.length === 0" description="暂无账本" />

      <van-cell-group v-else>
        <van-swipe-cell
          v-for="book in accountBooks"
          :key="book.id"
        >
          <van-cell
            :title="book.name"
            center
            @click="handleSetDefault(book)"
          >
            <template #right-icon>
              <van-tag v-if="book.isDefault" type="primary">默认</van-tag>
            </template>
            <template #label>
              <span class="create-time">创建于 {{ formatDate(book.createdAt) }}</span>
            </template>
          </van-cell>
          <template #right>
            <van-button
              square
              type="info"
              text="编辑"
              class="swipe-btn"
              @click="showEditDialog(book)"
            />
            <van-button
              square
              type="danger"
              text="删除"
              class="swipe-btn"
              :disabled="book.isDefault"
              @click="handleDelete(book)"
            />
          </template>
        </van-swipe-cell>
      </van-cell-group>
    </div>

    <!-- 添加/编辑账本弹窗 -->
    <van-dialog
      v-model="showDialog"
      :title="dialogMode === 'add' ? '添加账本' : '编辑账本'"
      show-cancel-button
      :before-close="handleDialogConfirm"
    >
      <van-field
        v-model="bookForm.name"
        label="账本名称"
        placeholder="请输入账本名称"
        :rules="[{ required: true, message: '请输入账本名称' }]"
      />
    </van-dialog>
  </div>
</template>

<script>
import { mapActions } from 'vuex'
import { accountBookApi } from '@/api'

export default {
  name: 'AccountBookManage',
  data () {
    return {
      loading: false,
      accountBooks: [],
      showDialog: false,
      dialogMode: 'add',
      bookForm: {
        id: null,
        name: ''
      }
    }
  },
  created () {
    this.loadAccountBooks()
  },
  methods: {
    ...mapActions('family', ['setAccountBooks']),
    async loadAccountBooks () {
      this.loading = true
      try {
        const res = await accountBookApi.getList()
        if (res.code === 200) {
          this.accountBooks = res.data || []
          this.setAccountBooks(this.accountBooks)
        }
      } catch (error) {
        this.$toast.fail('加载失败')
      } finally {
        this.loading = false
      }
    },
    formatDate (dateStr) {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
    },
    showAddDialog () {
      this.dialogMode = 'add'
      this.bookForm = {
        id: null,
        name: ''
      }
      this.showDialog = true
    },
    showEditDialog (book) {
      this.dialogMode = 'edit'
      this.bookForm = {
        id: book.id,
        name: book.name
      }
      this.showDialog = true
    },
    async handleDialogConfirm (action, done) {
      if (action === 'confirm') {
        if (!this.bookForm.name.trim()) {
          this.$toast('请输入账本名称')
          done(false)
          return
        }
        try {
          let res
          if (this.dialogMode === 'add') {
            res = await accountBookApi.create({ name: this.bookForm.name })
          } else {
            res = await accountBookApi.update(this.bookForm.id, { name: this.bookForm.name })
          }
          if (res.code === 200) {
            this.$toast.success(this.dialogMode === 'add' ? '添加成功' : '修改成功')
            done()
            await this.loadAccountBooks()
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
    async handleSetDefault (book) {
      if (book.isDefault) return
      try {
        await this.$dialog.confirm({
          title: '提示',
          message: `确定要将"${book.name}"设为默认账本吗？`
        })
        const res = await accountBookApi.setDefault(book.id)
        if (res.code === 200) {
          this.$toast.success('设置成功')
          await this.loadAccountBooks()
        } else {
          this.$toast.fail(res.message || '设置失败')
        }
      } catch (error) {
        // 用户取消
      }
    },
    async handleDelete (book) {
      if (book.isDefault) {
        this.$toast('默认账本不能删除')
        return
      }
      try {
        await this.$dialog.confirm({
          title: '提示',
          message: `确定要删除账本"${book.name}"吗？删除后相关记录也会被删除。`
        })
        const res = await accountBookApi.delete(book.id)
        if (res.code === 200) {
          this.$toast.success('删除成功')
          await this.loadAccountBooks()
        } else {
          this.$toast.fail(res.message || '删除失败')
        }
      } catch (error) {
        // 用户取消
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.account-book-manage-page {
  min-height: 100%;
  background-color: #f7f8fa;

  .account-book-list {
    padding-top: 12px;

    .create-time {
      font-size: 12px;
      color: #969799;
    }
  }

  .swipe-btn {
    height: 100%;
  }
}
</style>
