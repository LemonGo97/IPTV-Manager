<template>
  <CommonPage>
    <MeCrud
      ref="$table"
      v-model:query-items="queryItems"
      :scroll-x="1600"
      :columns="columns"
      :get-data="api.getAll"
    >
      <MeQueryItem label="任务ID" :label-width="60">
        <n-input
          v-model:value="queryItems.taskId"
          type="text"
          placeholder="请输入任务ID"
          clearable
        />
      </MeQueryItem>

      <MeQueryItem label="订阅源" :label-width="60">
        <n-input
          v-model:value="queryItems.providerName"
          type="text"
          placeholder="请输入订阅源名称"
          clearable
        />
      </MeQueryItem>

      <MeQueryItem label="触发方式" :label-width="70">
        <n-select
          v-model:value="queryItems.triggerType"
          clearable
          :options="[
            { label: '手动刷新', value: 'manual' },
            { label: '定时任务', value: 'scheduled' },
          ]"
        />
      </MeQueryItem>

      <MeQueryItem label="状态" :label-width="50">
        <n-select
          v-model:value="queryItems.status"
          clearable
          :options="[
            { label: '成功', value: 'success' },
            { label: '失败', value: 'failed' },
            { label: '进行中', value: 'running' },
          ]"
        />
      </MeQueryItem>

      <MeQueryItem label="时间范围" :label-width="70">
        <n-date-picker
          v-model:value="queryItems.dateRange"
          type="datetimerange"
          clearable
          class="w-full"
        />
      </MeQueryItem>
    </MeCrud>

    <!-- 任务详情弹窗 -->
    <MeModal ref="detailModalRef" width="800px" title="任务详情" :show-footer="false">
      <n-descriptions bordered :column="2" v-if="currentTask">
        <n-descriptions-item label="任务ID">
          {{ currentTask.id }}
        </n-descriptions-item>
        <n-descriptions-item label="订阅源">
          {{ currentTask.providerName }}
        </n-descriptions-item>
        <n-descriptions-item label="触发方式">
          <n-tag :type="currentTask.triggerType === 'manual' ? 'info' : 'warning'" size="small">
            {{ currentTask.triggerType === 'manual' ? '手动刷新' : '定时任务' }}
          </n-tag>
        </n-descriptions-item>
        <n-descriptions-item label="执行状态">
          <n-tag
            :type="getStatusType(currentTask.status)"
            size="small"
          >
            {{ getStatusText(currentTask.status) }}
          </n-tag>
        </n-descriptions-item>
        <n-descriptions-item label="开始时间">
          {{ currentTask.startTime }}
        </n-descriptions-item>
        <n-descriptions-item label="结束时间">
          {{ currentTask.endTime || '-' }}
        </n-descriptions-item>
        <n-descriptions-item label="耗时">
          {{ currentTask.duration ? `${currentTask.duration}ms` : '-' }}
        </n-descriptions-item>
        <n-descriptions-item label="解析频道数">
          {{ currentTask.channelCount ?? '-' }}
        </n-descriptions-item>
        <n-descriptions-item label="错误信息" :span="2" v-if="currentTask.error">
          <n-alert type="error" :bordered="false">
            {{ currentTask.error }}
          </n-alert>
        </n-descriptions-item>
        <n-descriptions-item label="原始内容" :span="2" v-if="currentTask.rawContent">
          <n-scrollbar style="max-height: 200px">
            <n-code :code="currentTask.rawContent" language="plaintext" />
          </n-scrollbar>
        </n-descriptions-item>
      </n-descriptions>
    </MeModal>

    <!-- 播放列表预览弹窗 -->
    <MeModal ref="playlistModalRef" width="900px" title="频道列表" :show-footer="false">
      <n-data-table
        :columns="playlistColumns"
        :data="currentPlaylist"
        :pagination="{ pageSize: 20 }"
        :scroll-x="1200"
        size="small"
      />
    </MeModal>
  </CommonPage>
</template>

<script setup>
import { NTag, NAlert, NCode, NScrollbar, NDataTable, NButton, NDescriptions, NDescriptionsItem, NDatePicker } from 'naive-ui'
import { MeCrud, MeModal, MeQueryItem } from '@/components'
import api from './api'

const queryItems = reactive({
  taskId: '',
  providerName: '',
  triggerType: null,
  status: null,
  dateRange: null,
})

const detailModalRef = ref(null)
const playlistModalRef = ref(null)
const currentTask = ref(null)
const currentPlaylist = ref([])

const columns = [
  { title: '任务ID', key: 'id', width: 120, render: row => row.id || '-' },
  { title: '订阅源', key: 'providerName', width: 150, ellipsis: { tooltip: true } },
  {
    title: '触发方式',
    key: 'triggerType',
    width: 100,
    render: row =>
      h(
        NTag,
        { type: row.triggerType === 'manual' ? 'info' : 'warning', size: 'small' },
        { default: () => (row.triggerType === 'manual' ? '手动刷新' : '定时任务') }
      ),
  },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render: row =>
      h(
        NTag,
        { type: getStatusType(row.status), size: 'small' },
        { default: () => getStatusText(row.status) }
      ),
  },
  { title: '开始时间', key: 'startTime', width: 180 },
  { title: '结束时间', key: 'endTime', width: 180, render: row => row.endTime || '-' },
  {
    title: '耗时',
    key: 'duration',
    width: 100,
    render: row => (row.duration ? `${row.duration}ms` : '-'),
  },
  {
    title: '频道数',
    key: 'channelCount',
    width: 80,
    render: row => row.channelCount ?? '-',
  },
  {
    title: '操作',
    key: 'actions',
    width: 180,
    fixed: 'right',
    render: row =>
      h('div', { class: 'flex items-center gap-8' }, [
        h(
          NButton,
          {
            size: 'small',
            onClick: () => handleViewDetail(row),
          },
          { default: () => '详情' }
        ),
        h(
          NButton,
          {
            size: 'small',
            type: 'primary',
            disabled: row.status !== 'success',
            onClick: () => handleViewPlaylist(row),
          },
          { default: () => '频道列表' }
        ),
      ]),
  },
]

const playlistColumns = [
  { title: '序号', key: 'index', width: 80 },
  { title: '频道名称', key: 'name', width: 200, ellipsis: { tooltip: true } },
  { title: '分组', key: 'group', width: 120 },
  { title: 'URL', key: 'url', width: 400, ellipsis: { tooltip: true } },
  { title: 'Logo', key: 'logo', width: 120, ellipsis: { tooltip: true } },
]

const getStatusType = (status) => {
  const types = {
    success: 'success',
    failed: 'error',
    running: 'warning',
  }
  return types[status] || 'default'
}

const getStatusText = (status) => {
  const texts = {
    success: '成功',
    failed: '失败',
    running: '进行中',
  }
  return texts[status] || '未知'
}

const handleViewDetail = (row) => {
  currentTask.value = row
  detailModalRef.value.show()
}

const handleViewPlaylist = (row) => {
  // TODO: 获取频道列表数据
  currentPlaylist.value = row.channels || []
  playlistModalRef.value.show()
}

defineOptions({
  name: 'TaskHistory',
})
</script>
