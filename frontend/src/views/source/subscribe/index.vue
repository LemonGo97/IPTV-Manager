<template>
  <CommonPage>
    <template #action>
      <NButton type="primary" @click="handleAdd()">
        <i class="i-material-symbols:add mr-4 text-18" />
        添加订阅源
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
      @refresh="handleTableRefresh"
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
  name: '',
  type: null,
  enabled: null,
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

// 表格内部刷新（触发订阅源刷新）
function handleTableRefresh() {
  // 订阅源刷新后需要重新获取数据
  fetchTableData()
}

// 搜索
function handleSearch(searchParams) {
  if (searchParams) {
    Object.assign(queryItems, searchParams)
  }
}

defineOptions({
  name: 'SubscribeConfig',
})
</script>
