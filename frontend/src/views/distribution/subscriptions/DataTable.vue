<template>
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
</template>

<script setup>
import { NButton, NTag, NDataTable } from 'naive-ui'
import { useClipboard } from '@vueuse/core'
import { isBefore, isAfter } from 'date-fns'
import { formatDateTime } from '@/utils'
import { h, computed, onMounted, reactive, ref, watch } from 'vue'
import api from './api'

const props = defineProps({
  searchParams: {
    type: Object,
    default: () => ({}),
  },
})

const emit = defineEmits(['edit'])

// 使用剪贴板
const { copy, copied } = useClipboard()

// 监听复制状态
watch(copied, (val) => {
  if (val) $message.success('已复制到剪贴板')
})

// 表格数据
const loading = ref(false)
const tableData = ref([])

// 分页配置
const pagination = reactive({
  page: 1,
  pageSize: 10,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100],
  showQuickJumper: true,
  prefix: ({ itemCount }) => `共 ${itemCount} 条`,
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

// 获取表格数据
async function fetchTableData() {
  loading.value = true
  try {
    const res = await api.getList({
      pageNum: pagination.page,
      pageSize: pagination.pageSize,
      name: props.searchParams.name || undefined,
      userId: props.searchParams.userId || undefined,
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

// 复制订阅链接
async function handleCopyUrl(row) {
  try {
    const res = await api.getSubscriptionUrl(row.id)
    await copy(res.data || window.location.origin + res.data)
  } catch (error) {
    $message.error('获取订阅链接失败')
  }
}

// 删除
async function handleDelete(id) {
  try {
    await api.delete(id)
    $message.success('删除成功')
    await fetchTableData()
  } catch (error) {
    $message.error('删除失败')
    console.error(error)
  }
}

// 枚举值到显示名称的映射
const dateTypeMap = {
  'MONTH': '1个月',
  'QUARTER': '3个月',
  'HALF_YEAR': '半年',
  'YEAR': '一年',
  'FOREVER': '永久',
  'CUSTOM': '自定义',
}

// 格式化日期
function formatDate(date) {
  return `${date.getMonth() + 1}/${date.getDate()}`
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

// 表格列定义
const columns = computed(() => [
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
          { default: () => '复制链接' },
        ),
        h(
          NButton,
          { size: 'small', onClick: () => emit('edit', row) },
          { default: () => '编辑' },
        ),
        h(
          NButton,
          {
            size: 'small',
            type: 'error',
            onClick: () => handleDelete(row.id),
          },
          { default: () => '删除' },
        ),
      ]),
  },
])

// 监听搜索参数变化
watch(() => props.searchParams, () => {
  pagination.page = 1
  fetchTableData()
}, { deep: true })

onMounted(() => {
  fetchTableData()
})

// 暴露刷新方法给父组件
defineExpose({
  refresh: fetchTableData,
})
</script>
