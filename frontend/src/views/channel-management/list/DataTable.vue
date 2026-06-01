<template>
  <n-data-table
    :columns="columns"
    :data="tableData"
    :pagination="pagination"
    :remote="true"
    :loading="loading"
    :scroll-x="1400"
    :row-key="(row) => row.id"
    :filters="filters"
    @update:filters="handleFiltersChange"
    size="small"
  />
</template>

<script setup>
import { NButton, NTag, NDataTable, NSpace, NDropdown } from 'naive-ui'
import { h, computed, onMounted, reactive, ref, watch } from 'vue'
import { useClipboard } from '@vueuse/core'
import api from './api'

const props = defineProps({
  searchParams: {
    type: Object,
    default: () => ({}),
  },
})

const emit = defineEmits(['viewEpg', 'updateFilters', 'preview'])

const { copy, copied } = useClipboard()

// 监听复制状态
watch(copied, (val) => {
  if (val) {
    $message.success('已复制到剪贴板')
  }
})

// 表格数据
const loading = ref(false)
const tableData = ref([])

// 表格过滤值
const filters = ref({
  'provider.name': null,
  'channelGroup.name': null,
  'status': null,
})

// 过滤器选项
const providerOptions = ref([])
const channelGroupOptions = ref([])

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

// 处理过滤器变化
function handleFiltersChange(filters) {
  emit('updateFilters', filters)
}

// 获取表格数据
async function fetchTableData() {
  loading.value = true
  try {
    const res = await api.getList({
      pageNum: pagination.page,
      pageSize: pagination.pageSize,
      name: props.searchParams.name || undefined,
      providerId: props.searchParams.providerId || undefined,
      groupId: props.searchParams.groupId || undefined,
      status: props.searchParams.status || undefined,
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

// 获取过滤器选项
async function fetchFilterOptions() {
  try {
    const res = await api.getOptions()
    providerOptions.value = (res.data.provider || []).map(item => ({
      label: item.name,
      value: item.id,
    }))
    channelGroupOptions.value = (res.data.group || []).map(item => ({
      label: item.name,
      value: item.id,
    }))
  } catch (error) {
    $message.error('获取过滤器选项失败')
    console.error(error)
  }
}

// 复制到剪贴板
function copyToClipboard(text) {
  copy(text)
}

// 表格列定义
const columns = computed(() => [
  {
    type: "selection",
  },
  { title: 'ID', key: 'id', width: 80 },
  {
    title: '频道名称',
    key: 'name',
    width: 120,
    ellipsis: { tooltip: true },
  },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render: row =>
      h(
        NTag,
        { type: row.status === 'valid' ? 'success' : row.status === 'invalid' ? 'error' : 'info' },
        {
          default: () => {
            switch (row.status) {
              case 'valid': return '有效'
              case 'invalid': return '无效'
              case 'unknown': return '未知'
            }
          },
        }
      ),
    filter: true,
    filterOptions: [
      { label: '有效', value: 'valid' },
      { label: '无效', value: 'invalid' },
      { label: '未知', value: 'unknown' },
    ],
  },
  {
    title: '订阅源',
    key: 'provider.name',
    width: 120,
    filter: true,
    filterOptions: providerOptions.value,
  },
  {
    title: '频道组',
    key: 'channelGroup.name',
    width: 120,
    filter: true,
    filterOptions: channelGroupOptions.value,
  },
  {
    title: '延迟（ms）',
    key: 'httpDetectDelayMilliseconds',
    width: 200,
    render(row) {
      return h(NSpace, { vertical: true }, {
        default: () => [
          h('span', `HTTP 检测：${row.httpDetectDelayMilliseconds || '超时'}`),
          h('span', `ffmpeg 检测：${row.ffmpegDetectDelayMilliseconds || '超时'}`),
        ],
      })
    },
  },
  {
    title: '音视频信息',
    key: 'mediaInfo',
    width: 240,
    render(row) {
      return h(NSpace, { vertical: true }, {
        default: () => {
          const res = []
          if (row.videoInfo) {
            const vinfo = JSON.parse(row.videoInfo)
            res.push(h('span', `视频流信息：${vinfo.width} x ${vinfo.height} ${vinfo.codec}`))
          }
          if (row.audioInfo) {
            const ainfo = JSON.parse(row.audioInfo)
            res.push(h('span', `音频流信息：${ainfo.codec} ${ainfo.rate}`))
          }
          return res
        },
      })
    },
  },
  {
    title: '播放地址',
    key: 'url',
    width: 300,
    ellipsis: { tooltip: true },
    render: row =>
      h(
        'a',
        {
          href: row.url,
          target: '_blank',
          class: 'text-primary',
          onClick: e => {
            e.preventDefault()
            copyToClipboard(row.url)
          },
        },
        row.url
      ),
  },
  { title: '国家', key: 'country', width: 100 },
  { title: '语言', key: 'language', width: 100 },
  {
    title: '操作',
    key: 'actions',
    width: 180,
    fixed: 'right',
    render: row =>
      h('div', { class: 'flex gap-8' }, [
        h(
          NDropdown,
          {
            options: [
              { label: 'IINA', key: 'iina' },
              { label: 'PotPlayer', key: 'potplayer' },
              { label: 'VLC', key: 'vlc' },
            ],
            onSelect: (key) => emit('preview', { key, row }),
          },
          {
            default: () =>
              h(NButton, { size: 'small' }, { default: () => '预览' }),
          }
        ),
        h(NButton, { size: 'small', type: 'info', onClick: () => emit('viewEpg', row) }, {
          default: () => '查看EPG',
        }),
      ]),
  },
])

// 监听搜索参数变化
watch(() => props.searchParams, () => {
  pagination.page = 1
  fetchTableData()
}, { deep: true })

onMounted(() => {
  fetchFilterOptions()
  fetchTableData()
})

// 暴露刷新方法给父组件
defineExpose({
  refresh: fetchTableData,
})
</script>
