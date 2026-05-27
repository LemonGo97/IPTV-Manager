<template>
  <n-data-table
    :columns="columns"
    :data="tableData"
    :pagination="pagination"
    :remote="true"
    :loading="loading"
    :scroll-x="1200"
    :row-key="(row) => row.id"
    size="small"
  />
</template>

<script setup>
import { NButton, NTag, NDataTable, NPopconfirm } from 'naive-ui'
import { useClipboard } from '@vueuse/core'
import { formatDateTime } from '@/utils'
import { h, computed, onMounted, reactive, ref, watch } from 'vue'
import api from './api'

const props = defineProps({
  searchParams: {
    type: Object,
    default: () => ({}),
  },
})

const emit = defineEmits(['edit', 'reset-key'])

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
    const res = await api.getAll({
      pageNum: pagination.page,
      pageSize: pagination.pageSize,
      username: props.searchParams.username || undefined,
    })
    tableData.value = res.data || []
    pagination.itemCount = res.data.total || 0
  } catch (error) {
    $message.error('获取数据失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 复制用户ID
function handleCopyUserId(userId) {
  copy(userId)
}

// 重置访问密钥
async function handleResetKey(row) {
  try {
    await api.resetAccessKey(row.id)
    $message.success('访问密钥已重置')
    await fetchTableData()
  } catch (error) {
    $message.error('重置失败')
    console.error(error)
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

// 表格列定义
const columns = computed(() => [
  { title: 'ID', key: 'id', width: 80 },
  { title: '用户名', key: 'username', width: 150 },
  {
    title: '用户ID',
    key: 'userId',
    width: 280,
    render: row => h('span', {}, row.userId)
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
          { default: () => '复制ID', icon: () => h('i', { class: 'i-material-symbols:content-copy' }) },
        ),
        h(
          NButton,
          { size: 'small', onClick: () => handleResetKey(row) },
          { default: () => '重置密钥', icon: () => h('i', { class: 'i-material-symbols:refresh' }) },
        ),
        h(
          NButton,
          { size: 'small', onClick: () => emit('edit', row) },
          { default: () => '编辑' },
        ),
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
