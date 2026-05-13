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
              <i :class="statusIcon" class="status-icon mr-5"/>
            </template>
          </n-statistic>
        </n-gi>
      </n-grid>
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
    <MeCrud
      ref="$table"
      v-model:query-items="queryItems"
      :columns="columns"
      :get-data="getData"
      :scroll-x="1400"
    >
      <!-- 查询条件 -->
      <MeQueryItem label="频道名称" :label-width="80">
        <n-input v-model:value="queryItems.name" placeholder="请输入频道名称" clearable/>
      </MeQueryItem>
      <MeQueryItem label="频道组" :label-width="60">
        <n-select
          v-model:value="queryItems.groupId"
          placeholder="请选择频道组"
          clearable
          :options="groupOptions"
        />
      </MeQueryItem>
      <MeQueryItem label="国家" :label-width="60">
        <n-input v-model:value="queryItems.country" placeholder="请输入国家" clearable/>
      </MeQueryItem>
      <MeQueryItem label="状态" :label-width="60">
        <n-select
          v-model:value="queryItems.status"
          placeholder="请选择状态"
          clearable
          :options="[
            { label: '有效', value: 'valid' },
            { label: '无效', value: 'invalid' },
          ]"
        />
      </MeQueryItem>
    </MeCrud>
  </CommonPage>
</template>

<script setup>
import {NButton, NTag, NStatistic, NGrid, NGi, NDivider} from 'naive-ui'
import {MeCrud, MeQueryItem} from '@/components'
import {h} from 'vue'

const $table = ref(null)

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

// 查询条件
const queryItems = reactive({
  name: '',
  groupId: null,
  country: '',
  status: null,
})

// 频道组选项（模拟数据）
const groupOptions = ref([
  {label: '央视', value: 1},
  {label: '卫视', value: 2},
  {label: '地方台', value: 3},
  {label: '国际', value: 4},
])

// 表格列定义
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
  {title: '频道名称', key: 'name', width: 200, ellipsis: {tooltip: true}},
  {title: '频道组', key: 'groupName', width: 120},
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
          onClick: (e) => {
            e.preventDefault()
            copyToClipboard(row.url)
          },
        },
        row.url
      ),
  },
  {title: '国家', key: 'country', width: 100},
  {title: '语言', key: 'language', width: 100},
  {
    title: '状态',
    key: 'status',
    width: 100,
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

// 获取数据（模拟）
async function getData(params) {
  // 模拟数据
  return {
    code: 200,
    data: {
      records: [
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
      ],
      total: 100,
      size: 10,
      current: 1,
    },
  }
}

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
  window.$message.info(`查看 ${row.name} 的EPG信息`)
}

onMounted(() => {
  $table.value?.handleSearch()
})

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
</style>
