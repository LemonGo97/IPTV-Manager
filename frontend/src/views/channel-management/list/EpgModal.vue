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

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  channelName: {
    type: String,
    default: '',
  },
  timelineData: {
    type: Array,
    default: () => [],
  },
})

const emit = defineEmits(['update:visible'])

const epgContainerRef = ref(null)

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

// 滚动到当前节目
async function scrollToCurrent() {
  await nextTick()
  if (!epgContainerRef.value) return

  const currentIndex = props.timelineData.findIndex(item => item.isCurrent)
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
