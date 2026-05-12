<template>
  <CommonPage>
    <template #action>
      <NButton type="primary" @click="handleAdd()">
        <i class="i-material-symbols:add mr-4 text-18" />
        添加频道组
      </NButton>
    </template>

    <MeCrud
      ref="$table"
      v-model:query-items="queryItems"
      :scroll-x="1200"
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
            class="w-full"
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
    </MeModal>
  </CommonPage>
</template>

<script setup>
import { NButton, NInput, NInputNumber } from 'naive-ui'
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
  { title: '描述', key: 'description', ellipsis: { tooltip: true } },
  { title: '排序', key: 'sortOrder', width: 100 },
  {
    title: '创建时间',
    key: 'createdAt',
    width: 180,
    render: row => (row.createdAt ? new Date(row.createdAt).toLocaleString('zh-CN') : '-'),
  },
  {
    title: '操作',
    key: 'actions',
    width: 150,
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
  name: '频道组',
  initForm: {
    name: '',
    sortOrder: 0,
    description: '',
  },
  doCreate: api.create,
  doUpdate: (data) => api.update(data.id, data),
  doDelete: api.delete,
  refresh: () => $table.value?.handleSearch(),
})

onMounted(() => {
  $table.value?.handleSearch()
})

defineOptions({
  name: 'ChannelGroups',
})
</script>
