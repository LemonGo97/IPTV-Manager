<template>
  <CommonPage>
    <template #action>
      <NButton type="primary" @click="handleAdd()">
        <i class="i-material-symbols:add mr-4 text-18" />
        新增用户
      </NButton>
    </template>

    <MeCrud
      ref="$table"
      v-model:query-items="queryItems"
      :columns="columns"
      :get-data="api.getAll"
    >
      <MeQueryItem label="用户名" :label-width="60">
        <n-input v-model:value="queryItems.username" placeholder="请输入用户名" clearable />
      </MeQueryItem>
    </MeCrud>

    <MeModal ref="modalRef" width="500px">
      <n-form ref="modalFormRef" :model="modalForm" label-width="100">
        <n-form-item
          label="用户名"
          path="username"
          :rule="{
            required: true,
            message: '请输入用户名',
            trigger: ['input', 'blur'],
          }"
        >
          <n-input v-model:value="modalForm.username" placeholder="请输入用户名" />
        </n-form-item>

        <n-form-item
          label="访问密钥"
          path="accessKey"
        >
          <n-input
            v-model:value="modalForm.accessKey"
            placeholder="留空则自动生成"
            clearable
            show-password-on="click"
            type="password"
          />
          <template #feedback>
            留空则由系统自动生成32位随机密钥
          </template>
        </n-form-item>
      </n-form>
    </MeModal>
  </CommonPage>
</template>

<script setup>
import { NButton, NPopconfirm } from 'naive-ui'
import { useClipboard } from '@vueuse/core'
import { MeCrud, MeModal, MeQueryItem } from '@/components'
import { useCrud } from '@/composables'
import api from './api'
import { formatDateTime } from '@/utils'

const $table = ref(null)

// 使用剪贴板
const { copy, copied } = useClipboard()

// 监听复制状态
watch(copied, (val) => {
  if (val) $message.success('已复制到剪贴板')
})

const queryItems = reactive({
  username: '',
})

const columns = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '用户名', key: 'username', width: 150 },
  {
    title: '用户ID',
    key: 'userId',
    width: 280,
    render: row => h('span', { }, row.userId)
  },
  {
    title: '创建时间',
    key: 'createdAt',
    width: 160,
    render: row => formatDateTime(row.createdAt)
  },
  {
    title: '更新时间',
    key: 'updatedAt',
    width: 160,
    render: row => formatDateTime(row.updatedAt)
  },
  {
    title: '操作',
    key: 'actions',
    width: 240,
    fixed: 'right',
    render: row =>
      h('div', { class: 'flex gap-8' }, [
        h(
          NButton,
          { size: 'small', onClick: () => handleCopyUserId(row.userId) },
          { default: () => '复制ID', icon: h('i', { class: 'i-material-symbols:content-copy' }) },
        ),
        h(
          NButton,
          { size: 'small', onClick: () => handleResetKey(row) },
          { default: () => '重置密钥', icon: h('i', { class: 'i-material-symbols:refresh' }) },
        ),
        h(NButton, { size: 'small', onClick: () => handleOpen(row) }, { default: () => '编辑' }),
        h(
          NPopconfirm,
          { onPositiveClick: () => handleDelete(row.id) },
          {
            default: () => '确认删除该用户？',
            trigger: () =>
              h(NButton, { size: 'small', type: 'error' }, { default: () => '删除' }),
          },
        ),
      ]),
  },
]

const {
  modalRef,
  modalFormRef,
  modalForm,
  modalAction,
  handleAdd,
  handleDelete,
  handleOpen,
  handleSave,
} = useCrud({
  name: '订阅用户',
  initForm: {
    username: '',
    accessKey: '',
  },
  doCreate: api.create,
  doDelete: api.delete,
  doUpdate: (data) => api.update(data.id, data),
  refresh: () => $table.value?.handleSearch(),
})

// 复制用户ID
function handleCopyUserId(userId) {
  copy(userId)
}

// 重置访问密钥
async function handleResetKey(row) {
  try {
    await api.resetAccessKey(row.id)
    $message.success('访问密钥已重置')
    $table.value?.handleSearch()
  } catch (error) {
    $message.error('重置失败')
  }
}

onMounted(() => {
  $table.value?.handleSearch()
})

defineOptions({
  name: 'DistributionUsers',
})
</script>
