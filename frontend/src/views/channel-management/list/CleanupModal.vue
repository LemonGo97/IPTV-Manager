<template>
  <n-modal
    :show="props.visible"
    preset="card"
    title='数据清洗'
    :style="{ width: '500px' }"
    @update:show="(val) => emit('update:visible', val)"
  >

    正文内容



    <template #action>
      <n-space class="mt-4 flex justify-end" :size="16">
        <n-button @click="handleClose">
          取消
        </n-button>
        <n-button type="primary" @click="handleStartCleanup">
          开始执行
        </n-button>
      </n-space>
    </template>
  </n-modal>
</template>

<script setup>
import { NModal, NSpace, NButton } from 'naive-ui'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['update:visible'])

// 打开预览弹窗
function open() {
  emit('update:visible', true)
}

// 关闭弹窗
function handleClose() {
  emit('update:visible', false)
}

// 打开播放器
function handleStartCleanup() {
  emit('update:visible', false)
}

// 暴露方法给父组件
defineExpose({
  open,
})
</script>
