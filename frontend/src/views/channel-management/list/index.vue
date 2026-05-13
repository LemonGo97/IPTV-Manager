<template>
  <CommonPage>
    <!-- 统计数据区域 -->
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
          <n-statistic label="状态" :value="statistics.status">
            <template #prefix>
              <i :class="statusIcon" class="status-icon mr-5 mt-5"/>
            </template>
          </n-statistic>
        </n-gi>
      </n-grid>
    </div>

    <!-- 处理步骤区域 -->
    <div class="steps-area">
      <n-steps :current="currentStep" :status="stepStatus">
        <n-step title="频道过滤" description="根据规则过滤无效和测试频道"/>
        <n-step title="名称规范化" description="统一频道名称格式和标识"/>
        <n-step title="延迟检测" description="检测各源的延迟情况"/>
        <n-step title="频道分组" description="按类别整理归类频道"/>
        <n-step title="信号评分" description="评估源信号质量并打分"/>
      </n-steps>
    </div>

    <n-divider/>

    <!-- 操作按钮区域 -->
    <div class="action-area">
      <n-space>
        <n-button type="primary" @click="handleRefresh">
          <i class="i-material-symbols:refresh mr-4 text-18"/>
          刷新
        </n-button>
        <n-button type="success" @click="handleStartProcessing">
          <i class="i-material-symbols:play-arrow mr-4 text-18"/>
          开始处理
        </n-button>
      </n-space>
    </div>

    <!-- 数据表格 -->
    <n-data-table
      :columns="columns"
      :data="tableData"
      :pagination="pagination"
      :scroll-x="1400"
      size="small"
    />

    <!-- EPG弹窗 -->
    <n-modal
      v-model:show="epgModalVisible"
      preset="card"
      :title="`EPG节目单 - ${epgChannelName}`"
      :style="{ width: '600px', maxHeight: '70vh' }"
    >
      <div class="epg-container" ref="epgContainerRef">
        <n-timeline>
          <n-timeline-item
            v-for="item in epgTimelineData"
            :key="item.id"
            :type="item.type"
            :title="item.title"
            :time="item.time"
            :content="item.name"
            :class="{ 'current-program-item': item.isCurrent }"
          />
        </n-timeline>
      </div>
    </n-modal>

    <!-- 预览弹窗 -->
    <n-modal
      v-model:show="previewModalVisible"
      preset="card"
      :title="`确认使用 ${previewPlayerName} 预览`"
      :style="{ width: '500px' }"
    >
      <div v-if="previewChannel" class="preview-modal-content">
        <n-p class="text-16 mb-4">确定使用 {{ previewPlayerName }} 预览「{{ previewChannel.name }}」吗？</n-p>
        <n-p class="text-14 opacity-70 url-text">{{ previewChannel.url }}</n-p>
      </div>
      <template #action>
        <n-space class="mt-4 flex justify-end" :size="16">
          <n-button @click="previewModalVisible = false">
            关闭
          </n-button>
          <n-button @click="handleCopyUrl">
            复制链接
          </n-button>
          <n-button type="primary" @click="handleOpenPlayer">
            打开 {{ previewPlayerName }}
          </n-button>
        </n-space>
      </template>
    </n-modal>
  </CommonPage>
</template>

<script setup>
import {NButton, NTag, NStatistic, NGrid, NGi, NDivider, NDataTable, NSteps, NStep, NModal, NTimeline, NTimelineItem, NImage, NDropdown, NP, NSpace} from 'naive-ui'
import {h} from 'vue'
import { useClipboard } from '@vueuse/core'

// 使用剪贴板
const { copy, copied } = useClipboard()

// 监听复制状态
watch(copied, (val) => {
  if (val) {
    window.$message.success('已复制到剪贴板')
  }
})

// 统计数据（模拟数据）
const statistics = ref({
  totalChannels: 1234,
  validChannels: 1156,
  invalidChannels: 78,
  groupCount: 12,
  status: '进行中',
})

// 状态图标
const statusIcon = computed(() => {
  if (statistics.value.status === '进行中') {
    return 'i-mdi:human-scooter'
  }
  return 'i-mdi:walk'
})

// 当前步骤（模拟）
const currentStep = ref(3)
const stepStatus = ref('process') // process, finish, error, wait

// EPG弹窗相关
const epgModalVisible = ref(false)
const epgChannelName = ref('')
const epgContainerRef = ref(null)

// 预览弹窗相关
const previewModalVisible = ref(false)
const previewChannel = ref(null)
const previewPlayerKey = ref('')

// EPG时间轴数据（扁平结构）
const epgTimelineData = ref([])

// 真实EPG节目数据（从 backend/tmp/epg_cctv1_example.xml 提取）
// 时间格式：YYYY-MM-DD HH:mm:ss
const REAL_EPG_PROGRAMS = [
  // 2026-05-14 (星期四)
  { start: '2026-05-14 00:52:00', end: '2026-05-14 01:22:00', name: '晚间新闻' },
  { start: '2026-05-14 01:22:00', end: '2026-05-14 02:04:00', name: '生活早参考-特别节目（生活圈）2026-128' },
  { start: '2026-05-14 02:04:00', end: '2026-05-14 02:34:00', name: '农耕探文明-2025-20' },
  { start: '2026-05-14 02:34:00', end: '2026-05-14 02:38:00', name: '三餐四季（第二季）-宣传片' },
  { start: '2026-05-14 02:38:00', end: '2026-05-14 02:44:00', name: '非遗里的中国-MV' },
  { start: '2026-05-14 02:44:00', end: '2026-05-14 04:20:00', name: '宗师列传·大宋词人传-黄庭坚' },
  { start: '2026-05-14 04:20:00', end: '2026-05-14 04:22:00', name: '泱泱中华-历史文化街区1' },
  { start: '2026-05-14 04:22:00', end: '2026-05-14 04:53:00', name: '今日说法-2026-82' },
  { start: '2026-05-14 04:53:00', end: '2026-05-14 05:27:00', name: '新闻联播' },
  { start: '2026-05-14 05:27:00', end: '2026-05-14 06:00:00', name: '寻古中国-寻秦记4' },
  { start: '2026-05-14 06:00:00', end: '2026-05-14 08:30:00', name: '朝闻天下' },
  { start: '2026-05-14 08:30:00', end: '2026-05-14 09:00:00', name: '朝闻天下' },
  { start: '2026-05-14 09:00:00', end: '2026-05-14 09:30:00', name: '新闻直播间' },
  { start: '2026-05-14 09:30:00', end: '2026-05-14 10:45:00', name: '新闻直播间' },
  { start: '2026-05-14 10:45:00', end: '2026-05-14 11:00:00', name: '中华古树-黄山迎客松（4K）' },
  { start: '2026-05-14 11:00:00', end: '2026-05-14 11:50:00', name: '爱情没有神话第11集' },
  { start: '2026-05-14 11:50:00', end: '2026-05-14 11:51:00', name: '中华古树-绿色国宝（4K）' },
  { start: '2026-05-14 11:51:00', end: '2026-05-14 11:54:00', name: '非遗里的中国-MV' },
  { start: '2026-05-14 11:54:00', end: '2026-05-14 12:00:00', name: '秘境之眼-2026-113' },
  { start: '2026-05-14 12:00:00', end: '2026-05-14 12:34:00', name: '新闻30分' },
  { start: '2026-05-14 12:34:00', end: '2026-05-14 13:07:00', name: '今日说法-2026-83' },
  { start: '2026-05-14 13:07:00', end: '2026-05-14 13:54:00', name: '生命树第18集' },
  { start: '2026-05-14 13:54:00', end: '2026-05-14 14:42:00', name: '生命树第19集' },
  { start: '2026-05-14 14:42:00', end: '2026-05-14 15:30:00', name: '生命树第20集' },
  { start: '2026-05-14 15:30:00', end: '2026-05-14 16:20:00', name: '生命树第21集' },
  { start: '2026-05-14 16:20:00', end: '2026-05-14 17:12:00', name: '生命树第22集' },
  { start: '2026-05-14 17:12:00', end: '2026-05-14 17:37:00', name: '第一动画乐园-2026-128' },
  { start: '2026-05-14 17:37:00', end: '2026-05-14 18:21:00', name: '生活早参考-特别节目（生活圈）2026-129' },
  { start: '2026-05-14 18:21:00', end: '2026-05-14 18:51:00', name: '农耕探文明-2025-6' },
  { start: '2026-05-14 18:51:00', end: '2026-05-14 19:00:00', name: '秘境之眼-2026-127' },
  { start: '2026-05-14 19:00:00', end: '2026-05-14 19:38:00', name: '新闻联播' },
  { start: '2026-05-14 19:38:00', end: '2026-05-14 19:58:00', name: '焦点访谈' },
  { start: '2026-05-14 19:58:00', end: '2026-05-14 20:02:00', name: '前情提要-主角-9/48' },
  { start: '2026-05-14 20:02:00', end: '2026-05-14 20:53:00', name: '主角9/48' },
  { start: '2026-05-14 20:53:00', end: '2026-05-14 20:56:00', name: '前情提要-主角-10/48' },
  { start: '2026-05-14 20:56:00', end: '2026-05-14 21:46:00', name: '主角10/48' },
  { start: '2026-05-14 21:46:00', end: '2026-05-14 21:52:00', name: '非遗里的中国-MV' },
  { start: '2026-05-14 21:52:00', end: '2026-05-14 22:00:00', name: '三餐四季（第二季）-宣传片' },
  { start: '2026-05-14 22:00:00', end: '2026-05-14 22:35:00', name: '晚间新闻' },
  { start: '2026-05-14 22:35:00', end: '2026-05-14 23:10:00', name: '自然中国-探秘哀牢山-奇幻生灵' },
  { start: '2026-05-14 23:10:00', end: '2026-05-14 23:59:00', name: '宗师列传·大宋词人传-秦观' },
]

// 从真实EPG数据生成时间轴数据
function generateEpgData() {
  const now = new Date()
  const timelineItems = []
  let currentIndex = -1
  let lastDate = null

  REAL_EPG_PROGRAMS.forEach((prog) => {
    const startTime = new Date(prog.start)
    const endTime = new Date(prog.end)

    // 检查是否需要添加日期节点
    if (!lastDate || !isSameDay(lastDate, startTime)) {
      const today = new Date()
      today.setHours(0, 0, 0, 0)
      const progDate = new Date(startTime)
      progDate.setHours(0, 0, 0, 0)

      let dayType = 'default'
      if (progDate.getTime() === today.getTime()) {
        dayType = 'success'
      } else if (progDate < today) {
        dayType = 'info'
      }

      timelineItems.push({
        id: timelineItems.length,
        contentType: 'date',
        title: formatDate(startTime),
        type: dayType,
      })
      lastDate = startTime
    }

    // 判断是否当前播放
    const isCurrent = now >= startTime && now < endTime
    if (isCurrent) {
      currentIndex = timelineItems.length
    }

    // 判断节目状态
    let programType = 'default'
    if (isCurrent) {
      programType = 'success'
    } else if (endTime < now) {
      programType = 'info'
    }

    // 添加节目节点
    timelineItems.push({
      id: timelineItems.length,
      contentType: 'program',
      title: undefined,
      time: `${formatTime(startTime)} - ${formatTime(endTime)}`,
      name: prog.name,
      type: programType,
      isCurrent,
    })
  })

  epgTimelineData.value = timelineItems
  return currentIndex
}

// 判断是否同一天
function isSameDay(date1, date2) {
  return (
    date1.getFullYear() === date2.getFullYear() &&
    date1.getMonth() === date2.getMonth() &&
    date1.getDate() === date2.getDate()
  )
}

// 格式化时间显示
function formatTime(date) {
  return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

// 格式化日期显示
function formatDate(date) {
  const month = date.getMonth() + 1
  const day = date.getDate()
  const weekDay = ['日', '一', '二', '三', '四', '五', '六'][date.getDay()]
  return `${month}月${day}日 星期${weekDay}`
}

// 频道组选项（用于表头过滤）
const groupFilterOptions = ref([
  {label: '央视', value: '央视'},
  {label: '卫视', value: '卫视'},
  {label: '地方台', value: '地方台'},
  {label: '国际', value: '国际'},
])

// 状态选项（用于表头过滤）
const statusFilterOptions = ref([
  {label: '有效', value: 'valid'},
  {label: '无效', value: 'invalid'},
])

// 国家选项（用于表头过滤）
const countryFilterOptions = ref([
  {label: '中国', value: '中国'},
  {label: '美国', value: '美国'},
  {label: '日本', value: '日本'},
  {label: '韩国', value: '韩国'},
  {label: '英国', value: '英国'},
])

// 模拟数据
const tableData = ref([
  {
    id: 1,
    logo: 'https://tb.zbds.top/logo/CCTV1.png',
    name: 'CCTV-1',
    groupName: '央视',
    url: 'https://piccpndali.v.myalicdn.com/audio/cctv1_2.m3u8',
    country: '中国',
    language: '中文',
    status: 'valid',
  },
  {
    id: 2,
    logo: 'https://tb.zbds.top/logo/湖南卫视.png',
    name: '湖南卫视',
    groupName: '卫视',
    url: 'http://hlsal-ldvt.qing.mgtv.com/nn_live/nn_x64/dWlwPTEyNy4wLjAuMSZ1aWQ9cWluZy1jbXMmbm5fdGltZXpvbmU9OCZjZG5leF9pZD1hbF9obHNfbGR2dCZ1dWlkPTliODY4NmU5ZTM2YzYwMmMmZT02OTE0NjA0JnY9MSZpZD1ITldTWkdTVCZzPTcwN2RiYTc2YzJjNmJmMTQ4MmUyZGYzOWU2NWM3YWFi/HNWSZGST.m3u8',
    country: '中国',
    language: '中文',
    status: 'valid',
  },
  {
    id: 3,
    logo: 'https://tb.zbds.top/logo/CCTV17.png',
    name: 'BBC One',
    groupName: '国际',
    url: 'https://piccpndali.v.myalicdn.com/audio/cctv17_2.m3u8',
    country: '英国',
    language: '英语',
    status: 'valid',
  },
  {
    id: 4,
    logo: 'https://tb.zbds.top/logo/CCTV13.png',
    name: 'NHK World',
    groupName: '国际',
    url: 'https://piccpndali.v.myalicdn.com/audio/cctv13_2.m3u8',
    country: '日本',
    language: '日语',
    status: 'invalid',
  },
  {
    id: 5,
    logo: 'https://tb.zbds.top/logo/CGTN.png',
    name: 'KBS 1TV',
    groupName: '国际',
    url: 'https://0472.org/hls/cgtn.m3u8',
    country: '韩国',
    language: '韩语',
    status: 'valid',
  },
])

// 分页配置
const pagination = reactive({
  page: 1,
  pageSize: 10,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100],
  showQuickJumper: true,
  prefix: ({itemCount}) => `共 ${itemCount} 条`,
})

// 表格列定义（带过滤功能）
const columns = [
  {title: 'ID', key: 'id', width: 80},
  {
    title: '频道LOGO',
    key: 'logo',
    width: 120,
    render: row =>
      h(NImage, {
        src: row.logo || '/placeholder-logo.png',
        alt: row.name,
        width: 80,
        height: 50,
        previewDisabled: true,
        objectFit: 'contain',
        style: {
          borderRadius: '4px',
          backgroundColor: 'rgba(0, 0, 0, 0.06)',
          padding: '4px',
          boxSizing: 'content-box',
        },
      }),
  },
  {
    title: '频道名称',
    key: 'name',
    width: 200,
    ellipsis: {tooltip: true},
    filter: true,
    filterOptions: tableData.value.map(item => ({label: item.name, value: item.name})),
  },
  {
    title: '频道组',
    key: 'groupName',
    width: 120,
    filter: true,
    filterOptions: groupFilterOptions.value,
  },
  {
    title: '播放地址',
    key: 'url',
    width: 300,
    ellipsis: {tooltip: true},
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
  {
    title: '国家',
    key: 'country',
    width: 100,
    filter: true,
    filterOptions: countryFilterOptions.value,
  },
  {title: '语言', key: 'language', width: 100},
  {
    title: '状态',
    key: 'status',
    width: 100,
    filter: true,
    filterOptions: statusFilterOptions.value,
    render: row =>
      h(
        NTag,
        {type: row.status === 'valid' ? 'success' : 'error'},
        {default: () => (row.status === 'valid' ? '有效' : '无效')}
      ),
  },
  {
    title: '操作',
    key: 'actions',
    width: 180,
    fixed: 'right',
    render: row =>
      h('div', {class: 'flex gap-8'}, [
        h(NDropdown, {
          options: [
            {label: 'IINA', key: 'iina'},
            {label: 'PotPlayer', key: 'potplayer'},
            {label: 'VLC', key: 'vlc'},
          ],
          onSelect: (key) => handlePreview(key, row),
        }, {
          default: () =>
            h(
              NButton,
              {size: 'small'},
              {default: () => '预览'}
            ),
        }),
        h(
          NButton,
          {size: 'small', type: 'info', onClick: () => handleViewEpg(row)},
          {default: () => '查看EPG'}
        ),
      ]),
  },
]

// 复制到剪贴板
function copyToClipboard(text) {
  copy(text)
}

// 刷新
function handleRefresh() {
  window.$message.info('刷新功能开发中...')
}

// 开始处理
function handleStartProcessing() {
  window.$message.info('开始处理功能开发中...')
}

// 播放器名称映射
const playerNames = {
  iina: 'IINA',
  potplayer: 'PotPlayer',
  vlc: 'VLC',
}

// 当前选中的播放器名称
const previewPlayerName = computed(() => playerNames[previewPlayerKey.value] || '')

// 预览
function handlePreview(player, row) {
  previewPlayerKey.value = player
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
  window.$message.success(`正在使用 ${playerNames[player]} 打开：${row.name}`)
  previewModalVisible.value = false
}

// 下载 .strm 文件（被 PotPlayer、VLC 等播放器识别）
function downloadStrmFile(row) {
  const content = row.url
  const blob = new Blob([content], { type: 'text/plain' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${row.name}.strm`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}

// 查看EPG
function handleViewEpg(row) {
  epgChannelName.value = row.name
  const currentIndex = generateEpgData()
  epgModalVisible.value = true

  // 弹窗打开后滚动到当前节目
  nextTick(() => {
    if (currentIndex >= 0 && epgContainerRef.value) {
      const items = epgContainerRef.value.querySelectorAll('.n-timeline-item')
      const currentItem = items[currentIndex]
      if (currentItem) {
        currentItem.scrollIntoView({ behavior: 'smooth', block: 'center' })
      }
    }
  })
}

defineOptions({
  name: 'ChannelList',
})
</script>

<style scoped>
.statistics-area {
  margin-bottom: 16px;
  padding: 16px;
  background-color: var(--n-color-modal);
  border-radius: 4px;
}

.steps-area {
  margin-bottom: 16px;
  padding: 16px;
  background-color: var(--n-color-modal);
  border-radius: 4px;
}

.action-area {
  margin-bottom: 16px;
  display: flex;
  justify-content: flex-end;
}

.channel-logo {
  border-radius: 4px;
  background-color: rgba(0, 0, 0, 0.06);
  padding: 4px;
  box-sizing: content-box;
}

.statistic-value {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
}

.statistic-icon {
  font-size: 18px;
}

.status-display {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
}

.status-processing .status-icon {
  color: #18a058;
  animation: scoot 0.5s ease-in-out infinite;
}

@keyframes scoot {
  0%, 100% {
    transform: translate(0, 0);
  }
  25% {
    transform: translate(-2px, -1px);
  }
  50% {
    transform: translate(2px, 1px);
  }
  75% {
    transform: translate(-1px, 2px);
  }
}

.epg-container {
  max-height: 60vh;
  overflow-y: auto;
}

.current-program-item :deep(.n-timeline-item-timeline__line) {
  background-color: #18a058;
}

.epg-time {
  font-size: 12px;
  color: var(--n-text-color-2);
  margin-top: 4px;
}

.epg-program {
  display: flex;
  align-items: center;
  gap: 8px;
}

.epg-current {
  font-weight: 500;
}

.program-current-tag {
  font-size: 12px;
  padding: 2px 8px;
  background-color: #18a058;
  color: white;
  border-radius: 4px;
}
</style>
