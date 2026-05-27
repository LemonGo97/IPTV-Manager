<template>
  <n-modal
    :show="props.visible"
    preset="card"
    :title="`确认使用 ${playerName} 预览`"
    :style="{ width: '500px' }"
    @update:show="(val) => emit('update:visible', val)"
  >
    <div v-if="channel" class="preview-modal-content">
      <n-p class="text-16 mb-4">确定使用 {{ playerName }} 预览「{{ channel.name }}」吗？</n-p>
      <n-p class="text-14 opacity-70 url-text">{{ channel.url }}</n-p>
    </div>
    <template #action>
      <n-space class="mt-4 flex justify-end" :size="16">
        <n-button @click="emit('update:visible', false)">
          关闭
        </n-button>
        <n-button @click="emit('copyUrl')">
          复制链接
        </n-button>
        <n-button type="primary" @click="emit('openPlayer')">
          打开 {{ playerName }}
        </n-button>
      </n-space>
    </template>
  </n-modal>
</template>

<script setup>
import { NModal, NP, NSpace, NButton } from 'naive-ui'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  channel: {
    type: Object,
    default: null,
  },
  playerName: {
    type: String,
    default: '',
  },
})

const emit = defineEmits(['update:visible', 'copyUrl', 'openPlayer'])
</script>

<style scoped>
.preview-modal-content {
  padding: 8px 0;
}

.url-text {
  word-break: break-all;
}
</style>
