<template>
  <div class="search-bar">
    <n-space>
      <n-input
        v-model:value="queryItems.name"
        type="text"
        placeholder="请输入名称"
        clearable
        style="width: 150px"
        @keyup.enter="handleSearch"
      />
      <n-select
        v-model:value="queryItems.type"
        clearable
        placeholder="类型"
        style="width: 120px"
        :options="typeOptions"
      />
      <n-select
        v-model:value="queryItems.enabled"
        clearable
        placeholder="状态"
        style="width: 100px"
        :options="enabledOptions"
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
import { NSpace, NInput, NSelect, NButton } from 'naive-ui'
import { reactive } from 'vue'

const emit = defineEmits(['search', 'refresh'])

// 搜索条件
const queryItems = reactive({
  name: '',
  type: null,
  enabled: null,
})

// 类型选项
const typeOptions = [
  { label: '在线订阅', value: 'online' },
  { label: '本地文件', value: 'file' },
]

// 状态选项
const enabledOptions = [
  { label: '启用', value: true },
  { label: '停用', value: false },
]

function handleSearch() {
  emit('search', {
    name: queryItems.name || undefined,
    type: queryItems.type || undefined,
    enabled: queryItems.enabled || undefined,
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
