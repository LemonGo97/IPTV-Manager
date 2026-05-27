<template>
  <n-modal
    :show="props.visible"
    preset="card"
    title="分发用户"
    :style="{ width: '500px' }"
    :mask-closable="false"
    @update:show="(val) => emit('update:visible', val)"
    @after-leave="handleClose"
  >
    <n-form ref="modalFormRef" :model="modalForm" label-width="100">
      <n-form-item
        label="用户名"
        path="username"
        :rule="{
          required: true,
          message: '请输入用户名',
          trigger: ['input', 'blur'],
        }"
      >
        <n-input v-model:value="modalForm.username" placeholder="请输入用户名" />
      </n-form-item>

      <n-form-item
        label="访问密钥"
        path="accessKey"
      >
        <n-input
          v-model:value="modalForm.accessKey"
          placeholder="留空则自动生成"
          clearable
          show-password-on="click"
          type="password"
        />
        <template #feedback>
          留空则由系统自动生成32位随机密钥
        </template>
      </n-form-item>
    </n-form>

    <template #footer>
      <div class="flex justify-end gap-8">
        <n-button @click="handleCancel">取消</n-button>
        <n-button type="primary" @click="handleConfirm">确认</n-button>
      </div>
    </template>
  </n-modal>
</template>

<script setup>
import { NModal, NForm, NFormItem, NInput, NButton } from 'naive-ui'
import { ref, reactive } from 'vue'
import api from './api'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['update:visible', 'refresh'])

const modalFormRef = ref(null)
const internalAction = ref('add')

// 表单数据
const modalForm = reactive({
  id: null,
  username: '',
  accessKey: '',
})

// 取消
function handleCancel() {
  emit('update:visible', false)
}

// 确认
async function handleConfirm() {
  try {
    await modalFormRef.value?.validate()

    const payload = {
      username: modalForm.username,
      accessKey: modalForm.accessKey || undefined,
    }

    if (internalAction.value === 'add') {
      await api.create(payload)
      $message.success('创建成功')
    } else {
      await api.update(modalForm.id, payload)
      $message.success('更新成功')
    }

    emit('update:visible', false)
    emit('refresh')
  } catch (error) {
    if (error.errors) {
      // 表单验证失败
      return
    }
    $message.error(internalAction.value === 'add' ? '创建失败' : '更新失败')
    console.error(error)
  }
}

// 关闭后重置表单验证状态
function handleClose() {
  modalFormRef.value?.restoreValidation()
}

// 打开新增弹窗
function openAdd() {
  internalAction.value = 'add'
  Object.assign(modalForm, {
    id: null,
    username: '',
    accessKey: '',
  })
  emit('update:visible', true)
}

// 打开编辑弹窗
function openEdit(row) {
  internalAction.value = 'edit'
  Object.assign(modalForm, {
    ...row,
  })
  emit('update:visible', true)
}

// 暴露方法给父组件
defineExpose({
  openAdd,
  openEdit,
})
</script>
