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
        <n-button type="success" @click="handleStartProcessing">
          <i class="i-material-symbols:play-arrow mr-4 text-18"/>
          开始处理
        </n-button>
      </n-spin>
      <n-spin :show="stepsAreaLoadingStatus">
        <n-dropdown trigger="click" :options="stepPressOptions" @select="handleStepPressSelect">
          <n-button type="success">
            <i class="i-material-symbols:keyboard-arrow-down-rounded text-18"/>
            分步处理
          </n-button>
        </n-dropdown>
      </n-spin>
    </n-space>
  </div>
</template>

<script setup>
import { NInput, NButton, NSpace, NSpin, NDropdown } from 'naive-ui'
import { reactive } from 'vue'

const emit = defineEmits(['search', 'refresh', 'startProcessing', 'stepPressSelect'])

defineProps({
  stepsAreaLoadingStatus: {
    type: Boolean,
    default: false,
  },
})

const stepPressOptions = [
  { label: '频道过滤', key: 'FILTER' },
  { label: '名称规范化', key: 'NAME' },
  { label: '相同频道合并', key: 'MERGE' },
  { label: '延迟检测', key: 'DELAY' },
  { label: '频道分组', key: 'GROUP' },
]

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

function handleStartProcessing() {
  emit('startProcessing')
}

function handleStepPressSelect(key) {
  emit('stepPressSelect', key)
}
</script>

<style scoped>
.search-bar {
  margin-bottom: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
