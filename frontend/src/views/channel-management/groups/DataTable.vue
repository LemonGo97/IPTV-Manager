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
import { NButton, NDataTable } from 'naive-ui'
import { h, computed, onMounted, reactive, ref, watch } from 'vue'
import api from './api'

const props = defineProps({
  searchParams: {
    type: Object,
    default: () => ({}),
  },
})

const emit = defineEmits(['edit'])

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

// 表格列定义
const columns = computed(() => [
  { title: 'ID', key: 'id', width: 80 },
  { title: '名称', key: 'name', width: 200, ellipsis: { tooltip: true } },
  { title: '描述', key: 'description', ellipsis: { tooltip: true } },
  { title: '排序', key: 'sortOrder', width: 100 },
  {
    title: '创建时间',
    key: 'createdAt',
    width: 180,
    render: row => (row.createdAt ? new Date(row.createdAt).toLocaleString('zh-CN') : '-'),
  },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    fixed: 'right',
    render: row =>
      h('div', { class: 'flex gap-8' }, [
        h(
          NButton,
          { size: 'small', onClick: () => emit('edit', row) },
          { default: () => '编辑' }
        ),
        h(
          NButton,
          { size: 'small', type: 'error', onClick: () => handleDelete(row.id) },
          { default: () => '删除' }
        ),
      ]),
  },
])

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
