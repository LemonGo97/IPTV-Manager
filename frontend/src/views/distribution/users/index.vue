<template>
  <CommonPage>
    <template #action>
      <NButton type="primary" @click="handleAdd()">
        <i class="i-material-symbols:add mr-4 text-18" />
        新增用户
      </NButton>
    </template>

    <SearchBar
      @search="handleSearch"
      @refresh="fetchTableData"
    />

    <DataTable
      ref="tableRef"
      :search-params="queryItems"
      @edit="handleEdit"
    />

    <!-- 表单弹窗 -->
    <FormModal
      ref="modalRef"
      v-model:visible="modalVisible"
      @refresh="fetchTableData"
    />
  </CommonPage>
</template>

<script setup>
import { NButton } from 'naive-ui'
import { reactive, ref } from 'vue'
import SearchBar from './SearchBar.vue'
import DataTable from './DataTable.vue'
import FormModal from './FormModal.vue'
import {CommonPage} from "@/components/index.js"

// 弹窗显示状态和引用
const modalVisible = ref(false)
const modalRef = ref(null)
const tableRef = ref(null)

// 搜索条件
const queryItems = reactive({
  username: '',
})

// 新增
function handleAdd() {
  modalRef.value?.openAdd()
}

// 编辑
function handleEdit(row) {
  modalRef.value?.openEdit(row)
}

// 刷新表格
function fetchTableData() {
  tableRef.value?.refresh()
}

// 搜索
function handleSearch(searchParams) {
  if (searchParams) {
    queryItems.username = searchParams.username
  }
}

defineOptions({
  name: 'DistributionUsers',
})
</script>
