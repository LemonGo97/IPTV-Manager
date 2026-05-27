<template>
  <div class="steps-area">
    <n-steps :current="currentStep" :status="stepStatus">
      <n-step title="频道过滤" description="根据规则过滤无效和测试频道"/>
      <n-step title="名称规范化" description="统一频道名称格式和标识"/>
      <n-step title="延迟检测" description="检测各源的延迟情况"/>
      <n-step title="频道分组" description="按类别整理归类频道"/>
      <n-step title="信号评分" description="评估源信号质量并打分"/>
    </n-steps>
  </div>
</template>

<script setup>
import { NSteps, NStep } from 'naive-ui'

defineProps({
  currentStep: {
    type: Number,
    default: 1,
  },
  stepStatus: {
    type: String,
    default: 'process',
  },
})
</script>

<style scoped>
.steps-area {
  margin-bottom: 16px;
  padding: 16px;
  background-color: var(--n-color-modal);
  border-radius: 4px;
}
</style>
