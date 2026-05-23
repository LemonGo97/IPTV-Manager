<template>
  <CommonPage>
    <template #action>
      <NButton type="primary" @click="handleAdd()">
        <i class="i-material-symbols:add mr-4 text-18" />
        添加EPG源
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
    </MeCrud>

    <MeModal ref="modalRef" width="600px" :title="modalTitle">
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
          <n-input v-model:value="modalForm.name" placeholder="请输入EPG源名称" />
        </n-form-item>

        <n-form-item
          label="URL"
          path="url"
          :rule="{
            required: true,
            message: '请输入URL',
            trigger: ['input', 'blur'],
          }"
        >
          <n-input v-model:value="modalForm.url" placeholder="请输入EPG数据URL" />
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
          <n-select
            v-model:value="modalForm.type"
            placeholder="请选择类型"
            :options="[
              { label: 'XMLTV', value: 'xml' },
            ]"
          />
        </n-form-item>

        <n-form-item label="启用" path="enabled">
          <n-switch v-model:value="modalForm.enabled" />
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
    </MeModal>
  </CommonPage>
</template>

<script setup>
import { NButton, NInput, NSwitch, NSelect, NTag } from 'naive-ui'
import { MeCrud, MeModal, MeQueryItem } from '@/components'
import { useCrud } from '@/composables'
import api from './api'
import { h } from 'vue'

const $table = ref(null)
const queryItems = reactive({
  name: '',
})

const columns = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '名称', key: 'name', width: 200, ellipsis: { tooltip: true } },
  { title: 'URL', key: 'url', ellipsis: { tooltip: true } },
  { title: '类型', key: 'type', width: 100 },
  {
    title: '状态',
    key: 'enabled',
    width: 100,
    render: row =>
      h(
        NTag,
        { type: row.enabled ? 'success' : 'default' },
        { default: () => (row.enabled ? '已启用' : '已禁用') }
      ),
  },
  { title: '描述', key: 'description', ellipsis: { tooltip: true } },
  {
    title: '创建时间',
    key: 'createdAt',
    width: 180,
    render: row => (row.createdAt ? new Date(row.createdAt).toLocaleString('zh-CN') : '-'),
  },
  {
    title: '操作',
    key: 'actions',
    width: 220,
    fixed: 'right',
    render: row =>
      h('div', { class: 'flex gap-8' }, [
        h(
          NButton,
          { size: 'small', onClick: () => handleEdit(row) },
          { default: () => '编辑' }
        ),
        h(
          NButton,
          { size: 'small', type: 'info', onClick: () => handleRefresh(row) },
          { default: () => '刷新' }
        ),
        h(
          NButton,
          { size: 'small', type: 'error', onClick: () => handleDelete(row.id) },
          { default: () => '删除' }
        ),
      ]),
  },
]

// 使用 useCrud 管理表单和弹窗
const {
  modalRef,
  modalFormRef,
  modalForm,
  modalAction,
  modalTitle,
  handleAdd,
  handleDelete,
  handleEdit,
  handleSave,
} = useCrud({
  name: 'EPG源',
  initForm: {
    name: '',
    url: '',
    type: 'xml',
    enabled: true,
    description: '',
  },
  doCreate: api.create,
  doUpdate: (data) => api.update(data.id, data),
  doDelete: api.delete,
  refresh: () => $table.value?.handleSearch(),
})

// 刷新EPG源
async function handleRefresh(row) {
  const $message = window.$message
  try {
    $message.loading('正在刷新EPG数据...')
    await api.refresh(row.id)
    $message.success('EPG数据刷新成功')
  } catch (error) {
    $message.error('EPG数据刷新失败: ' + (error.message || '未知错误'))
  }
}

onMounted(() => {
  $table.value?.handleSearch()
})

defineOptions({
  name: 'EpgSources',
})
</script>
