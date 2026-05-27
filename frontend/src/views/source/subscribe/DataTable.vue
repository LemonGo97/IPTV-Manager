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
import { h, computed, onMounted, reactive, ref, watch } from 'vue'
import api from './api'

const props = defineProps({
  searchParams: {
    type: Object,
    default: () => ({}),
  },
})

const emit = defineEmits(['edit', 'refresh', 'delete'])

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
      name: props.searchParams.name,
      type: props.searchParams.type,
      enabled: props.searchParams.enabled,
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

// 刷新
async function handleRefresh(row) {
  try {
    const result = await api.refresh(row.id)
    $message.success('刷新任务已提交，请在任务历史中查看进度')
  } catch (error) {
    $message.error('刷新任务提交失败')
    console.error(error)
  }
}

// 表格列定义
const columns = computed(() => [
  { title: 'ID', key: 'id', width: 80, render: row => row.id || '-' },
  { title: '名称', key: 'name', width: 150, ellipsis: { tooltip: true } },
  {
    title: '类型',
    key: 'type',
    width: 100,
    render: row => (row.type === 'online' ? '在线订阅' : '本地文件'),
  },
  {
    title: '订阅地址/文件名',
    key: 'url',
    width: 300,
    ellipsis: { tooltip: true },
    render: row => row.url || row.filename || '-',
  },
  {
    title: '自动刷新',
    key: 'autoRefresh',
    width: 100,
    render: row => (row.autoRefresh ? `${row.refreshRate}秒` : '-'),
  },
  {
    title: '状态',
    key: 'enabled',
    width: 80,
    render: row =>
      h(
        NTag,
        { type: row.enabled ? 'success' : 'default', size: 'small' },
        { default: () => (row.enabled ? '启用' : '停用') }
      ),
  },
  { title: '备注', key: 'description', width: 200, ellipsis: { tooltip: true } },
  {
    title: '创建时间',
    key: 'createdAt',
    width: 180,
    render: row => row.createdAt || '-',
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
          {
            size: 'small',
            onClick: () => emit('edit', row),
          },
          { default: () => '编辑' }
        ),
        h(
          NButton,
          {
            size: 'small',
            type: 'primary',
            onClick: () => handleRefresh(row),
          },
          { default: () => '刷新' }
        ),
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
