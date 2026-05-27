<template>
  <div class="search-bar">
    <n-space>
      <n-input
        v-model:value="queryItems.name"
        type="text"
        placeholder="请输入名称"
        clearable
        style="width: 200px"
        @keyup.enter="handleSearch"
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
import { NSpace, NInput, NButton } from 'naive-ui'
import { reactive } from 'vue'

const emit = defineEmits(['search', 'refresh'])

// 搜索条件
const queryItems = reactive({
  name: '',
})

function handleSearch() {
  emit('search', {
    name: queryItems.name || undefined,
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
