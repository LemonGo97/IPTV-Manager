<template>
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
        <n-statistic label="状态" :value="statusText">
          <template #prefix>
            <i :class="statusIcon" class="status-icon mr-5 mt-5"/>
          </template>
        </n-statistic>
      </n-gi>
    </n-grid>
  </div>
</template>

<script setup>
import { NStatistic, NGrid, NGi } from 'naive-ui'
import { computed } from 'vue'

const props = defineProps({
  statistics: {
    type: Object,
    required: true,
  },
})

// 状态图标
const statusIcon = computed(() => {
  if (props.statistics.status === 'RUNNING') {
    return 'i-mdi:human-scooter'
  }
  return 'i-mdi:walk'
})

// 状态文本
const statusText = computed(() => {
  if (props.statistics.status === 'RUNNING') {
    return '清洗中'
  }
  return '未运行'
})
</script>

<style scoped>
.statistics-area {
  margin-bottom: 16px;
  padding: 16px;
  background-color: var(--n-color-modal);
  border-radius: 4px;
}
</style>
