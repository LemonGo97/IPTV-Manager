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
    </MeCrud>

    <!-- 表单弹窗 -->
    <MeModal ref="modalRef" width="600px">
      <n-form ref="modalFormRef" :model="modalForm" label-width="100">
        <n-form-item
          label="分发名称"
          path="name"
          :rule="{ required: true, message: '请输入分发名称', trigger: ['input', 'blur'] }"
        >
          <n-input v-model:value="modalForm.name" placeholder="请输入分发名称" />
        </n-form-item>

        <n-form-item
          label="订阅用户"
          path="userId"
          :rule="{ required: true, message: '请选择订阅用户', trigger: ['change', 'blur'] }"
        >
          <n-select
            v-model:value="modalForm.userId"
            placeholder="请选择订阅用户"
            :options="userOptions"
            label-field="username"
            value-field="id"
            clearable
            filterable
          />
        </n-form-item>

        <n-form-item
          label="有效期"
          path="validityType"
          :rule="{ required: true, message: '请选择有效期', trigger: ['change', 'blur'] }"
        >
          <n-select
            v-model:value="modalForm.validityType"
            placeholder="请选择有效期"
            :options="validityOptions"
            @update:value="handleValidityTypeChange"
          />
        </n-form-item>

        <!-- 自定义日期范围 -->
        <template v-if="modalForm.validityType === 'custom'">
          <n-form-item
            label="开始时间"
            path="customStartTime"
            :rule="{ required: true, message: '请选择开始时间', trigger: ['change', 'blur'] }"
          >
            <n-date-picker
              v-model:value="modalForm.customStartTime"
              type="datetime"
              placeholder="请选择开始时间"
              style="width: 100%"
              clearable
            />
          </n-form-item>

          <n-form-item
            label="结束时间"
            path="customEndTime"
            :rule="{ required: true, message: '请选择结束时间', trigger: ['change', 'blur'] }"
          >
            <n-date-picker
              v-model:value="modalForm.customEndTime"
              type="datetime"
              placeholder="请选择结束时间"
              style="width: 100%"
              clearable
              :is-date-disabled="(timestamp) => {
                if (!modalForm.customStartTime) return false
                return timestamp < modalForm.customStartTime
              }"
            />
          </n-form-item>
        </template>
      </n-form>
    </MeModal>
  </CommonPage>
</template>

<script setup>
import { NButton, NTag, NPopconfirm, NSelect } from 'naive-ui'
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
  name: '',
})

// 有效期选项
const validityOptions = [
  { label: '1个月', value: '1month' },
  { label: '3个月', value: '3month' },
  { label: '半年', value: '6month' },
  { label: '一年', value: '1year' },
  { label: '永久', value: 'forever' },
  { label: '自定义', value: 'custom' },
]

const userOptions = ref([])

// 格式化有效期显示
function formatValidity(row) {
  if (!row.endTime) return '永久'
  const start = new Date(row.startTime)
  const end = new Date(row.endTime)
  return `${formatDate(start)} ~ ${formatDate(end)}`
}

function formatDate(date) {
  return `${date.getMonth() + 1}/${date.getDate()}`
}

const columns = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '分发名称', key: 'name', ellipsis: { tooltip: true } },
  {
    title: '订阅用户',
    key: 'userId',
    width: 120,
    render: row => row.username || '-',
  },
  {
    title: '有效期',
    key: 'validity',
    width: 200,
    render: row => formatValidity(row),
  },
  {
    title: '创建时间',
    key: 'createdAt',
    width: 160,
    render: row => formatDateTime(row.createdAt),
  },
  {
    title: '更新时间',
    key: 'updatedAt',
    width: 160,
    render: row => formatDateTime(row.updatedAt),
  },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    fixed: 'right',
    render: row =>
      h('div', { class: 'flex gap-8' }, [
        h(
          NButton,
          { size: 'small', onClick: () => handleCopyUrl(row) },
          { default: () => '复制链接', icon: h('i', { class: 'i-material-symbols:link' }) },
        ),
        h(NButton, { size: 'small', onClick: () => handleOpen(row) }, { default: () => '编辑' }),
        h(
          NPopconfirm,
          { onPositiveClick: () => handleDelete(row.id) },
          {
            default: () => '确认删除该分发？',
            trigger: () =>
              h(NButton, { size: 'small', type: 'error' }, { default: () => '删除' }),
          },
        ),
      ]),
  },
]

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
  name: '分发订阅',
  initForm: {
    name: '',
    userId: null,
    validityType: '1year',
    customStartTime: null,
    customEndTime: null,
  },
  doCreate: async (data) => {
    const payload = {
      name: data.name,
      userId: data.userId,
      validityType: data.validityType,
      customStartTime: data.customStartTime ? new Date(data.customStartTime).toISOString() : null,
      customEndTime: data.customEndTime ? new Date(data.customEndTime).toISOString() : null,
    }
    return await api.create(payload)
  },
  doDelete: api.delete,
  doUpdate: async (data) => {
    const payload = {
      name: data.name,
      userId: data.userId,
      validityType: data.validityType,
      customStartTime: data.customStartTime ? new Date(data.customStartTime).toISOString() : null,
      customEndTime: data.customEndTime ? new Date(data.customEndTime).toISOString() : null,
    }
    return await api.update(data.id, payload)
  },
  refresh: () => $table.value?.handleSearch(),
})

// 有效期类型变化时处理
function handleValidityTypeChange(value) {
  if (value !== 'custom') {
    modalForm.customStartTime = null
    modalForm.customEndTime = null
  }
}

// 加载用户选项
async function loadUserOptions() {
  try {
    const { data } = await fetch('/api/distribution/users').then(r => r.json())
    userOptions.value = data || []
  } catch (error) {
    console.error('Failed to load users:', error)
  }
}

// 复制订阅链接
async function handleCopyUrl(row) {
  try {
    const res = await api.getSubscriptionUrl(row.id)
    copy(res.data.url || window.location.origin + res.data)
  } catch (error) {
    $message.error('获取订阅链接失败')
  }
}

onMounted(() => {
  $table.value?.handleSearch()
  loadUserOptions()
})

defineOptions({
  name: 'DistributionSubscriptions',
})
</script>
