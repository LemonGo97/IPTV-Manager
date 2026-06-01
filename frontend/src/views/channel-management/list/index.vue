<template>
  <CommonPage>
    <!-- 统计数据区域 -->
    <StatisticsArea ref="statisticsRef" />

    <!-- 处理步骤区域 -->
    <StepsArea ref="stepsRef" />

    <n-divider />

    <!-- 搜索和操作栏 -->
    <SearchBar
      @search="handleSearch"
      @refresh="handleRefresh"
      v-model:checkedRows="checkedRows"
    />

    <!-- 数据表格 -->
    <DataTable
      ref="tableRef"
      :search-params="queryItems"
      @view-epg="handleViewEpg"
      @update-filters="handleFiltersChange"
      @preview="handlePreview"
      @row-check="handleCheck"
    />

    <!-- EPG弹窗 -->
    <EpgModal ref="epgModalRef" v-model:visible="epgModalVisible" />

    <!-- 预览弹窗 -->
    <PreviewModal ref="previewModalRef" v-model:visible="previewModalVisible" />
  </CommonPage>
</template>

<script setup>
import { NDivider } from 'naive-ui'
import { reactive, ref } from 'vue'
import StatisticsArea from './StatisticsArea.vue'
import StepsArea from './StepsArea.vue'
import SearchBar from './SearchBar.vue'
import DataTable from './DataTable.vue'
import EpgModal from './EpgModal.vue'
import PreviewModal from './PreviewModal.vue'

// 弹窗显示状态
const epgModalVisible = ref(false)
const previewModalVisible = ref(false)

// 组件引用
const statisticsRef = ref(null)
const stepsRef = ref(null)
const tableRef = ref(null)
const epgModalRef = ref(null)
const previewModalRef = ref(null)

const checkedRows = ref([])


// 搜索参数
const queryItems = reactive({
  name: '',
  providerId: null,
  groupId: null,
  status: null,
})

// 处理过滤器变化
function handleFiltersChange(filters) {
  queryItems.providerId = filters['provider.name'] || null
  queryItems.groupId = filters['channelGroup.name'] || null
  queryItems.status = filters['status'] || null
}

// 搜索
function handleSearch(searchParams) {
  if (searchParams) {
    Object.assign(queryItems, searchParams)
  }
}

// 刷新
function handleRefresh() {
  statisticsRef.value?.refresh()
  tableRef.value?.refresh()
}

// 查看EPG
function handleViewEpg(row) {
  epgModalRef.value?.open(row)
}

// 预览
function handlePreview({ key, row }) {
  previewModalRef.value?.open(key, row)
}

// 处理选中行
function handleCheck(rowKeys) {
  console.log('选中的行:', rowKeys)
  checkedRows.value = rowKeys
}

defineOptions({
  name: 'ChannelList',
})
</script>
