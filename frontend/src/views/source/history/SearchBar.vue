<template>
  <div class="search-bar">
    <n-space>
      <n-input
        v-model:value="queryItems.providerName"
        type="text"
        placeholder="请输入订阅源名称"
        clearable
        style="width: 150px"
        @keyup.enter="handleSearch"
      />
      <n-select
        v-model:value="queryItems.triggerType"
        clearable
        placeholder="触发方式"
        style="width: 120px"
        :options="triggerTypeOptions"
      />
      <n-select
        v-model:value="queryItems.status"
        clearable
        placeholder="状态"
        style="width: 100px"
        :options="statusOptions"
      />
      <n-date-picker
        v-model:value="queryItems.dateRange"
        type="datetimerange"
        clearable
        style="width: 350px"
      />
      <n-button type="primary" @click="handleSearch">
        <i class="i-material-symbols:search mr-4 text-18"/>
        搜索
      </n-button>
      <n-button type="primary" @click="handleRefresh">
        <i class="i-material-symbols:refresh mr-4 text-18"/>
        刷新
      </n-button>
    </n-space>
  </div>
</template>

<script setup>
import { NSpace, NInput, NSelect, NDatePicker, NButton } from 'naive-ui'
import { reactive } from 'vue'

const emit = defineEmits(['search', 'refresh'])

// 搜索条件
const queryItems = reactive({
  providerName: '',
  triggerType: null,
  status: null,
  dateRange: null,
})

// 触发方式选项
const triggerTypeOptions = [
  { label: '手动刷新', value: 'manual' },
  { label: '定时任务', value: 'scheduled' },
]

// 状态选项
const statusOptions = [
  { label: '成功', value: 'success' },
  { label: '失败', value: 'failed' },
  { label: '进行中', value: 'running' },
]

function handleSearch() {
  emit('search', {
    providerName: queryItems.providerName || undefined,
    triggerType: queryItems.triggerType || undefined,
    status: queryItems.status || undefined,
    startTime: queryItems.dateRange?.[0] ? new Date(queryItems.dateRange[0]).toISOString() : undefined,
    endTime: queryItems.dateRange?.[1] ? new Date(queryItems.dateRange[1]).toISOString() : undefined,
  })
}

function handleRefresh() {
  emit('refresh')
}
</script>

<style scoped>
.search-bar {
  margin-bottom: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
