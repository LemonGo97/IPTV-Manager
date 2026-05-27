<template>
  <n-modal
    :show="props.visible"
    preset="card"
    title="任务详情"
    :style="{ width: '800px' }"
    @update:show="(val) => emit('update:visible', val)"
  >
    <n-descriptions bordered :column="2" v-if="task">
      <n-descriptions-item label="任务ID">
        {{ task.id }}
      </n-descriptions-item>
      <n-descriptions-item label="订阅源">
        {{ task.providerName }}
      </n-descriptions-item>
      <n-descriptions-item label="触发方式">
        <n-tag :type="task.triggerType === 'manual' ? 'info' : 'warning'" size="small">
          {{ task.triggerType === 'manual' ? '手动刷新' : '定时任务' }}
        </n-tag>
      </n-descriptions-item>
      <n-descriptions-item label="执行状态">
        <n-tag
          :type="getStatusType(task.status)"
          size="small"
        >
          {{ getStatusText(task.status) }}
        </n-tag>
      </n-descriptions-item>
      <n-descriptions-item label="开始时间">
        {{ formatTime(task.startTime) }}
      </n-descriptions-item>
      <n-descriptions-item label="结束时间">
        {{ formatTime(task.endTime) }}
      </n-descriptions-item>
      <n-descriptions-item label="耗时">
        {{ formatDuration(task.duration) }}
      </n-descriptions-item>
      <n-descriptions-item label="解析频道数">
        {{ task.channelCount ?? '-' }}
      </n-descriptions-item>
      <n-descriptions-item label="错误信息" :span="2" v-if="task.errorMessage">
        <n-alert type="error" :bordered="false">
          {{ task.errorMessage }}
        </n-alert>
      </n-descriptions-item>
      <n-descriptions-item label="原始内容" :span="2" v-if="task.rawContent">
        <n-scrollbar style="max-height: 200px">
          <n-code :code="task.rawContent" language="plaintext" />
        </n-scrollbar>
      </n-descriptions-item>
    </n-descriptions>
  </n-modal>
</template>

<script setup>
import { NModal, NDescriptions, NDescriptionsItem, NTag, NAlert, NScrollbar, NCode } from 'naive-ui'
import dayjs from 'dayjs'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  task: {
    type: Object,
    default: null,
  },
})

const emit = defineEmits(['update:visible'])

// 格式化时间戳为可读格式（精确到毫秒）
function formatTime(timestamp) {
  if (!timestamp) return '-'
  return dayjs(timestamp).format('YYYY-MM-DD HH:mm:ss.SSS')
}

// 格式化耗时为 x小时x分x秒x毫秒
function formatDuration(ms) {
  if (!ms || ms <= 0) return '-'

  const hours = Math.floor(ms / 3600000)
  const minutes = Math.floor((ms % 3600000) / 60000)
  const seconds = Math.floor((ms % 60000) / 1000)
  const milliseconds = ms % 1000

  const parts = []
  if (hours > 0) parts.push(`${hours}小时`)
  if (minutes > 0) parts.push(`${minutes}分`)
  if (seconds > 0) parts.push(`${seconds}秒`)
  if (milliseconds > 0 || parts.length === 0) parts.push(`${milliseconds}毫秒`)

  return parts.join('')
}

// 获取状态类型
function getStatusType(status) {
  const types = {
    success: 'success',
    failed: 'error',
    running: 'warning',
  }
  return types[status] || 'default'
}

// 获取状态文本
function getStatusText(status) {
  const texts = {
    success: '成功',
    failed: '失败',
    running: '进行中',
  }
  return texts[status] || '未知'
}
</script>
