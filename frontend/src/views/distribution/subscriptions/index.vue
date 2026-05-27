<template>
  <CommonPage>
    <template #action>
      <NButton type="primary" @click="handleAdd()">
        <i class="i-material-symbols:add mr-4 text-18" />
        新增分发
      </NButton>
    </template>

    <MeCrud
      ref="$table"
      v-model:query-items="queryItems"
      :columns="columns"
      :get-data="api.getAll"
    >
      <MeQueryItem label="名称" :label-width="50">
        <n-input v-model:value="queryItems.name" placeholder="请输入名称" clearable />
      </MeQueryItem>
      <MeQueryItem label="状态" :label-width="50">
        <n-select
          v-model:value="queryItems.enabled"
          placeholder="请选择状态"
          clearable
          :options="[
            { label: '全部', value: null },
            { label: '已启用', value: true },
            { label: '已禁用', value: false },
          ]"
        />
      </MeQueryItem>
    </MeCrud>

    <!-- 表单弹窗 -->
    <MeModal ref="modalRef" width="600px">
      <n-form ref="modalFormRef" :model="modalForm" label-width="100">
        <n-form-item
          label="名称"
          path="name"
          :rule="{ required: true, message: '请输入名称', trigger: ['input', 'blur'] }"
        >
          <n-input v-model:value="modalForm.name" placeholder="请输入分发源名称" />
        </n-form-item>
        <n-form-item
          label="描述"
          path="description"
        >
          <n-input
            v-model:value="modalForm.description"
            type="textarea"
            placeholder="请输入描述"
            :autosize="{ minRows: 3, maxRows: 5 }"
          />
        </n-form-item>
        <n-form-item
          label="包含频道组"
          path="channelGroupIds"
          :rule="{ required: true, message: '请选择频道组', trigger: ['change', 'blur'] }"
        >
          <n-select
            v-model:value="modalForm.channelGroupIds"
            multiple
            placeholder="请选择频道组"
            :options="channelGroupOptions"
            clearable
          />
        </n-form-item>
        <n-form-item
          label="过滤规则"
          path="cleanupRuleIds"
        >
          <n-select
            v-model:value="modalForm.cleanupRuleIds"
            multiple
            placeholder="请选择过滤规则"
            :options="cleanupRuleOptions"
            clearable
          />
        </n-form-item>
        <n-form-item
          label="排序方式"
          path="sortOrder"
          :rule="{ required: true, message: '请选择排序方式', trigger: ['change', 'blur'] }"
        >
          <n-select
            v-model:value="modalForm.sortOrder"
            placeholder="请选择排序方式"
            :options="[
              { label: '按频道组排序', value: 'group' },
              { label: '按频道名称排序', value: 'name' },
            ]"
          />
        </n-form-item>
        <n-form-item
          label="是否启用"
          path="enabled"
        >
          <n-switch v-model:value="modalForm.enabled" />
        </n-form-item>
      </n-form>
    </MeModal>
  </CommonPage>
</template>

<script setup>
import { NButton, NTag, NSpace, NPopconfirm, NInput, NSwitch, NSelect } from 'naive-ui'
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
  name: '',
  enabled: null,
})

const columns = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '名称', key: 'name', ellipsis: { tooltip: true } },
  { title: '订阅URL', key: 'subscriptionUrl', ellipsis: { tooltip: true } },
  {
    title: '频道组数',
    key: 'channelGroupCount',
    width: 100,
    render: row => h('span', row.channelGroupCount || 0),
  },
  {
    title: '状态',
    key: 'enabled',
    width: 80,
    render: row =>
      h(
        NTag,
        { type: row.enabled ? 'success' : 'default' },
        { default: () => (row.enabled ? '已启用' : '已禁用') }
      ),
  },
  {
    title: '订阅次数',
    key: 'subscriptionCount',
    width: 100,
    render: row => h('span', row.subscriptionCount || 0),
  },
  {
    title: '创建时间',
    key: 'createdAt',
    width: 180,
    render: row => formatDate(row.createdAt),
  },
  {
    title: '操作',
    key: 'actions',
    width: 280,
    fixed: 'right',
    render: row =>
      h('div', { class: 'flex gap-8' }, [
        h(
          NPopconfirm,
          {
            onPositiveClick: () => handleToggleEnabled(row),
          },
          {
            default: () =>
              row.enabled
                ? h('span', '禁用')
                : h('span', '启用'),
          },
          h(
            NButton,
            { size: 'small', onClick: () => handleCopyUrl(row) },
            { default: () => '复制链接' }
          )
        ),
        h(
          NButton,
          { size: 'small', onClick: () => handleOpen(row) },
          { default: () => '编辑' }
        ),
        h(
          NPopconfirm,
          {
            onPositiveClick: () => handleDelete(row.id),
          },
          {
            default: () =>
              h(NButton, { size: 'small', type: 'error' }, { default: () => '删除' }),
          }
        ),
      ]),
  },
]

const channelGroupOptions = ref([])
const cleanupRuleOptions = ref([])

// 使用 useCrud 管理表单和弹窗
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
  name: '分发订阅源',
  initForm: {
    name: '',
    description: '',
    channelGroupIds: [],
    cleanupRuleIds: [],
    sortOrder: 'group',
    enabled: true,
  },
  doCreate: api.create,
  doDelete: api.delete,
  doUpdate: (data) => api.update(data.id, data),
  refresh: () => $table.value?.handleSearch(),
})

async function handleToggleEnabled(row) {
  try {
    await api.toggleEnabled(row.id)
    $table.value?.handleSearch()
    window.$message?.success(row.enabled ? '已禁用' : '已启用')
  } catch (error) {
    console.error('Toggle enabled failed:', error)
  }
}

async function handleCopyUrl(row) {
  try {
    const res = await api.getSubscriptionUrl(row.id)
    const url = res.data.url
    copy(url)
    
  } catch (error) {
    console.error('Copy URL failed:', error)
    window.$message?.error('复制失败，请手动复制')
  }
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

onMounted(() => {
  $table.value?.handleSearch()
  // TODO: 加载频道组和清洗规则选项
  channelGroupOptions.value = []
  cleanupRuleOptions.value = []
})

defineOptions({
  name: 'DistributionSubscriptions',
})
</script>
