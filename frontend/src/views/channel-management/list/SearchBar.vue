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
      <n-spin v-if="props.checkedRows.length > 0" :show="stepsAreaLoadingStatus">
        <n-dropdown trigger="click" :options="stepPressOptions" @select="handleStepPressSelectBatch">
          <n-button type="success" :disabled="props.checkedRows.length <= 0">
            <i class="i-material-symbols:keyboard-arrow-down-rounded text-18"/>
            批量处理 {{ props.checkedRows.length ? `（${props.checkedRows.length} 个频道）` : ''}}
          </n-button>
        </n-dropdown>
      </n-spin>
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
import { reactive, ref } from 'vue'
import api from './api'

const props = defineProps({
  checkedRows: {
    type: Array,
    default: [],
  },
})

const emit = defineEmits(['search', 'refresh'])

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

const stepsAreaLoadingStatus = ref(false)

function handleSearch() {
  emit('search', {
    name: queryItems.name || undefined,
  })
}

function handleRefresh() {
  emit('refresh')
}

async function handleStartProcessing() {
  stepsAreaLoadingStatus.value = true
  try {
    await api.dataClean(undefined, [])
    $message.info('频道列表开始数据清洗并生成新的频道列表中...')
  } catch (error) {
    $message.error('开始处理失败')
    console.error(error)
  } finally {
    stepsAreaLoadingStatus.value = false
  }
}

async function handleStepPressSelect(key) {
  stepsAreaLoadingStatus.value = true
  try {
    await api.dataClean(key, [])
    $message.info('频道列表开始数据清洗并生成新的频道列表中...')
  } catch (error) {
    $message.error('分步处理失败')
    console.error(error)
  } finally {
    stepsAreaLoadingStatus.value = false
  }
}

async function handleStepPressSelectBatch(key) {
  stepsAreaLoadingStatus.value = true
  try {
    await api.dataClean(key, props.checkedRows)
    $message.info('频道列表开始数据清洗并生成新的频道列表中...')
  } catch (error) {
    $message.error('分步处理失败')
    console.error(error)
  } finally {
    stepsAreaLoadingStatus.value = false
  }
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
