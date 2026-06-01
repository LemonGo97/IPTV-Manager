<template>
  <n-modal
    :show="props.visible"
    preset="card"
    :title="`确认使用 ${currentPlayerName} 预览`"
    :style="{ width: '500px' }"
    @update:show="(val) => emit('update:visible', val)"
  >
    <div v-if="channel" class="preview-modal-content">
      <n-p class="text-16 mb-4">确定使用 {{ currentPlayerName }} 预览「{{ channel.name }}」吗？</n-p>
      <n-p class="text-14 opacity-70 url-text">{{ channel.url }}</n-p>
    </div>
    <template #action>
      <n-space class="mt-4 flex justify-end" :size="16">
        <n-button @click="handleClose">
          关闭
        </n-button>
        <n-button @click="handleCopyUrl">
          复制链接
        </n-button>
        <n-button type="primary" @click="handleOpenPlayer">
          打开 {{ currentPlayerName }}
        </n-button>
      </n-space>
    </template>
  </n-modal>
</template>

<script setup>
import { NModal, NP, NSpace, NButton } from 'naive-ui'
import { ref, computed, watch } from 'vue'
import { useClipboard } from '@vueuse/core'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['update:visible'])

// 使用剪贴板
const { copy, copied } = useClipboard()

// 监听复制状态
watch(copied, (val) => {
  if (val) {
    $message.success('已复制到剪贴板')
  }
})

// 内部状态
const channel = ref(null)
const playerKey = ref('')

// 播放器名称映射
const playerNames = {
  iina: 'IINA',
  potplayer: 'PotPlayer',
  vlc: 'VLC',
}

// 当前选中的播放器名称
const currentPlayerName = computed(() => playerNames[playerKey.value] || '')

// 打开预览弹窗
function open(key, row) {
  playerKey.value = key
  channel.value = row
  emit('update:visible', true)
}

// 关闭弹窗
function handleClose() {
  emit('update:visible', false)
}

// 复制链接
function handleCopyUrl() {
  if (channel.value) {
    copy(channel.value.url)
  }
}

// 打开播放器
function handleOpenPlayer() {
  if (!channel.value) return

  const player = playerKey.value
  const row = channel.value

  // 播放器 URL scheme 映射
  const playerSchemes = {
    iina: (url) => `iina://weblink?url=${encodeURIComponent(url)}`,
    potplayer: (url) => `potplayer:${url}`,
    vlc: (url) => `vlc://${encodeURIComponent(url)}`,
  }

  const schemeBuilder = playerSchemes[player]
  if (!schemeBuilder) return

  const playerUrl = schemeBuilder(row.url)
  window.open(playerUrl, '_blank')
  $message.success(`正在使用 ${playerNames[player]} 打开：${row.name}`)
  emit('update:visible', false)
}

// 暴露方法给父组件
defineExpose({
  open,
})
</script>

<style scoped>
.preview-modal-content {
  padding: 8px 0;
}

.url-text {
  word-break: break-all;
}
</style>
