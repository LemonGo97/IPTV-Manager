<template>
  <CommonPage>
    <template #action>
      <NButton type="primary" @click="handleAdd()">
        <i class="i-material-symbols:add mr-4 text-18" />
        新增分发
      </NButton>
    </template>

    <div class="action-area">
      <n-space>
        <n-input v-model:value="queryItems.name" placeholder="搜索订阅名称" clearable style="width: 200px" @keyup.enter="handleSearch"/>
        <n-select
          v-model:value="queryItems.userId"
          filterable
          placeholder="根据用户搜索"
          :options="queryForm.userId.optionsRef"
          :loading="queryForm.userId.loadingRef"
          clearable
          remote
          @search="queryForm.userId.search"
          @@scroll="queryForm.userId.scroll"
        />
        <n-button type="primary" @click="handleSearch">
          <i class="i-material-symbols:search mr-4 text-18"/>
          搜索
        </n-button>
        <n-button type="primary" @click="fetchTableData">
          <i class="i-material-symbols:refresh mr-4 text-18"/>
          刷新
        </n-button>
      </n-space>
    </div>
    <n-data-table
      :columns="columns"
      :data="tableData"
      :pagination="pagination"
      :remote="true"
      :loading="loading"
      :scroll-x="1400"
      :row-key="(row) => row.id"
      size="small"
    />

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
          :rule="{ type: 'number', required: true, message: '请选择订阅用户', trigger: ['change', 'blur'] }"
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
          path="dateType"
          :rule="{ required: true, message: '请选择有效期', trigger: ['change', 'blur'] }"
        >
          <n-select
            v-model:value="modalForm.dateType"
            placeholder="请选择有效期"
            :options="validityOptions"
            @update:value="handleValidityTypeChange"
          />
        </n-form-item>

        <!-- 自定义日期范围 -->
        <template v-if="modalForm.dateType === 'CUSTOM'">
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
import {NButton, NTag, NSelect, NDataTable, NSpace} from 'naive-ui'
import { useClipboard } from '@vueuse/core'
import { isBefore, isAfter } from 'date-fns'
import { MeModal } from '@/components'
import { useCrud } from '@/composables'
import api from './api'
import distributionUserApi from '../users/api'
import { formatDateTime, debounce } from '@/utils'
import {h, onMounted, reactive, ref, watch} from "vue";

const $table = ref(null)


const queryForm = reactive({
  userId : {
    optionsRef: [],
    loadingRef: false,
    debounceSearch: debounce((query=null) => {
      fetchQueryFormOptions({username: query}, () => {
        queryForm.userId.loadingRef = false;
      })
    }, 1e3, false),
    search: (query) => {
      if (!query.length) {
        queryForm.userId.optionsRef = [];
        return;
      }
      queryForm.userId.loadingRef = true;
      queryForm.userId.debounceSearch(query)
    }
  }
})
// 使用剪贴板
const { copy, copied } = useClipboard()

// 监听复制状态
watch(copied, (val) => {
  if (val) $message.success('已复制到剪贴板')
})

const queryItems = reactive({
  name: '',
})

// 有效期选项（枚举值）
const validityOptions = [
  { label: '1个月', value: 'MONTH' },
  { label: '3个月', value: 'QUARTER' },
  { label: '半年', value: 'HALF_YEAR' },
  { label: '一年', value: 'YEAR' },
  { label: '永久', value: 'FOREVER' },
  { label: '自定义', value: 'CUSTOM' },
]

const userOptions = ref([])

// 枚举值到显示名称的映射
const dateTypeMap = {
  'MONTH': '1个月',
  'QUARTER': '3个月',
  'HALF_YEAR': '半年',
  'YEAR': '一年',
  'FOREVER': '永久',
  'CUSTOM': '自定义',
}

// 格式化有效期显示
function formatValidity(row) {
  if (!row.endTime) return '永久'
  if (row.dateType === 'CUSTOM') {
    const start = new Date(row.startTime)
    const end = new Date(row.endTime)
    return `${formatDate(start)} ~ ${formatDate(end)}`
  }
  return dateTypeMap[row.dateType] || row.dateType
}

function formatDate(date) {
  return `${date.getMonth() + 1}/${date.getDate()}`
}

// 获取订阅状态
function getStatus(row) {
  const now = new Date()

  // 永久有效
  if (!row.endTime) {
    return { label: '生效中', type: 'success' }
  }

  const startTime = new Date(row.startTime)
  const endTime = new Date(row.endTime)

  // 未开始
  if (isBefore(now, startTime)) {
    return { label: '未生效', type: 'default' }
  }

  // 已过期
  if (isAfter(now, endTime)) {
    return { label: '已过期', type: 'error' }
  }

  // 生效中
  return { label: '生效中', type: 'success' }
}

// 渲染状态标签
function renderStatus(row) {
  const status = getStatus(row)
  return h(NTag, { type: status.type }, { default: () => status.label })
}

const columns = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '分发名称', key: 'name', ellipsis: { tooltip: true } },
  {
    title: '订阅用户',
    key: 'distributionUser.username',
    width: 120,
  },
  {
    title: '生效时间',
    key: 'startTime',
    width: 160,
    render: row => row.startTime ? formatDateTime(row.startTime) : '-',
  },
  {
    title: '过期时间',
    key: 'endTime',
    width: 160,
    render: row => row.endTime ? formatDateTime(row.endTime) : '永久',
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: row => renderStatus(row),
  },
  {
    title: '有效期',
    key: 'validity',
    width: 80,
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
      h('div', { class: 'flex items-center gap-8' }, [
        h(
          NButton,
          { size: 'small', onClick: () => handleCopyUrl(row) },
          { default: () => '复制链接'},
        ),
        h(NButton, { size: 'small', onClick: () => handleOpenWrapper(row) }, { default: () => '编辑' }),
        h(
          NButton,
          {
            size: 'small',
            type: 'error',
            onClick: () => handleDelete(row.id),
          },
          { default: () => '删除' }
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
    dateType: 'YEAR',
    customStartTime: null,
    customEndTime: null,
  },
  doCreate: async (data) => {
    const payload = {
      name: data.name,
      userId: data.userId,
      dateType: data.dateType,
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
      dateType: data.dateType,
      customStartTime: data.customStartTime ? new Date(data.customStartTime).toISOString() : null,
      customEndTime: data.customEndTime ? new Date(data.customEndTime).toISOString() : null,
    }
    return await api.update(data.id, payload)
  },
  refresh: () => $table.value?.handleSearch(),
})

// 包装 handleOpen 以处理编辑时的数据转换
async function handleOpenWrapper(row) {
  const rowData = row ?? {}

  // 后端返回的是枚举名称，直接使用
  const dateType = rowData.dateType || 'YEAR'

  // 自定义类型需要设置日期时间戳
  let customStartTime = null
  let customEndTime = null

  if (rowData.dateType === 'CUSTOM' && rowData.startTime && rowData.endTime) {
    customStartTime = new Date(rowData.startTime).getTime()
    customEndTime = new Date(rowData.endTime).getTime()
  }

  // 调用原始的 handleOpen 并传递处理后的数据
  handleOpen({
    action: 'edit',
    row: {
      ...rowData,
      dateType,
      customStartTime,
      customEndTime,
    }
  })
}

// 有效期类型变化时处理
function handleValidityTypeChange(value) {
  if (value !== 'CUSTOM') {
    modalForm.customStartTime = null
    modalForm.customEndTime = null
  }
}

// 加载用户选项
async function loadUserOptions() {
  try {
    const res = await fetch('/api/distribution/users').then(r => r.json())
    userOptions.value = res.data || []
  } catch (error) {
    console.error('Failed to load users:', error)
  }
}

// 复制订阅链接
async function handleCopyUrl(row) {
  try {
    const res = await api.getSubscriptionUrl(row.id)
    copy(res.data || window.location.origin + res.data)
  } catch (error) {
    $message.error('获取订阅链接失败')
  }
}

const loading = ref(false)
const tableData = ref([])
// 分页配置
const pagination = reactive({
  page: 1,
  pageSize: 10,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100],
  showQuickJumper: true,
  prefix: ({itemCount}) => `共 ${itemCount} 条`,
  itemCount: 0,
  onChange: (page) => {
    pagination.page = page
    fetchTableData()
  },
  onUpdatePageSize: (pageSize) => {
    pagination.pageSize = pageSize
    pagination.page = 1
    fetchTableData()
  },
})

async function fetchTableData() {
  loading.value = true
  try {
    const res = await api.getList({
      pageNum: pagination.page,
      pageSize: pagination.pageSize,
      name: queryItems.name || undefined,
      userId: queryItems.userId || undefined,
    })
    tableData.value = res.data.list || []
    pagination.itemCount = res.data.total || 0
  } catch (error) {
    $message.error('获取数据失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 搜索
function handleSearch() {
  pagination.page = 1
  fetchTableData()
}

function fetchQueryFormOptions(params, func){
  distributionUserApi.getAll(params).then((res) => {
    queryForm.userId.optionsRef = res.data.map(u => { return {"label" : u.username, "value": u.id}})
    if (func) func(res)
  })
}

onMounted(() => {
  fetchTableData()
  fetchQueryFormOptions()
  loadUserOptions()
})

defineOptions({
  name: 'DistributionSubscriptions',
})
</script>
<style scoped>
.action-area {
  margin-bottom: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
