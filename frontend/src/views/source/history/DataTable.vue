<template>
  <n-data-table
    :columns="columns"
    :data="tableData"
    :pagination="pagination"
    :remote="true"
    :loading="loading"
    :scroll-x="1600"
    :row-key="(row) => row.id"
    size="small"
  />
</template>

<script setup>
import { NButton, NTag, NDataTable } from 'naive-ui'
import { h, computed, onMounted, reactive, ref, watch } from 'vue'
import api from './api'
import dayjs from 'dayjs'

const props = defineProps({
  searchParams: {
    type: Object,
    default: () => ({}),
  },
})

const emit = defineEmits(['view-detail', 'view-playlist'])

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
    const res = await api.getAll({
      pageNum: pagination.page,
      pageSize: pagination.pageSize,
      taskId: props.searchParams.taskId,
      providerName: props.searchParams.providerName,
      triggerType: props.searchParams.triggerType,
      status: props.searchParams.status,
      startTime: props.searchParams.startTime,
      endTime: props.searchParams.endTime,
    })
    tableData.value = res.data || []
    pagination.itemCount = res.data?.total || 0
  } catch (error) {
    $message.error('获取数据失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 格式化时间戳为可读格式（精确到毫秒）
function formatTime(timestamp) {
  if (!timestamp) return '-'
  return dayjs(timestamp).format('YYYY-MM-DD HH:mm:ss.SSS')
}

// 格式化耗时为 x小时x分x秒x毫秒
function formatDuration(ms) {
  if (!ms || ms <= 0) return '-'

  const hours = Math.floor(ms / 3600000)
  const minutes = Math.floor((ms % 3600000) / 60000)
  const seconds = Math.floor((ms % 60000) / 1000)
  const milliseconds = ms % 1000

  const parts = []
  if (hours > 0) parts.push(`${hours}小时`)
  if (minutes > 0) parts.push(`${minutes}分`)
  if (seconds > 0) parts.push(`${seconds}秒`)
  if (milliseconds > 0 || parts.length === 0) parts.push(`${milliseconds}毫秒`)

  return parts.join('')
}

// 获取状态类型
function getStatusType(status) {
  const types = {
    success: 'success',
    failed: 'error',
    running: 'warning',
  }
  return types[status] || 'default'
}

// 获取状态文本
function getStatusText(status) {
  const texts = {
    success: '成功',
    failed: '失败',
    running: '进行中',
  }
  return texts[status] || '未知'
}

// 表格列定义
const columns = computed(() => [
  { title: '任务ID', key: 'id', width: 120, render: row => row.id || '-' },
  { title: '订阅源', key: 'providerName', width: 150, ellipsis: { tooltip: true } },
  {
    title: '触发方式',
    key: 'triggerType',
    width: 100,
    render: row =>
      h(
        NTag,
        { type: row.triggerType === 'manual' ? 'info' : 'warning', size: 'small' },
        { default: () => (row.triggerType === 'manual' ? '手动刷新' : '定时任务') }
      ),
  },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render: row =>
      h(
        NTag,
        { type: getStatusType(row.status), size: 'small' },
        { default: () => getStatusText(row.status) }
      ),
  },
  { title: '开始时间', key: 'startTime', width: 180, render: row => formatTime(row.startTime) },
  { title: '结束时间', key: 'endTime', width: 180, render: row => formatTime(row.endTime) },
  {
    title: '耗时',
    key: 'duration',
    width: 150,
    render: row => formatDuration(row.duration),
  },
  {
    title: '频道数',
    key: 'channelCount',
    width: 80,
    render: row => row.channelCount ?? '-',
  },
  {
    title: '操作',
    key: 'actions',
    width: 180,
    fixed: 'right',
    render: row =>
      h('div', { class: 'flex items-center gap-8' }, [
        h(
          NButton,
          {
            size: 'small',
            onClick: () => emit('view-detail', row),
          },
          { default: () => '详情' }
        ),
        h(
          NButton,
          {
            size: 'small',
            type: 'primary',
            disabled: row.status !== 'success',
            onClick: () => emit('view-playlist', row),
          },
          { default: () => '频道列表' }
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
