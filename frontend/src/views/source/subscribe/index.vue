<template>
  <CommonPage>
    <template #action>
      <NButton type="primary" @click="handleAdd()">
        <i class="i-material-symbols:add mr-4 text-18" />
        添加订阅源
      </NButton>
    </template>

    <MeCrud
      ref="$table"
      v-model:query-items="queryItems"
      :scroll-x="1400"
      :columns="columns"
      :get-data="api.getAll"
    >
      <MeQueryItem label="名称" :label-width="50">
        <n-input
          v-model:value="queryItems.name"
          type="text"
          placeholder="请输入名称"
          clearable
        />
      </MeQueryItem>

      <MeQueryItem label="类型" :label-width="50">
        <n-select
          v-model:value="queryItems.type"
          clearable
          :options="[
            { label: '在线订阅', value: 'online' },
            { label: '本地文件', value: 'local' },
          ]"
        />
      </MeQueryItem>

      <MeQueryItem label="状态" :label-width="50">
        <n-select
          v-model:value="queryItems.enabled"
          clearable
          :options="[
            { label: '启用', value: true },
            { label: '停用', value: false },
          ]"
        />
      </MeQueryItem>
    </MeCrud>

    <MeModal ref="modalRef" width="600px" :title="modalTitle">
      <n-form
        ref="modalFormRef"
        label-placement="left"
        label-align="left"
        :label-width="100"
        :model="modalForm"
        :disabled="modalAction === 'view'"
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
          <n-radio-group v-model:value="modalForm.type" :disabled="modalAction === 'edit'">
            <n-radio value="online">
              在线订阅
            </n-radio>
            <n-radio value="local">
              本地文件
            </n-radio>
          </n-radio-group>
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
          v-if="modalForm.type === 'local'"
          label="上传文件"
          path="file"
          :rule="{
            required: modalAction === 'add',
            message: '请选择 M3U8 文件',
            trigger: ['change', 'blur'],
          }"
        >
          <n-upload
            :file-list="fileList"
            :max="1"
            accept=".m3u,.m3u8"
            :custom-request="handleFileUpload"
            @update:file-list="handleFileListChange"
          >
            <NButton>选择文件</NButton>
          </n-upload>
        </n-form-item>

        <n-form-item
          v-if="modalForm.type === 'online'"
          label="自动刷新"
          path="autoRefresh"
        >
          <NSwitch v-model:value="modalForm.autoRefresh">
            <template #checked>
              启用
            </template>
            <template #unchecked>
              停用
            </template>
          </NSwitch>
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
          <NInputNumber
            v-model:value="modalForm.refreshRate"
            :min="60"
            :max="86400"
            placeholder="请输入刷新间隔（秒）"
            class="w-full"
          >
            <template #suffix>
              秒
            </template>
          </NInputNumber>
        </n-form-item>

        <n-form-item
          label="状态"
          path="enabled"
        >
          <NSwitch v-model:value="modalForm.enabled">
            <template #checked>
              启用
            </template>
            <template #unchecked>
              停用
            </template>
          </NSwitch>
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
    </MeModal>
  </CommonPage>
</template>

<script setup>
import {
  NButton,
  NInput,
  NSelect,
  NSwitch,
  NRadio,
  NRadioGroup,
  NInputNumber,
  NUpload,
  NTag,
  NForm,
  NFormItem,
  NAlert,
} from 'naive-ui'
import { MeCrud, MeModal, MeQueryItem } from '@/components'
import { useCrud } from '@/composables'
import api from './api'

const $table = ref(null)
const queryItems = reactive({
  name: '',
  type: null,
  enabled: null,
})

const fileList = ref([])

const modalTitle = computed(() => {
  const titles = {
    add: '添加订阅源',
    edit: '编辑订阅源',
    view: '查看订阅源',
  }
  return titles[modalAction.value] || '订阅源'
})

const columns = [
  { title: 'ID', key: 'id', width: 80, render: row => row.id || '-' },
  { title: '名称', key: 'name', width: 150, ellipsis: { tooltip: true } },
  {
    title: '类型',
    key: 'type',
    width: 100,
    render: row => (row.type === 'online' ? '在线订阅' : '本地文件'),
  },
  {
    title: '订阅地址/文件名',
    key: 'url',
    width: 300,
    ellipsis: { tooltip: true },
    render: row => row.url || row.fileName || '-',
  },
  {
    title: '自动刷新',
    key: 'autoRefresh',
    width: 100,
    render: row => (row.autoRefresh ? `${row.refreshRate}秒` : '-'),
  },
  {
    title: '状态',
    key: 'enabled',
    width: 80,
    render: row =>
      h(
        NTag,
        { type: row.enabled ? 'success' : 'default', size: 'small' },
        { default: () => (row.enabled ? '启用' : '停用') }
      ),
  },
  { title: '备注', key: 'description', width: 200, ellipsis: { tooltip: true } },
  {
    title: '创建时间',
    key: 'createdAt',
    width: 180,
    render: row => row.createdAt || '-',
  },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    fixed: 'right',
    render: row =>
      h('div', { class: 'flex items-center gap-8' }, [
        h(
          NButton,
          {
            size: 'small',
            onClick: () => handleOpen(row),
          },
          { default: () => '编辑' }
        ),
        h(
          NButton,
          {
            size: 'small',
            type: 'primary',
            onClick: () => handleRefresh(row),
          },
          { default: () => '刷新' }
        ),
        h(
          NButton,
          {
            size: 'small',
            type: 'error',
            onClick: () => handleDelete(row.id),
          },
          { default: () => '删除' }
        ),
      ]),
  },
]

const {
  modalRef,
  modalFormRef,
  modalForm,
  modalAction,
  handleAdd,
  handleDelete,
  handleOpen: _handleOpen,
  handleSave,
} = useCrud({
  name: '订阅源',
  initForm: {
    type: 'online',
    autoRefresh: false,
    refreshRate: 3600,
    enabled: true,
  },
  doCreate: api.create,
  doDelete: api.delete,
  doUpdate: (data) => api.update(data.id, data),
  refresh: () => $table.value?.handleSearch(),
})

// Override handleOpen to set autoRefresh from refreshRate
function handleOpen(row) {
  const rowData = row ?? {}
  _handleOpen({
    action: 'edit',
    row: {
      ...rowData,
      autoRefresh: !!rowData.refreshRate,
    },
  })
}

const handleFileUpload = async ({ file }) => {
  try {
    await api.upload(file.file, modalForm.value.name, modalForm.value.description)
    window.$message?.success('文件上传成功')
    modalRef.value.hide()
    handleSave()
  } catch (error) {
    window.$message?.error('文件上传失败')
  }
}

const handleFileListChange = (list) => {
  fileList.value = list
}

const handleRefresh = async (row) => {
  try {
    const result = await api.refresh(row.id)
    // result.data.taskId 包含任务 ID
    window.$message?.success(`刷新任务已提交，请在任务历史中查看进度`)
    // 不立即刷新表格，因为任务在后台执行中
  } catch (error) {
    window.$message?.error('刷新任务提交失败')
  }
}

onMounted(() => {
  $table.value?.handleSearch()
})

defineOptions({
  name: 'SubscribeConfig',
})
</script>
