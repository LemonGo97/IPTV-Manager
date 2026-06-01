<template>
  <n-modal
    :show="props.visible"
    preset="card"
    :title="`EPG节目单 - ${channelName}`"
    :style="{ width: '600px', maxHeight: '70vh' }"
    @update:show="(val) => emit('update:visible', val)"
  >
    <div class="epg-container" ref="epgContainerRef">
      <n-timeline>
        <n-timeline-item
          v-for="item in timelineData"
          :key="item.index"
          :type="getItemType(item)"
          :title="item.type === 'date' ? item.title : null"
          :time="item.time"
          :content="item.type === 'program' ? item.title : null"
          :class="{ 'current-program-item': item.isCurrent }"
        />
      </n-timeline>
    </div>
  </n-modal>
</template>

<script setup>
import { NModal, NTimeline, NTimelineItem } from 'naive-ui'
import { nextTick, ref, watch } from 'vue'
import api from './api'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['update:visible'])

// 内部状态
const channelName = ref('')
const timelineData = ref([])
const epgContainerRef = ref(null)

// 格式化时间显示
function formatTime(date) {
  return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

// 获取时间轴项目类型
function getItemType(item) {
  if (item.type === 'date') {
    return 'success'
  }
  if (item.isCurrent) {
    return 'success'
  }
  if (item.played) {
    return 'info'
  }
  return 'default'
}

// 打开 EPG 弹窗
async function open(row) {
  channelName.value = row.name

  try {
    const res = await api.getTimeline(row.id)
    const data = res.data

    const now = new Date()

    for (let i = 0; i < data.length; i++) {
      let item = data[i]
      item.index = i

      const startTime = new Date(item.startTime)
      const endTime = new Date(item.stopTime)

      if (item.type === 'program') {
        item.time = `${formatTime(startTime)} - ${formatTime(endTime)}`
        item.isCurrent = now >= startTime && now < endTime
        item.played = now > endTime
      }
    }

    timelineData.value = data
    emit('update:visible', true)
  } catch (error) {
    $message.error('获取EPG数据失败')
    console.error(error)
  }
}

// 滚动到当前节目
async function scrollToCurrent() {
  await nextTick()
  if (!epgContainerRef.value) return

  const currentIndex = timelineData.value.findIndex(item => item.isCurrent)
  if (currentIndex < 0) return

  const items = epgContainerRef.value.querySelectorAll('.n-timeline-item')
  const currentItem = items[currentIndex]
  if (currentItem) {
    currentItem.scrollIntoView({ behavior: 'smooth', block: 'center' })
  }
}

// 监听弹窗打开，滚动到当前节目
watch(() => props.visible, (val) => {
  if (val) {
    scrollToCurrent()
  }
})

// 暴露方法给父组件
defineExpose({
  open,
})
</script>

<style scoped>
.epg-container {
  max-height: 60vh;
  overflow-y: auto;
}

.current-program-item :deep(.n-timeline-item-timeline__line) {
  background-color: #18a058;
}
</style>
