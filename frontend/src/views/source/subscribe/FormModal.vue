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
        <n-input v-model:value="modalForm.name" placeholder="请输入订阅源名称" />
      </n-form-item>

      <n-form-item
        label="类型"
        path="type"
        :rule="{
          required: true,
          message: '请选择类型',
          trigger: ['change', 'blur'],
        }"
      >
        <n-radio-group v-model:value="modalForm.type" :disabled="internalAction === 'edit'">
          <n-radio value="online">
            在线订阅
          </n-radio>
          <n-radio value="file">
            本地文件
          </n-radio>
        </n-radio-group>
      </n-form-item>

      <n-form-item
        label="内容类型"
        path="contentType"
        :rule="{
          required: true,
          message: '请选择内容类型',
          trigger: ['change', 'blur'],
        }"
      >
        <n-select
          v-model:value="modalForm.contentType"
          clearable
          :options="[
            { label: 'M3U8', value: 'M3U8' },
            { label: 'TXT', value: 'TXT' },
          ]"
        />
      </n-form-item>

      <n-form-item
        v-if="modalForm.type === 'online'"
        label="订阅地址"
        path="url"
        :rule="{
          required: true,
          message: '请输入订阅地址',
          trigger: ['input', 'blur'],
        }"
      >
        <n-input
          v-model:value="modalForm.url"
          placeholder="请输入 M3U8 订阅地址"
          clearable
        />
      </n-form-item>

      <n-form-item
        v-if="modalForm.type === 'file'"
        label="上传文件"
        path="file"
      >
        <n-upload
          accept=".m3u,.m3u8,.txt,.text"
          :on-change="handleFileUploadChange"
          :on-remove="handleFileUploadRemove"
          :default-upload="false"
          :max="1"
        >
          <n-button>
            <i class="i-me:arcticons:iptv-pro mr-5"></i>
            选择文件
          </n-button>
        </n-upload>
      </n-form-item>

      <n-form-item
        v-if="modalForm.type === 'online'"
        label="自动刷新"
        path="autoRefresh"
      >
        <n-switch v-model:value="modalForm.autoRefresh">
          <template #checked>
            启用
          </template>
          <template #unchecked>
            停用
          </template>
        </n-switch>
      </n-form-item>

      <n-form-item
        v-if="modalForm.type === 'online' && modalForm.autoRefresh"
        label="刷新间隔"
        path="refreshRate"
        :rule="{
          required: true,
          type: 'number',
          message: '请输入刷新间隔（秒）',
          trigger: ['input', 'blur'],
        }"
      >
        <n-input-number
          v-model:value="modalForm.refreshRate"
          :min="60"
          :max="86400"
          placeholder="请输入刷新间隔（秒）"
          style="width: 100%"
        >
          <template #suffix>
            秒
          </template>
        </n-input-number>
      </n-form-item>

      <n-form-item
        label="状态"
        path="enabled"
      >
        <n-switch v-model:value="modalForm.enabled">
          <template #checked>
            启用
          </template>
          <template #unchecked>
            停用
          </template>
        </n-switch>
      </n-form-item>

      <n-form-item
        label="备注"
        path="description"
      >
        <n-input
          v-model:value="modalForm.description"
          type="textarea"
          placeholder="请输入备注信息"
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
import { NModal, NForm, NFormItem, NInput, NSelect, NRadio, NRadioGroup, NSwitch, NInputNumber, NUpload, NButton } from 'naive-ui'
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
  type: 'online',
  contentType: 'M3U8',
  url: '',
  file: null,
  autoRefresh: false,
  refreshRate: 3600,
  enabled: true,
  description: '',
})

// 弹窗标题
const modalTitle = computed(() => {
  const titles = {
    add: '添加订阅源',
    edit: '编辑订阅源',
  }
  return titles[internalAction.value] || '订阅源'
})

// 文件上传变更
function handleFileUploadChange(options) {
  if (options.file) {
    modalForm.file = options.file
  }
}

// 文件移除
function handleFileUploadRemove() {
  modalForm.file = null
}

// 取消
function handleCancel() {
  emit('update:visible', false)
}

// 确认
async function handleConfirm() {
  try {
    await modalFormRef.value?.validate()

    // 构建 FormData
    let formData = new FormData()
    for (let key of Object.keys(modalForm)) {
      if (key === 'file' && modalForm.file) {
        formData.append('file', modalForm.file.file)
      } else if (modalForm[key] !== null && modalForm[key] !== undefined) {
        formData.append(key, modalForm[key])
      }
    }

    if (internalAction.value === 'add') {
      await api.create(formData)
      $message.success('创建成功')
    } else {
      await api.update(modalForm.id, formData)
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
    type: 'online',
    contentType: 'M3U8',
    url: '',
    file: null,
    autoRefresh: false,
    refreshRate: 3600,
    enabled: true,
    description: '',
  })
  emit('update:visible', true)
}

// 打开编辑弹窗
function openEdit(row) {
  internalAction.value = 'edit'
  Object.assign(modalForm, {
    ...row,
    autoRefresh: !!row.refreshRate,
  })
  emit('update:visible', true)
}

// 暴露方法给父组件
defineExpose({
  openAdd,
  openEdit,
})
</script>
