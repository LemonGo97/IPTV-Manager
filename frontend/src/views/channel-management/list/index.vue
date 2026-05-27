<template>
  <CommonPage>
    <!-- 统计数据区域 -->
    <StatisticsArea :statistics="statistics" />

    <!-- 处理步骤区域 -->
    <StepsArea :current-step="currentStep" :step-status="stepStatus" />

    <n-divider />

    <!-- 搜索和操作栏 -->
    <SearchBar
      :steps-area-loading-status="stepsAreaLoadingStatus"
      @search="handleSearch"
      @refresh="handleRefresh"
      @start-processing="handleStartProcessing"
      @step-press-select="handleStepPressSelect"
    />

    <!-- 数据表格 -->
    <DataTable
      ref="tableRef"
      :search-params="queryItems"
      @view-epg="handleViewEpg"
      @update-filters="handleFiltersChange"
      @preview="handlePreview"
    />

    <!-- EPG弹窗 -->
    <EpgModal
      v-model:visible="epgModalVisible"
      :channel-name="epgChannelName"
      :timeline-data="epgTimelineData"
    />

    <!-- 预览弹窗 -->
    <PreviewModal
      v-model:visible="previewModalVisible"
      :channel="previewChannel"
      :player-name="previewPlayerName"
      @copy-url="handleCopyUrl"
      @open-player="handleOpenPlayer"
    />
  </CommonPage>
</template>

<script setup>
import { NDivider } from 'naive-ui'
import { computed, onMounted, onBeforeUnmount, reactive, ref } from 'vue'
import { useClipboard } from '@vueuse/core'
import StatisticsArea from './StatisticsArea.vue'
import StepsArea from './StepsArea.vue'
import SearchBar from './SearchBar.vue'
import DataTable from './DataTable.vue'
import EpgModal from './EpgModal.vue'
import PreviewModal from './PreviewModal.vue'
import api from './api'
import { createWebSocket } from '@/utils'

// 使用剪贴板
const { copy } = useClipboard()

// 统计数据
const statistics = ref({
  totalChannels: 0,
  validChannels: 0,
  invalidChannels: 0,
  groupCount: 0,
  status: 'NOT_RUNNING',
})

// 当前步骤（模拟）
const currentStep = ref(3)
const stepStatus = ref('process') // process, finish, error, wait
const stepsAreaLoadingStatus = ref(false)

// EPG弹窗相关
const epgModalVisible = ref(false)
const epgChannelName = ref('')
const epgTimelineData = ref([])

// 预览弹窗相关
const previewModalVisible = ref(false)
const previewChannel = ref(null)
const previewPlayerKey = ref('')

// Websocket
const websocket = ref(null)

// 表格引用
const tableRef = ref(null)

// 搜索参数
const queryItems = reactive({
  name: '',
  providerId: null,
  groupId: null,
  status: null,
})

// 播放器名称映射
const playerNames = {
  iina: 'IINA',
  potplayer: 'PotPlayer',
  vlc: 'VLC',
}

// 当前选中的播放器名称
const previewPlayerName = computed(() => playerNames[previewPlayerKey.value] || '')

// 获取统计数据
async function fetchStatistics() {
  try {
    const res = await api.getStatistic()
    stepsAreaLoadingStatus.value = res.data.status === 'RUNNING'
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
    stepsAreaLoadingStatus.value = task.status === 'RUNNING'
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

// 格式化时间显示
function formatTime(date) {
  return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

// 处理过滤器变化
function handleFiltersChange(filters) {
  queryItems.providerId = filters['provider.name'] || null
  queryItems.groupId = filters['channelGroup.name'] || null
  queryItems.status = filters['status'] || null
}

// 搜索
function handleSearch(searchParams) {
  if (searchParams) {
    Object.assign(queryItems, searchParams)
  }
}

// 刷新
function handleRefresh() {
  fetchStatistics()
  tableRef.value?.refresh()
}

// 开始处理
async function handleStartProcessing() {
  stepsAreaLoadingStatus.value = true
  await api.dataClean()
  $message.info('频道列表开始数据清洗并生成新的频道列表中...')
}

// 分步处理
async function handleStepPressSelect(key) {
  stepsAreaLoadingStatus.value = true
  await api.dataClean(key)
  $message.info('频道列表开始数据清洗并生成新的频道列表中...')
}

// 预览
function handlePreview({ key, row }) {
  previewPlayerKey.value = key
  previewChannel.value = row
  previewModalVisible.value = true
}

// 复制链接
function handleCopyUrl() {
  if (previewChannel.value) {
    copy(previewChannel.value.url)
  }
}

// 打开播放器
function handleOpenPlayer() {
  if (!previewChannel.value) return

  const player = previewPlayerKey.value
  const row = previewChannel.value

  // 播放器 URL scheme 映射
  const playerSchemes = {
    iina: (url) => `iina://weblink?url=${encodeURIComponent(url)}`,
    potplayer: (url) => `potplayer:${url}`,
    vlc: (url) => `vlc://${encodeURIComponent(url)}`,
  }

  const schemeBuilder = playerSchemes[player]
  if (!schemeBuilder) return

  const playerUrl = schemeBuilder(row.url)
  window.open(playerUrl, '_blank')
  $message.success(`正在使用 ${playerNames[player]} 打开：${row.name}`)
  previewModalVisible.value = false
}

// 查看EPG
async function handleViewEpg(row) {
  epgChannelName.value = row.name

  try {
    const res = await api.getTimeline(row.id)
    const timelineData = res.data

    const now = new Date()
    let currentIndex = -1

    for (let i = 0; i < timelineData.length; i++) {
      let item = timelineData[i]
      item.index = i

      const startTime = new Date(item.startTime)
      const endTime = new Date(item.stopTime)

      if (item.type === 'program') {
        item.time = `${formatTime(startTime)} - ${formatTime(endTime)}`
        item.isCurrent = now >= startTime && now < endTime
        item.played = now > endTime
        if (item.isCurrent) {
          currentIndex = i
        }
      }
    }

    epgTimelineData.value = timelineData
    epgModalVisible.value = true
  } catch (error) {
    $message.error('获取EPG数据失败')
    console.error(error)
  }
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

defineOptions({
  name: 'ChannelList',
})
</script>
