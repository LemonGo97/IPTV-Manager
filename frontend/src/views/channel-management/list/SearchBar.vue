<template>
  <div class="search-bar">
    <n-space>
      <n-input
        v-model:value="queryItems.name"
        placeholder="搜索频道名称"
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
      <n-spin :show="stepsAreaLoadingStatus">
        <n-button type="success" @click="handleCleanup">
          <i class="i-mdi:database-clock-outline mr-4 text-18"/>
          数据清洗
        </n-button>
      </n-spin>
    </n-space>
  </div>
</template>

<script setup>
import { NInput, NButton, NSpace, NSpin, NDropdown } from 'naive-ui'
import { reactive, ref } from 'vue'
import api from './api'

const props = defineProps({
  checkedRows: {
    type: Array,
    default: [],
  },
})

const emit = defineEmits(['search', 'refresh', 'cleanup'])

const queryItems = reactive({
  name: '',
})

const stepsAreaLoadingStatus = ref(false)

function handleSearch() {
  emit('search', {
    name: queryItems.name || undefined,
  })
}

function handleCleanup() {
  emit('cleanup')
}

function handleRefresh() {
  emit('refresh')
}

// 暴露方法给父组件
defineExpose({
  getLoadingStatus: () => stepsAreaLoadingStatus.value,
})
</script>

<style scoped>
.search-bar {
  margin-bottom: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
