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
          path="startTime"
          :rule="{ required: true, message: '请选择开始时间', trigger: ['change', 'blur'] }"
        >
          <n-date-picker
            v-model:value="modalForm.startTime"
            type="datetime"
            placeholder="请选择开始时间"
            style="width: 100%"
            clearable
          />
        </n-form-item>

        <n-form-item
          label="结束时间"
          path="endTime"
          :rule="{ required: true, message: '请选择结束时间', trigger: ['change', 'blur'] }"
        >
          <n-date-picker
            v-model:value="modalForm.endTime"
            type="datetime"
            placeholder="请选择结束时间"
            style="width: 100%"
            clearable
            :is-date-disabled="(timestamp) => {
              if (!modalForm.startTime) return false
              return timestamp < modalForm.startTime
            }"
          />
        </n-form-item>
      </template>

      <!-- 高级设置 -->
      <n-collapse>
        <n-collapse-item title="高级设置" name="advanced">
          <n-form-item label="过滤无效频道" path="filterInvalidChannels">
            <n-switch v-model:value="modalForm.filterInvalidChannels" />
          </n-form-item>

          <n-form-item label="过滤HTTP测试延迟较高频道" path="filterHttpHighDelay">
            <n-input-number
              v-model:value="modalForm.filterHttpHighDelay"
              placeholder="允许的最大延迟时间(ms)，-1表示不过滤"
              :min="-1"
              style="width: 100%"
            />
          </n-form-item>

          <n-form-item label="过滤FFmpeg测试延迟较高频道" path="filterFfmpegHighDelay">
            <n-input-number
              v-model:value="modalForm.filterFfmpegHighDelay"
              placeholder="允许的最大延迟时间(ms)，-1表示不过滤"
              :min="-1"
              style="width: 100%"
            />
          </n-form-item>

          <n-form-item label="过滤无视频流频道" path="filterNoVideoStream">
            <n-switch v-model:value="modalForm.filterNoVideoStream" />
          </n-form-item>

          <n-form-item label="过滤无音频流频道" path="filterNoAudioStream">
            <n-switch v-model:value="modalForm.filterNoAudioStream" />
          </n-form-item>

          <n-form-item label="过滤低分辨率频道" path="filterLowResolution">
            <n-select
              v-model:value="modalForm.filterLowResolution"
              :options="resolutionOptions"
              placeholder="请选择最低分辨率"
            />
          </n-form-item>

          <n-form-item label="合并相同频道为多线路" path="mergeSameChannels">
            <n-switch v-model:value="modalForm.mergeSameChannels" />
          </n-form-item>

        </n-collapse-item>
      </n-collapse>
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
import { NModal, NForm, NFormItem, NInput, NSelect, NDatePicker, NButton, NSwitch, NInputNumber, NTransfer, NCollapse, NCollapseItem } from 'naive-ui'
import { ref, reactive, onMounted } from 'vue'
import distributionUserApi from '../users/api'
import api from './api'
import channelGroupApi from '@/views/channel-management/groups/api'

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
  startTime: null,
  endTime: null,
  // 高级设置
  filterInvalidChannels: true,
  filterHttpHighDelay: -1,
  filterFfmpegHighDelay: -1,
  filterNoVideoStream: true,
  filterNoAudioStream: true,
  filterLowResolution: '1080p',
  mergeSameChannels: true,
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

// 分辨率选项
const resolutionOptions = [
  { label: '480p', value: '480p' },
  { label: '720p', value: '720p' },
  { label: '1080p', value: '1080p' },
  { label: '1440p (2K)', value: '1440p' },
  { label: '2160p (4K)', value: '2160p' },
]

const userOptions = ref([])
const channelGroupOptions = ref([])

// 有效期类型变化时处理
function handleValidityTypeChange(value) {
  if (value !== 'CUSTOM') {
    modalForm.startTime = null
    modalForm.endTime = null
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

// 加载频道组选项
async function loadChannelGroupOptions() {
  try {
    const res = await channelGroupApi.getAll()
    const groups = res.data || []
    // 转换为 transfer 组件需要的格式
    channelGroupOptions.value = groups.map(group => ({
      label: group.name,
      value: group.id,
    }))
  } catch (error) {
    console.error('Failed to load channel groups:', error)
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
      startTime: modalForm.startTime ? new Date(modalForm.startTime).toISOString() : null,
      endTime: modalForm.endTime ? new Date(modalForm.endTime).toISOString() : null,
      // 高级设置
      filterInvalidChannels: modalForm.filterInvalidChannels,
      filterHttpHighDelay: modalForm.filterHttpHighDelay,
      filterFfmpegHighDelay: modalForm.filterFfmpegHighDelay,
      filterNoVideoStream: modalForm.filterNoVideoStream,
      filterNoAudioStream: modalForm.filterNoAudioStream,
      filterLowResolution: modalForm.filterLowResolution,
      mergeSameChannels: modalForm.mergeSameChannels,
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
    startTime: null,
    endTime: null,
    // 高级设置
    filterInvalidChannels: true,
    filterHttpHighDelay: -1,
    filterFfmpegHighDelay: -1,
    filterNoVideoStream: true,
    filterNoAudioStream: true,
    filterLowResolution: '1080p',
    mergeSameChannels: true,
  })
  emit('update:visible', true)
}

// 打开编辑弹窗
function openEdit(row) {
  internalAction.value = 'edit'
  const dateType = row.dateType || 'YEAR'

  let startTime = null
  let endTime = null

  if (row.dateType === 'CUSTOM' && row.startTime && row.endTime) {
    startTime = new Date(row.startTime).getTime()
    endTime = new Date(row.endTime).getTime()
  }

  Object.assign(modalForm, {
    ...row,
    dateType,
    startTime,
    endTime,
    // 高级设置 - 使用后端返回的值或默认值
    filterInvalidChannels: row.filterInvalidChannels ?? true,
    filterHttpHighDelay: row.filterHttpHighDelay ?? -1,
    filterFfmpegHighDelay: row.filterFfmpegHighDelay ?? -1,
    filterNoVideoStream: row.filterNoVideoStream ?? true,
    filterNoAudioStream: row.filterNoAudioStream ?? true,
    filterLowResolution: row.filterLowResolution ?? '1080p',
    mergeSameChannels: row.mergeSameChannels ?? true,
  })
  emit('update:visible', true)
}

onMounted(() => {
  loadUserOptions()
  loadChannelGroupOptions()
})

// 暴露方法给父组件
defineExpose({
  openAdd,
  openEdit,
})
</script>
