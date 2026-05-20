<template>
  <CommonPage>

    <n-collapse class="rules-collapse" :default-expanded-names="['filter']" accordion>
      <n-steps vertical>
        <n-step title="频道过滤" description="根据规则过滤无效和测试频道">
          <template #title>
            频道过滤
          </template>
          <template #default>
            根据规则过滤无效和测试频道
                <div class="rule-content">
                  <div class="rule-header" v-if="hasEngineForType('FILTER')">
                    <n-button size="small" type="primary" @click="handleAdd('FILTER')">
                      <i class="i-material-symbols:add mr-4 text-16" />
                      新增规则
                    </n-button>
                  </div>
                  <n-data-table
                    :columns="filterColumns"
                    :data="filterRules"
                    :pagination="false"
                    :scroll-x="1200"
                    size="small"
                  />
                </div>
          </template>
        </n-step>
        <n-step title="名称规范化" description="统一频道名称格式和标识">
          <template #default>
            统一频道名称格式和标识
            <div class="rule-content">
              <div class="rule-header" v-if="hasEngineForType('NAME')">
                <n-button size="small" type="primary" @click="handleAdd('NAME')">
                  <i class="i-material-symbols:add mr-4 text-16" />
                  新增规则
                </n-button>
              </div>
              <n-data-table
                :columns="normalizeColumns"
                :data="normalizeRules"
                :pagination="false"
                :scroll-x="1200"
                size="small"
              />
            </div>
          </template>
        </n-step>
        <n-step title="相同频道合并" description="合并相同频道为多条线路">
          <template #default>
            合并相同频道为多条线路
            <div class="rule-content">
              <div class="rule-header" v-if="hasEngineForType('MERGE')">
                <n-button size="small" type="primary" @click="handleAdd('MERGE')">
                  <i class="i-material-symbols:add mr-4 text-16" />
                  新增规则
                </n-button>
              </div>
              <n-data-table
                :columns="mergeColumns"
                :data="mergeRules"
                :pagination="false"
                :scroll-x="1200"
                size="small"
              />
            </div>
          </template>
        </n-step>
        <n-step title="延迟检测" description="检测各源的延迟情况">
          <template #default>
            检测各源的延迟情况
            <div class="rule-content">
              <div class="rule-header" v-if="hasEngineForType('DELAY')">
                <n-button size="small" type="primary" @click="handleAdd('DELAY')">
                  <i class="i-material-symbols:add mr-4 text-16" />
                  新增规则
                </n-button>
              </div>
              <n-data-table
                :columns="delayColumns"
                :data="delayRules"
                :pagination="false"
                :scroll-x="1200"
                size="small"
              />
            </div>
          </template>
        </n-step>
        <n-step title="频道分组" description="按类别整理归类频道">
          <template #default>
            按类别整理归类频道
            <div class="rule-content">
              <div class="rule-header" v-if="hasEngineForType('GROUP')">
                <n-button size="small" type="primary" @click="handleAdd('GROUP')">
                  <i class="i-material-symbols:add mr-4 text-16" />
                  新增规则
                </n-button>
              </div>
              <n-data-table
                :columns="groupColumns"
                :data="groupRules"
                :pagination="false"
                :scroll-x="1200"
                size="small"
              />
            </div>
          </template>
        </n-step>
        <n-step title="信号评分" description="评估源信号质量并打分">
          <template #default>
          </template>
        </n-step>
      </n-steps>
    </n-collapse>

    <!-- 动态规则编辑弹窗 -->
    <RuleModal ref="modalRef" :engines="engines" @save="handleSave" />
  </CommonPage>
</template>

<script setup>
import { NButton } from 'naive-ui'
import { CommonPage, MeCrud } from '@/components'
import { h, onMounted, ref } from 'vue'
import RuleModal from './RuleModal.vue'
import api from './api'

const modalRef = ref(null)
const engines = ref([])

// 检查规则类型是否有可用引擎
function hasEngineForType(ruleType) {
  return engines.value.some(e => e.ruleType === ruleType)
}

// 模拟数据
const filterRules = ref([
  { id: 1, name: '过滤测试频道', engine: 'BlackListEngine', enabled: true },
  { id: 2, name: '过滤广告频道', engine: 'WhiteListEngine', enabled: false },
])

const normalizeRules = ref([
  { id: 1, name: 'CCTV统一命名', engine: 'NameNormalizeEngine', enabled: true },
  { id: 2, name: '去除标清标识', engine: 'RemoveSuffixEngine', enabled: true },
])

const mergeRules = ref([
  { id: 1, name: '合并相同频道', engine: 'ChannelMergeEngine', enabled: true },
])

const delayRules = ref([
  { id: 1, name: '检测源延迟', engine: 'DelayDetectionEngine', enabled: true },
  { id: 2, name: '检测源延迟', engine: 'TimeoutDetectionEngine', enabled: false },
])

const groupRules = ref([
  { id: 1, name: '央视频道分组', engine: 'ChannelGroupEngine', enabled: true },
  { id: 2, name: '卫视频道分组', engine: 'ChannelGroupEngine', enabled: true },
])

// 频道过滤规则列
const filterColumns = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '规则名称', key: 'name', width: 200 },
  { title: '处理引擎', key: 'engine', width: 200 },
  { title: '启用状态', key: 'enabled', width: 100, render: row => h('span', row.enabled ? '启用' : '停用') },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    fixed: 'right',
    render: row =>
      h('div', { class: 'flex gap-8' }, [
        h(NButton, { size: 'small', onClick: () => handleEdit(row, 'FILTER') }, { default: () => '编辑' }),
        h(NButton, { size: 'small', type: 'error', onClick: () => handleDelete(row) }, { default: () => '删除' }),
      ]),
  },
]

// 频道名称规范化规则列
const normalizeColumns = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '规则名称', key: 'name', width: 200 },
  { title: '处理引擎', key: 'engine', width: 200 },
  { title: '启用状态', key: 'enabled', width: 100, render: row => h('span', row.enabled ? '启用' : '停用') },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    fixed: 'right',
    render: row =>
      h('div', { class: 'flex gap-8' }, [
        h(NButton, { size: 'small', onClick: () => handleEdit(row, 'NAME') }, { default: () => '编辑' }),
        h(NButton, { size: 'small', type: 'error', onClick: () => handleDelete(row) }, { default: () => '删除' }),
      ]),
  },
]

// 相同频道合并规则列
const mergeColumns = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '规则名称', key: 'name', width: 200 },
  { title: '处理引擎', key: 'engine', width: 200 },
  { title: '启用状态', key: 'enabled', width: 100, render: row => h('span', row.enabled ? '启用' : '停用') },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    fixed: 'right',
    render: row =>
      h('div', { class: 'flex gap-8' }, [
        h(NButton, { size: 'small', onClick: () => handleEdit(row, 'MERGE') }, { default: () => '编辑' }),
        h(NButton, { size: 'small', type: 'error', onClick: () => handleDelete(row) }, { default: () => '删除' }),
      ]),
  },
]

// 延迟检测规则列
const delayColumns = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '规则名称', key: 'name', width: 200 },
  { title: '处理引擎', key: 'engine', width: 200 },
  { title: '启用状态', key: 'enabled', width: 100, render: row => h('span', row.enabled ? '启用' : '停用') },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    fixed: 'right',
    render: row =>
      h('div', { class: 'flex gap-8' }, [
        h(NButton, { size: 'small', onClick: () => handleEdit(row, 'DELAY') }, { default: () => '编辑' }),
        h(NButton, { size: 'small', type: 'error', onClick: () => handleDelete(row) }, { default: () => '删除' }),
      ]),
  },
]

// 频道分组规则列
const groupColumns = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '规则名称', key: 'name', width: 200 },
  { title: '处理引擎', key: 'engine', width: 200 },
  { title: '启用状态', key: 'enabled', width: 100, render: row => h('span', row.enabled ? '启用' : '停用') },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    fixed: 'right',
    render: row =>
      h('div', { class: 'flex gap-8' }, [
        h(NButton, { size: 'small', onClick: () => handleEdit(row, 'GROUP') }, { default: () => '编辑' }),
        h(NButton, { size: 'small', type: 'error', onClick: () => handleDelete(row) }, { default: () => '删除' }),
      ]),
  },
]

// 新增规则
function handleAdd(ruleType) {
  modalRef.value?.handleOpen(ruleType)
}

// 编辑规则
function handleEdit(row, ruleType) {
  modalRef.value?.handleOpen(ruleType, row)
}

// 删除规则
function handleDelete(row) {
  window.$dialog.warning({
    content: `确定删除该规则吗？`,
    title: '提示',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: () => {
      window.$message.success('删除成功')
      // TODO: 调用删除API
    },
  })
}

// 保存规则
function handleSave(data) {
  console.log('保存规则:', data)

  window.$message.success('规则已保存（仅控制台输出，未调用API）')

  // TODO: 调用保存API
}

// 获取支持引擎列表
async function fetchSupportEngineList() {
  try {
    const res = await api.listEngine()
    engines.value = res.data || []
    console.log('支持的引擎列表:', engines.value)
  } catch (error) {
    window.$message.error('获取引擎支持列表失败')
    console.error(error)
  }
}

onMounted(() => {
  fetchSupportEngineList()
})

defineOptions({
  name: 'ChannelRules',
})
</script>

<style scoped>
.rule-content {
  border-radius: 4px;
}

.rule-header {
  margin-bottom: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
