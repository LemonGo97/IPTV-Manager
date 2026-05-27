<template>
  <CommonPage>
    <SearchBar
      @search="handleSearch"
      @refresh="fetchTableData"
    />

    <DataTable
      ref="tableRef"
      :search-params="queryItems"
      @view-detail="handleViewDetail"
      @view-playlist="handleViewPlaylist"
    />

    <!-- 任务详情弹窗 -->
    <DetailModal
      v-model:visible="detailVisible"
      :task="currentTask"
    />

    <!-- 频道列表弹窗 -->
    <PlaylistModal
      v-model:visible="playlistVisible"
      :task-id="currentTaskId"
    />
  </CommonPage>
</template>

<script setup>
import { reactive, ref } from 'vue'
import SearchBar from './SearchBar.vue'
import DataTable from './DataTable.vue'
import DetailModal from './DetailModal.vue'
import PlaylistModal from './PlaylistModal.vue'
import {CommonPage} from "@/components/index.js"

const tableRef = ref(null)

// 搜索条件
const queryItems = reactive({
  taskId: '',
  providerName: '',
  triggerType: null,
  status: null,
  startTime: null,
  endTime: null,
})

// 详情弹窗
const detailVisible = ref(false)
const currentTask = ref(null)

// 频道列表弹窗
const playlistVisible = ref(false)
const currentTaskId = ref(null)

// 刷新表格
function fetchTableData() {
  tableRef.value?.refresh()
}

// 搜索
function handleSearch(searchParams) {
  if (searchParams) {
    Object.assign(queryItems, searchParams)
  }
}

// 查看详情
function handleViewDetail(row) {
  currentTask.value = row
  detailVisible.value = true
}

// 查看频道列表
function handleViewPlaylist(row) {
  currentTaskId.value = row.id
  playlistVisible.value = true
}

defineOptions({
  name: 'TaskHistory',
})
</script>
