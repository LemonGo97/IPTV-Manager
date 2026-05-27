<template>
  <n-modal
    :show="props.visible"
    preset="card"
    title="分发订阅"
    :style="{ width: '600px' }"
    :mask-closable="false"
    @update:show="(val) => emit('update:visible', val)"
    @after-leave="handleClose"
  >
    <n-form ref="modalFormRef" :model="modalForm" label-width="100">
      <n-form-item
        label="分发名称"
        path="name"
        :rule="{ required: true, message: '请输入分发名称', trigger: ['input', 'blur'] }"
      >
        <n-input v-model:value="modalForm.name" placeholder="请输入分发名称" />
      </n-form-item>

      <n-form-item
        label="订阅用户"
        path="userId"
        :rule="{ type: 'number', required: true, message: '请选择订阅用户', trigger: ['change', 'blur'] }"
      >
        <n-select
          v-model:value="modalForm.userId"
          placeholder="请选择订阅用户"
          :options="userOptions"
          label-field="username"
          value-field="id"
          clearable
          filterable
        />
      </n-form-item>

      <n-form-item
        label="有效期"
        path="dateType"
        :rule="{ required: true, message: '请选择有效期', trigger: ['change', 'blur'] }"
      >
        <n-select
          v-model:value="modalForm.dateType"
          placeholder="请选择有效期"
          :options="validityOptions"
          @update:value="handleValidityTypeChange"
        />
      </n-form-item>

      <!-- 自定义日期范围 -->
      <template v-if="modalForm.dateType === 'CUSTOM'">
        <n-form-item
          label="开始时间"
          path="customStartTime"
          :rule="{ required: true, message: '请选择开始时间', trigger: ['change', 'blur'] }"
        >
          <n-date-picker
            v-model:value="modalForm.customStartTime"
            type="datetime"
            placeholder="请选择开始时间"
            style="width: 100%"
            clearable
          />
        </n-form-item>

        <n-form-item
          label="结束时间"
          path="customEndTime"
          :rule="{ required: true, message: '请选择结束时间', trigger: ['change', 'blur'] }"
        >
          <n-date-picker
            v-model:value="modalForm.customEndTime"
            type="datetime"
            placeholder="请选择结束时间"
            style="width: 100%"
            clearable
            :is-date-disabled="(timestamp) => {
              if (!modalForm.customStartTime) return false
              return timestamp < modalForm.customStartTime
            }"
          />
        </n-form-item>
      </template>
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
import { NModal, NForm, NFormItem, NInput, NSelect, NDatePicker, NButton } from 'naive-ui'
import { ref, reactive, onMounted } from 'vue'
import distributionUserApi from '../users/api'
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
  userId: null,
  dateType: 'YEAR',
  customStartTime: null,
  customEndTime: null,
})

// 有效期选项（枚举值）
const validityOptions = [
  { label: '1个月', value: 'MONTH' },
  { label: '3个月', value: 'QUARTER' },
  { label: '半年', value: 'HALF_YEAR' },
  { label: '一年', value: 'YEAR' },
  { label: '永久', value: 'FOREVER' },
  { label: '自定义', value: 'CUSTOM' },
]

const userOptions = ref([])

// 有效期类型变化时处理
function handleValidityTypeChange(value) {
  if (value !== 'CUSTOM') {
    modalForm.customStartTime = null
    modalForm.customEndTime = null
  }
}

// 加载用户选项
async function loadUserOptions() {
  try {
    const res = await distributionUserApi.getAll()
    userOptions.value = res.data || []
  } catch (error) {
    console.error('Failed to load users:', error)
  }
}

// 取消
function handleCancel() {
  emit('update:visible', false)
}

// 确认
async function handleConfirm() {
  try {
    await modalFormRef.value?.validate()

    const payload = {
      name: modalForm.name,
      userId: modalForm.userId,
      dateType: modalForm.dateType,
      customStartTime: modalForm.customStartTime ? new Date(modalForm.customStartTime).toISOString() : null,
      customEndTime: modalForm.customEndTime ? new Date(modalForm.customEndTime).toISOString() : null,
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
    name: '',
    userId: null,
    dateType: 'YEAR',
    customStartTime: null,
    customEndTime: null,
  })
  emit('update:visible', true)
}

// 打开编辑弹窗
function openEdit(row) {
  internalAction.value = 'edit'
  const dateType = row.dateType || 'YEAR'

  let customStartTime = null
  let customEndTime = null

  if (row.dateType === 'CUSTOM' && row.startTime && row.endTime) {
    customStartTime = new Date(row.startTime).getTime()
    customEndTime = new Date(row.endTime).getTime()
  }

  Object.assign(modalForm, {
    ...row,
    dateType,
    customStartTime,
    customEndTime,
  })
  emit('update:visible', true)
}

onMounted(() => {
  loadUserOptions()
})

// 暴露方法给父组件
defineExpose({
  openAdd,
  openEdit,
})
</script>
