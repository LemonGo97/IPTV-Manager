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

    <MeModal ref="modalRef" width="600px">
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
          :rule="{
            required: modalAction.value === 'add',
            message: '请输入访问密钥',
            trigger: ['input', 'blur'],
          }"
        >
          <n-input
            v-model:value="modalForm.accessKey"
            :placeholder="modalAction.value === 'add' ? '请输入访问密钥' : '留空则不修改'"
            clearable
            show-password-on="click"
            type="password"
          />
        </n-form-item>

        <n-form-item label="关联订阅" path="subscriptionIds">
          <n-select
            v-model:value="modalForm.subscriptionIds"
            multiple
            label-field="name"
            value-field="id"
            placeholder="请选择关联的订阅"
            :options="subscriptionOptions"
            clearable
            filterable
          />
        </n-form-item>

        <n-form-item label="排序" path="sortOrder">
          <n-input-number v-model:value="modalForm.sortOrder" placeholder="请输入排序值" :min="0" style="width: 100%" />
        </n-form-item>

        <n-form-item label="状态" path="enabled">
          <n-switch v-model:value="modalForm.enabled" />
        </n-form-item>
      </n-form>
    </MeModal>
  </CommonPage>
</template>

<script setup>
import { NButton, NTag, NSwitch, NPopconfirm } from 'naive-ui'
import { useClipboard } from '@vueuse/core'
import { MeCrud, MeModal, MeQueryItem } from '@/components'
import { useCrud } from '@/composables'
import api from './api'

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

const subscriptionOptions = ref([])

const columns = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '用户名', key: 'username', width: 150 },
  { title: '访问密钥', key: 'accessKey', width: 200, render: row => '••••••••' },
  {
    title: '关联订阅',
    key: 'subscriptionIds',
    width: 200,
    render: row =>
      row.subscriptionIds?.length
        ? h('div', { class: 'flex flex-wrap gap-4' }, row.subscriptionIds.map(id => h(NTag, { size: 'small' }, { default: () => `ID: ${id}` })))
        : '-',
  },
  { title: '排序', key: 'sortOrder', width: 80 },
  {
    title: '状态',
    key: 'enabled',
    width: 100,
    render: row =>
      h(
        NSwitch,
        {
          size: 'small',
          value: row.enabled,
          onUpdateValue: val => handleToggleEnabled(row, val),
        },
      ),
  },
  {
    title: '操作',
    key: 'actions',
    width: 280,
    fixed: 'right',
    render: row =>
      h('div', { class: 'flex gap-8' }, [
        h(
          NButton,
          { size: 'small', onClick: () => handleCopyUrl(row) },
          { default: () => '订阅链接', icon: h('i', { class: 'i-material-symbols:link' }) },
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
    subscriptionIds: [],
    sortOrder: 0,
    enabled: true,
  },
  doCreate: api.create,
  doDelete: api.delete,
  doUpdate: (data) => api.update(data.id, data),
  refresh: () => $table.value?.handleSearch(),
})

// 加载订阅选项
async function loadSubscriptionOptions() {
  // TODO: 调用订阅列表 API 获取选项
  subscriptionOptions.value = []
}

// 切换启用状态
async function handleToggleEnabled(row, value) {
  try {
    await api.update(row.id, { ...row, enabled: value })
    $message.success('状态更新成功')
    $table.value?.handleSearch()
  } catch (error) {
    $message.error('状态更新失败')
  }
}

// 复制订阅链接
async function handleCopyUrl(row) {
  try {
    const res = await api.copySubscriptionUrl(row.id)
    const url = res.data.url || `${window.location.origin}/api/distribution/users/${row.id}/playlist.m3u8?key=${row.accessKey}`
    copy(url)
    
  } catch (error) {
    $message.error('获取订阅链接失败')
  }
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
  loadSubscriptionOptions()
})

defineOptions({
  name: 'DistributionUsers',
})
</script>
