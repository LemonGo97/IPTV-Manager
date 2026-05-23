<template>
  <CommonPage>
    <template #action>
      <NSelect
        v-model:value="selectedSourceId"
        placeholder="请选择EPG源"
        :options="sourceOptions"
        :loading="sourcesLoading"
        style="width: 300px"
        @update:value="handleSourceChange"
        clearable
      />
    </template>

    <NDataTable
      :columns="columns"
      :data="tableData"
      :loading="loading"
      :row-key="rowKey"
      :expanded-row-keys="expandedRowKeys"
      @update:expanded-row-keys="handleExpand"
    />
  </CommonPage>
</template>

<script setup>
import { NDataTable, NSelect } from 'naive-ui'
import api from './api'
import sourceApi from '../sources/api'

const $message = window.$message

// EPG 源选择
const selectedSourceId = ref(null)
const sourceOptions = ref([])
const sourcesLoading = ref(false)

// 表格数据
const tableData = ref([])
const loading = ref(false)
const expandedRowKeys = ref([])

// 树形数据的行键函数
const rowKey = (row) => {
  return row.type === 'channel' ? `channel-${row.channelName}` : `program-${row.id}`
}

// 表格列定义
const columns = [
  {
    type: 'expand',
    expandable: (row) => row.type === 'channel',
  },
  {
    title: '频道/节目',
    key: 'name',
    render: (row) => {
      if (row.type === 'channel') {
        return `${row.channelName} (${row.programCount}个节目)`
      }
      return row.title
    },
  },
  {
    title: '开始时间',
    key: 'startTime',
    render: (row) => {
      if (row.type === 'channel') return '-'
      return formatTime(row.startTime)
    },
  },
  {
    title: '结束时间',
    key: 'stopTime',
    render: (row) => {
      if (row.type === 'channel') return '-'
      return formatTime(row.stopTime)
    },
  },
  {
    title: '描述',
    key: 'description',
    ellipsis: { tooltip: true },
    render: (row) => {
      if (row.type === 'channel') return '-'
      return row.description || '-'
    },
  },
]

// 格式化 XMLTV 时间
function formatTime(xmltvTime) {
  if (!xmltvTime) return '-'
  try {
    // XMLTV 格式: 20240101120000 +0800
    const match = xmltvTime.match(/(\d{14})\s*([+-]\d{4})/)
    if (match) {
      const [, datetime, timezone] = match
      const year = datetime.substring(0, 4)
      const month = datetime.substring(4, 6)
      const day = datetime.substring(6, 8)
      const hour = datetime.substring(8, 10)
      const minute = datetime.substring(10, 12)
      const second = datetime.substring(12, 14)
      return `${year}-${month}-${day} ${hour}:${minute}:${second}`
    }
    return xmltvTime
  } catch {
    return xmltvTime
  }
}

// 加载 EPG 源列表
async function loadSources() {
  sourcesLoading.value = true
  try {
    const res = await sourceApi.getAll()
    sourceOptions.value = res.data.map(source => ({
      label: source.name,
      value: source.id,
    }))
  } catch (error) {
    $message.error('加载EPG源失败: ' + (error.message || '未知错误'))
  } finally {
    sourcesLoading.value = false
  }
}

// EPG 源切换
async function handleSourceChange(sourceId) {
  if (!sourceId) {
    tableData.value = []
    expandedRowKeys.value = []
    return
  }

  loading.value = true
  try {
    const res = await api.getChannels(sourceId)
    // 将频道分组转换为树形数据的第一层
    tableData.value = res.data.map(channel => ({
      type: 'channel',
      channelName: channel.channelName,
      programCount: channel.programCount,
      children: null, // 懒加载，初始为 null
      _loaded: false,  // 标记是否已加载
    }))
  } catch (error) {
    $message.error('加载频道列表失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 处理展开/收起
async function handleExpand(keys) {
  expandedRowKeys.value = keys

  // 查找需要加载节目的频道
  const channelsToLoad = tableData.value
    .filter(row => row.type === 'channel' && !row._loaded && keys.includes(rowKey(row)))
    .map(row => row.channelName)

  if (channelsToLoad.length === 0) return

  loading.value = true
  try {
    // 并行加载所有需要加载的频道的节目
    await Promise.all(
      channelsToLoad.map(async (channelName) => {
        try {
          const res = await api.getPrograms(selectedSourceId.value, channelName)
          const programs = res.data.map(program => ({
            ...program,
            type: 'program',
          }))

          // 更新对应频道的 children
          const channelIndex = tableData.value.findIndex(
            row => row.type === 'channel' && row.channelName === channelName
          )
          if (channelIndex !== -1) {
            tableData.value[channelIndex].children = programs
            tableData.value[channelIndex]._loaded = true
          }
        } catch (error) {
          console.error(`加载频道 ${channelName} 的节目失败:`, error)
        }
      })
    )
  } catch (error) {
    $message.error('加载节目数据失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadSources()
})

defineOptions({
  name: 'EpgViewer',
})
</script>
