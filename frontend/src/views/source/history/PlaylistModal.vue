<template>
  <n-modal
    :show="props.visible"
    preset="card"
    title="频道列表"
    :style="{ width: '900px' }"
    @update:show="(val) => emit('update:visible', val)"
  >
    <n-data-table
      :columns="columns"
      :data="playlistData"
      :pagination="{ pageSize: 20 }"
      :scroll-x="1200"
      size="small"
    />
  </n-modal>
</template>

<script setup>
import { NModal, NDataTable } from 'naive-ui'
import { ref, watch } from 'vue'
import api from './api'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  taskId: {
    type: Number,
    default: null,
  },
})

const emit = defineEmits(['update:visible'])

const playlistData = ref([])

const columns = [
  { title: '序号', key: 'index', width: 80 },
  { title: 'Logo', key: 'tvGuideLogo', width: 80, ellipsis: { tooltip: true } },
  { title: '频道名称', key: 'name', width: 200, ellipsis: { tooltip: true } },
  { title: '分组', key: 'groupTitle', width: 120 },
  { title: '指南ID', key: 'tvGuideId', width: 120 },
  { title: '指南名称', key: 'tvGuideName', width: 120 },
  { title: '国家', key: 'tvGuideCountry', width: 120 },
  { title: '语言', key: 'tvGuideLanguage', width: 120 },
  { title: 'URL', key: 'url', width: 400, ellipsis: { tooltip: true } },
  { title: '更新时间', key: 'updatedAt', width: 400, ellipsis: { tooltip: true } },
]

// 监听 taskId 变化，加载频道列表
watch(() => props.taskId, async (newTaskId) => {
  if (newTaskId && props.visible) {
    await loadPlaylist(newTaskId)
  }
})

// 监听 visible 变化
watch(() => props.visible, async (newVisible) => {
  if (newVisible && props.taskId) {
    await loadPlaylist(props.taskId)
  }
})

async function loadPlaylist(taskId) {
  try {
    const res = await api.getChannels(taskId)
    playlistData.value = res.data.map((ch, index) => ({
      index: index + 1,
      tvGuideLogo: ch.tvGuideLogo || '-',
      name: ch.name || '-',
      groupTitle: ch.groupTitle || '-',
      tvGuideId: ch.tvGuideId || '-',
      tvGuideName: ch.tvGuideName || '-',
      tvGuideCountry: ch.tvGuideCountry || '-',
      tvGuideLanguage: ch.tvGuideLanguage || '-',
      url: ch.url || '-',
      updatedAt: ch.updatedAt || '-',
    }))
  } catch (error) {
    console.error(error)
    $message.error('获取频道列表失败')
  }
}
</script>
