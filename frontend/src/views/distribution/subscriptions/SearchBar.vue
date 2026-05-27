<template>
  <div class="search-bar">
    <n-space>
      <n-input
        v-model:value="queryItems.name"
        placeholder="搜索订阅名称"
        clearable
        style="width: 200px"
        @keyup.enter="handleSearch"
      />
      <n-select
        v-model:value="queryItems.userId"
        filterable
        placeholder="根据用户搜索"
        :options="queryForm.userId.optionsRef"
        :loading="queryForm.userId.loadingRef"
        clearable
        remote
        @search="queryForm.userId.search"
        @scroll="queryForm.userId.scroll"
      />
      <n-button type="primary" @click="handleSearch">
        <i class="i-material-symbols:search mr-4 text-18"/>
        搜索
      </n-button>
      <n-button type="primary" @click="handleRefresh">
        <i class="i-material-symbols:refresh mr-4 text-18"/>
        刷新
      </n-button>
    </n-space>
  </div>
</template>

<script setup>
import { NSpace, NInput, NSelect, NButton } from 'naive-ui'
import { reactive, onMounted } from 'vue'
import { debounce } from '@/utils'
import distributionUserApi from '../users/api'

const emit = defineEmits(['search', 'refresh'])

// 搜索条件
const queryItems = reactive({
  name: '',
  userId: null,
})

// 用户远程搜索配置
const queryForm = reactive({
  userId: {
    optionsRef: [],
    loadingRef: false,
    debounceSearch: debounce((query = null) => {
      fetchQueryFormOptions({ username: query }, () => {
        queryForm.userId.loadingRef = false
      })
    }, 1000, false),
    search: (query) => {
      if (!query.length) {
        queryForm.userId.optionsRef = []
        return
      }
      queryForm.userId.loadingRef = true
      queryForm.userId.debounceSearch(query)
    },
    scroll: () => {},
  },
})

// 获取用户选项
function fetchQueryFormOptions(params, func) {
  distributionUserApi.getAll(params).then((res) => {
    queryForm.userId.optionsRef = res.data.map((u) => ({ label: u.username, value: u.id }))
    if (func) func(res)
  })
}

function handleSearch() {
  emit('search', {
    name: queryItems.name,
    userId: queryItems.userId,
  })
}

function handleRefresh() {
  emit('refresh')
}

onMounted(() => {
  fetchQueryFormOptions()
})
</script>

<style scoped>
.search-bar {
  margin-bottom: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
