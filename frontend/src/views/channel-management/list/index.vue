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
  </CommonPage>
</template>

<script setup>
import {NButton, NTag, NStatistic, NGrid, NGi, NDivider, NDataTable, NSteps, NStep, NModal, NTimeline, NTimelineItem} from 'naive-ui'
import {h} from 'vue'

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

// EPG时间轴数据（扁平结构）
const epgTimelineData = ref([])

// 生成模拟EPG数据（扁平结构：日期节点和节目节点交替）
function generateEpgData() {
  const now = new Date()
  const timelineItems = []
  let currentIndex = -1

  // 节目模板（循环使用）
  const programTemplates = [
    { name: '新闻早班车', duration: 60 },
    { name: '健康生活', duration: 30 },
    { name: '电视剧场（一）', duration: 120 },
    { name: '午间新闻', duration: 30 },
    { name: '农业天地', duration: 45 },
    { name: '科技之光', duration: 45 },
    { name: '动画乐园', duration: 120 },
    { name: '晚间新闻', duration: 30 },
    { name: '黄金剧场', duration: 120 },
    { name: '体育世界', duration: 60 },
    { name: '星光大道', duration: 90 },
    { name: '夜间影院', duration: 120 },
  ]

  // 从今天开始生成7天的节目单
  for (let day = 0; day < 7; day++) {
    const dayDate = new Date(now)
    dayDate.setDate(dayDate.getDate() + day)
    dayDate.setHours(6, 0, 0, 0)

    // 格式化日期
    const month = dayDate.getMonth() + 1
    const date = dayDate.getDate()
    const weekDay = ['日', '一', '二', '三', '四', '五', '六'][dayDate.getDay()]
    const dateStr = `${month}月${date}日 星期${weekDay}`

    // 添加日期节点
    let dayType = 'default'
    const today = new Date()
    today.setHours(0, 0, 0, 0)
    const compareDate = new Date(dayDate)
    compareDate.setHours(0, 0, 0, 0)

    if (day === 0) dayType = 'success'
    else if (compareDate < today) dayType = 'info'

    timelineItems.push({
      id: timelineItems.length,
      contentType: 'date',
      title: dateStr,
      type: dayType,
    })

    // 生成当天的节目节点
    let currentOffset = 0
    let templateIndex = 0

    while (currentOffset < 22 * 60) {
      const template = programTemplates[templateIndex % programTemplates.length]
      const startTime = new Date(dayDate)
      startTime.setMinutes(startTime.getMinutes() + currentOffset)

      const endTime = new Date(startTime)
      endTime.setMinutes(startTime.getMinutes() + template.duration)

      const isCurrent = now >= startTime && now < endTime
      if (isCurrent) {
        currentIndex = timelineItems.length
      }

      let programType = 'default'
      if (isCurrent) programType = 'success'
      else if (endTime < now) programType = 'info'

      const formatTime = d =>
        `${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`

      timelineItems.push({
        id: timelineItems.length,
        contentType: 'program',
        title: undefined,
        time: `${formatTime(startTime)} - ${formatTime(endTime)}`,
        name: template.name,
        type: programType,
        isCurrent,
      })

      currentOffset += template.duration
      templateIndex++
    }
  }
  epgTimelineData.value = timelineItems
  return currentIndex
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
    logo: 'https://example.com/logo1.png',
    name: 'CCTV-1',
    groupName: '央视',
    url: 'https://example.com/live/cctv1.m3u8',
    country: '中国',
    language: '中文',
    status: 'valid',
  },
  {
    id: 2,
    logo: 'https://example.com/logo2.png',
    name: '湖南卫视',
    groupName: '卫视',
    url: 'https://example.com/live/hunan.m3u8',
    country: '中国',
    language: '中文',
    status: 'valid',
  },
  {
    id: 3,
    logo: 'https://example.com/logo3.png',
    name: 'BBC One',
    groupName: '国际',
    url: 'https://example.com/live/bbc.m3u8',
    country: '英国',
    language: '英语',
    status: 'valid',
  },
  {
    id: 4,
    logo: 'https://example.com/logo4.png',
    name: 'NHK World',
    groupName: '国际',
    url: 'https://example.com/live/nhk.m3u8',
    country: '日本',
    language: '日语',
    status: 'invalid',
  },
  {
    id: 5,
    logo: 'https://example.com/logo5.png',
    name: 'KBS 1TV',
    groupName: '国际',
    url: 'https://example.com/live/kbs.m3u8',
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
    width: 100,
    render: row =>
      h('img', {
        src: row.logo || '/placeholder-logo.png',
        alt: row.name,
        class: 'channel-logo',
        style: {width: '50px', height: '50px', objectFit: 'cover'},
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
    width: 150,
    fixed: 'right',
    render: row =>
      h(
        NButton,
        {size: 'small', type: 'info', onClick: () => handleViewEpg(row)},
        {default: () => '查看EPG'}
      ),
  },
]

// 复制到剪贴板
function copyToClipboard(text) {
  navigator.clipboard.writeText(text).then(() => {
    window.$message.success('已复制到剪贴板')
  })
}

// 刷新
function handleRefresh() {
  window.$message.info('刷新功能开发中...')
}

// 开始处理
function handleStartProcessing() {
  window.$message.info('开始处理功能开发中...')
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
