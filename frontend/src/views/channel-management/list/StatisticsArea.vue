<template>
  <div class="statistics-area">
    <n-grid x-gap="12" y-gap="12" :cols="5">
      <n-gi>
        <n-statistic label="频道总数" :value="statistics.totalChannels">
          <template #prefix>
            <i class="i-mdi:television mr-5"/>
          </template>
        </n-statistic>
      </n-gi>
      <n-gi>
        <n-statistic label="有效频道" :value="statistics.validChannels">
          <template #prefix>
            <i class="i-mdi:check-circle mr-5"/>
          </template>
        </n-statistic>
      </n-gi>
      <n-gi>
        <n-statistic label="无效频道" :value="statistics.invalidChannels">
          <template #prefix>
            <i class="i-mdi:close-circle mr-5"/>
          </template>
        </n-statistic>
      </n-gi>
      <n-gi>
        <n-statistic label="频道组数量" :value="statistics.groupCount">
          <template #prefix>
            <i class="i-mdi:folder-multiple mr-5"/>
          </template>
        </n-statistic>
      </n-gi>
      <n-gi>
        <n-statistic label="状态" :value="statusText">
          <template #prefix>
            <i :class="statusIcon" class="status-icon mr-5 mt-5"/>
          </template>
        </n-statistic>
      </n-gi>
    </n-grid>
  </div>
</template>

<script setup>
import { NStatistic, NGrid, NGi } from 'naive-ui'
import { computed, ref, onMounted, onBeforeUnmount } from 'vue'
import api from './api'
import { createWebSocket } from '@/utils'

// 统计数据
const statistics = ref({
  totalChannels: 0,
  validChannels: 0,
  invalidChannels: 0,
  groupCount: 0,
  status: 'NOT_RUNNING',
})

// WebSocket
const websocket = ref(null)

// 状态图标
const statusIcon = computed(() => {
  if (statistics.value.status === 'RUNNING') {
    return 'i-mdi:human-scooter'
  }
  return 'i-mdi:walk'
})

// 状态文本
const statusText = computed(() => {
  if (statistics.value.status === 'RUNNING') {
    return '清洗中'
  }
  return '未运行'
})

// 获取统计数据
async function fetchStatistics() {
  try {
    const res = await api.getStatistic()
    statistics.value = {
      totalChannels: res.data.totalChannels || 0,
      validChannels: res.data.validChannels || 0,
      invalidChannels: res.data.invalidChannels || 0,
      groupCount: res.data.groupCount || 0,
      status: res.data.status || 'NOT_RUNNING',
    }
  } catch (error) {
    $message.error('获取统计数据失败')
    console.error(error)
  }
}

// 设置 WebSocket
function setupWebsocket() {
  let ws = createWebSocket('/channel/cleanup/status')

  ws.onmessage = (e) => {
    let task = JSON.parse(e.data)
    statistics.value.status = task.status
  }

  ws.onclose = async (e) => {
    console.debug('websocket closed')
    websocket.value = null
    await new Promise(resolve => setTimeout(resolve, 3000))
    setupWebsocket()
  }

  console.debug('setupWebsocket')
  websocket.value = ws
}

// 获取清洗状态
function getStatus() {
  return statistics.value.status === 'RUNNING'
}

onMounted(() => {
  fetchStatistics()
  setupWebsocket()
})

onBeforeUnmount(() => {
  if (websocket.value) {
    websocket.value.close()
  }
})

// 暴露方法给父组件
defineExpose({
  refresh: fetchStatistics,
  getStatus,
})
</script>

<style scoped>
.statistics-area {
  margin-bottom: 16px;
  padding: 16px;
  background-color: var(--n-color-modal);
  border-radius: 4px;
}
</style>
