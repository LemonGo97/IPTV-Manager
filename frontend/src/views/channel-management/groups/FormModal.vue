<template>
  <n-modal
    :show="props.visible"
    preset="card"
    :title="modalTitle"
    :style="{ width: '600px' }"
    :mask-closable="false"
    @update:show="(val) => emit('update:visible', val)"
    @after-leave="handleClose"
  >
    <n-form
      ref="modalFormRef"
      label-placement="left"
      label-align="left"
      :label-width="100"
      :model="modalForm"
    >
      <n-form-item
        label="名称"
        path="name"
        :rule="{
          required: true,
          message: '请输入名称',
          trigger: ['input', 'blur'],
        }"
      >
        <n-input v-model:value="modalForm.name" placeholder="请输入频道组名称" />
      </n-form-item>

      <n-form-item
        label="排序"
        path="sortOrder"
        :rule="{
          type: 'number',
          message: '请输入有效的排序值',
          trigger: ['input', 'blur'],
        }"
      >
        <n-input-number
          v-model:value="modalForm.sortOrder"
          placeholder="请输入排序值"
          :min="0"
          style="width: 100%"
        />
      </n-form-item>

      <n-form-item label="描述" path="description">
        <n-input
          v-model:value="modalForm.description"
          type="textarea"
          placeholder="请输入描述"
          :rows="3"
        />
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
import { NModal, NForm, NFormItem, NInput, NInputNumber, NButton } from 'naive-ui'
import { ref, reactive, computed } from 'vue'
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
  name: '',
  sortOrder: 0,
  description: '',
})

// 弹窗标题
const modalTitle = computed(() => {
  const titles = {
    add: '添加频道组',
    edit: '编辑频道组',
  }
  return titles[internalAction.value] || '频道组'
})

// 取消
function handleCancel() {
  emit('update:visible', false)
}

// 确认
async function handleConfirm() {
  try {
    await modalFormRef.value?.validate()

    if (internalAction.value === 'add') {
      await api.create(modalForm)
      $message.success('创建成功')
    } else {
      await api.update(modalForm.id, modalForm)
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
    name: '',
    sortOrder: 0,
    description: '',
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
