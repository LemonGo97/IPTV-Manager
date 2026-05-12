<template>
  <CommonPage>
    <n-collapse class="rules-collapse" :default-expanded-names="['filter']" accordion>
      <!-- 频道过滤规则 -->
      <n-collapse-item title="频道过滤规则" name="filter">
        <div class="rule-content">
          <div class="rule-header">
            <n-button size="small" type="primary" @click="handleAddFilter">
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
          <div class="rule-actions">
            <n-button @click="handleCancelFilter">取消</n-button>
            <n-button type="primary" @click="handleSaveFilter">保存并应用</n-button>
          </div>
        </div>
      </n-collapse-item>

      <!-- 频道名称规范化规则 -->
      <n-collapse-item title="频道名称规范化规则" name="normalize">
        <div class="rule-content">
          <div class="rule-header">
            <n-button size="small" type="primary" @click="handleAddNormalize">
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
          <div class="rule-actions">
            <n-button @click="handleCancelNormalize">取消</n-button>
            <n-button type="primary" @click="handleSaveNormalize">保存并应用</n-button>
          </div>
        </div>
      </n-collapse-item>

      <!-- 延迟检测规则 -->
      <n-collapse-item title="延迟检测规则" name="delay">
        <div class="rule-content">
          <div class="rule-header">
            <n-button size="small" type="primary" @click="handleAddDelay">
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
          <div class="rule-actions">
            <n-button @click="handleCancelDelay">取消</n-button>
            <n-button type="primary" @click="handleSaveDelay">保存并应用</n-button>
          </div>
        </div>
      </n-collapse-item>

      <!-- 频道分组规则 -->
      <n-collapse-item title="频道分组规则" name="group">
        <div class="rule-content">
          <div class="rule-header">
            <n-button size="small" type="primary" @click="handleAddGroup">
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
          <div class="rule-actions">
            <n-button @click="handleCancelGroup">取消</n-button>
            <n-button type="primary" @click="handleSaveGroup">保存并应用</n-button>
          </div>
        </div>
      </n-collapse-item>
    </n-collapse>

    <!-- 规则编辑弹窗 -->
    <MeModal ref="modalRef" width="600px" :title="modalTitle">
      <n-form
        ref="modalFormRef"
        label-placement="left"
        label-align="left"
        :label-width="120"
        :model="modalForm"
      >
        <!-- 动态表单内容，根据规则类型渲染 -->
        <n-form-item
          label="规则名称"
          path="name"
          :rule="{ required: true, message: '请输入规则名称', trigger: ['input', 'blur'] }"
        >
          <n-input v-model:value="modalForm.name" placeholder="请输入规则名称" />
        </n-form-item>

        <n-form-item
          label="匹配条件"
          path="condition"
          :rule="{ required: true, message: '请输入匹配条件', trigger: ['input', 'blur'] }"
        >
          <n-input v-model:value="modalForm.condition" placeholder="请输入匹配条件" />
        </n-form-item>

        <n-form-item
          label="匹配类型"
          path="matchType"
          :rule="{ required: true, message: '请选择匹配类型', trigger: ['change', 'blur'] }"
        >
          <n-select
            v-model:value="modalForm.matchType"
            :options="[
              { label: '包含', value: 'contains' },
              { label: '等于', value: 'equals' },
              { label: '正则表达式', value: 'regex' },
            ]"
          />
        </n-form-item>

        <n-form-item
          v-if="currentRuleType !== 'filter'"
          label="操作值"
          path="actionValue"
        >
          <n-input v-model:value="modalForm.actionValue" placeholder="请输入操作值" />
        </n-form-item>

        <n-form-item
          v-if="currentRuleType === 'group'"
          label="目标分组"
          path="targetGroup"
          :rule="{ required: true, message: '请选择目标分组', trigger: ['change', 'blur'] }"
        >
          <n-select
            v-model:value="modalForm.targetGroup"
            :options="groupOptions"
            placeholder="请选择目标分组"
          />
        </n-form-item>

        <n-form-item label="启用状态" path="enabled">
          <n-switch v-model:value="modalForm.enabled">
            <template #checked>启用</template>
            <template #unchecked>停用</template>
          </n-switch>
        </n-form-item>
      </n-form>
    </MeModal>
  </CommonPage>
</template>

<script setup>
import { NButton, NSwitch } from 'naive-ui'
import { CommonPage, MeCrud, MeModal } from '@/components'
import { h } from 'vue'

const modalRef = ref(null)
const modalFormRef = ref(null)
const modalForm = ref({})
const modalTitle = ref('')
const currentRuleType = ref('')

// 模拟数据
const filterRules = ref([
  { id: 1, name: '过滤测试频道', condition: '测试', matchType: 'contains', enabled: true },
  { id: 2, name: '过滤广告频道', condition: '广告', matchType: 'contains', enabled: false },
])

const normalizeRules = ref([
  { id: 1, name: 'CCTV统一命名', condition: 'CCTV', matchType: 'contains', actionValue: 'CCTV中央台', enabled: true },
  { id: 2, name: '去除标清标识', condition: '[480P]', matchType: 'regex', actionValue: '', enabled: true },
])

const delayRules = ref([
  { id: 1, name: '检测源延迟', condition: 'freetv.fun', matchType: 'contains', actionValue: '2000', enabled: true },
  { id: 2, name: '检测源延迟', condition: 'iptv.com', matchType: 'contains', actionValue: '3000', enabled: false },
])

const groupRules = ref([
  { id: 1, name: '央视频道分组', condition: 'CCTV', matchType: 'contains', targetGroup: '1', enabled: true },
  { id: 2, name: '卫视频道分组', condition: '卫视', matchType: 'contains', targetGroup: '2', enabled: true },
])

const groupOptions = ref([
  { label: '央视', value: '1' },
  { label: '卫视', value: '2' },
  { label: '地方台', value: '3' },
])

// 频道过滤规则列
const filterColumns = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '规则名称', key: 'name', width: 200 },
  { title: '匹配条件', key: 'condition', width: 150 },
  { title: '匹配类型', key: 'matchType', width: 100, render: (row) => getMatchTypeLabel(row.matchType) },
  { title: '启用状态', key: 'enabled', width: 100, render: (row) => h('span', row.enabled ? '启用' : '停用') },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    fixed: 'right',
    render: (row) =>
      h('div', { class: 'flex gap-8' }, [
        h(NButton, { size: 'small', onClick: () => handleEdit(row, 'filter') }, { default: () => '编辑' }),
        h(NButton, { size: 'small', type: 'error', onClick: () => handleDelete(row, 'filter') }, { default: () => '删除' }),
      ]),
  },
]

// 频道名称规范化规则列
const normalizeColumns = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '规则名称', key: 'name', width: 200 },
  { title: '匹配条件', key: 'condition', width: 150 },
  { title: '匹配类型', key: 'matchType', width: 100, render: (row) => getMatchTypeLabel(row.matchType) },
  { title: '替换为', key: 'actionValue', width: 150 },
  { title: '启用状态', key: 'enabled', width: 100, render: (row) => h('span', row.enabled ? '启用' : '停用') },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    fixed: 'right',
    render: (row) =>
      h('div', { class: 'flex gap-8' }, [
        h(NButton, { size: 'small', onClick: () => handleEdit(row, 'normalize') }, { default: () => '编辑' }),
        h(NButton, { size: 'small', type: 'error', onClick: () => handleDelete(row, 'normalize') }, { default: () => '删除' }),
      ]),
  },
]

// 延迟检测规则列
const delayColumns = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '规则名称', key: 'name', width: 200 },
  { title: '匹配条件', key: 'condition', width: 150 },
  { title: '匹配类型', key: 'matchType', width: 100, render: (row) => getMatchTypeLabel(row.matchType) },
  { title: '超时时间(ms)', key: 'actionValue', width: 120 },
  { title: '启用状态', key: 'enabled', width: 100, render: (row) => h('span', row.enabled ? '启用' : '停用') },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    fixed: 'right',
    render: (row) =>
      h('div', { class: 'flex gap-8' }, [
        h(NButton, { size: 'small', onClick: () => handleEdit(row, 'delay') }, { default: () => '编辑' }),
        h(NButton, { size: 'small', type: 'error', onClick: () => handleDelete(row, 'delay') }, { default: () => '删除' }),
      ]),
  },
]

// 频道分组规则列
const groupColumns = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '规则名称', key: 'name', width: 200 },
  { title: '匹配条件', key: 'condition', width: 150 },
  { title: '匹配类型', key: 'matchType', width: 100, render: (row) => getMatchTypeLabel(row.matchType) },
  { title: '目标分组', key: 'targetGroup', width: 120, render: (row) => getGroupName(row.targetGroup) },
  { title: '启用状态', key: 'enabled', width: 100, render: (row) => h('span', row.enabled ? '启用' : '停用') },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    fixed: 'right',
    render: (row) =>
      h('div', { class: 'flex gap-8' }, [
        h(NButton, { size: 'small', onClick: () => handleEdit(row, 'group') }, { default: () => '编辑' }),
        h(NButton, { size: 'small', type: 'error', onClick: () => handleDelete(row, 'group') }, { default: () => '删除' }),
      ]),
  },
]

function getMatchTypeLabel(type) {
  const labels = {
    contains: '包含',
    equals: '等于',
    regex: '正则',
  }
  return labels[type] || type
}

function getGroupName(value) {
  const group = groupOptions.value.find(g => g.value === value)
  return group ? group.label : value
}

// 新增规则
function handleAddFilter() {
  currentRuleType.value = 'filter'
  modalTitle.value = '新增频道过滤规则'
  modalForm.value = { name: '', condition: '', matchType: 'contains', enabled: true }
  modalRef.value?.open()
}

function handleAddNormalize() {
  currentRuleType.value = 'normalize'
  modalTitle.value = '新增频道名称规范化规则'
  modalForm.value = { name: '', condition: '', matchType: 'contains', actionValue: '', enabled: true }
  modalRef.value?.open()
}

function handleAddDelay() {
  currentRuleType.value = 'delay'
  modalTitle.value = '新增延迟检测规则'
  modalForm.value = { name: '', condition: '', matchType: 'contains', actionValue: '', enabled: true }
  modalRef.value?.open()
}

function handleAddGroup() {
  currentRuleType.value = 'group'
  modalTitle.value = '新增频道分组规则'
  modalForm.value = { name: '', condition: '', matchType: 'contains', targetGroup: null, enabled: true }
  modalRef.value?.open()
}

// 编辑规则
function handleEdit(row, type) {
  currentRuleType.value = type
  modalTitle.value = `编辑${getRuleTypeName(type)}`
  modalForm.value = { ...row }
  modalRef.value?.open()
}

// 删除规则
function handleDelete(row, type) {
  window.$dialog.warning({
    content: `确定删除该${getRuleTypeName(type)}吗？`,
    title: '提示',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: () => {
      window.$message.success('删除成功')
      // TODO: 调用删除API
    },
  })
}

// 保存并应用
function handleSaveFilter() {
  window.$message.success('频道过滤规则已保存并应用')
  // TODO: 调用保存API
}

function handleSaveNormalize() {
  window.$message.success('频道名称规范化规则已保存并应用')
  // TODO: 调用保存API
}

function handleSaveDelay() {
  window.$message.success('延迟检测规则已保存并应用')
  // TODO: 调用保存API
}

function handleSaveGroup() {
  window.$message.success('频道分组规则已保存并应用')
  // TODO: 调用保存API
}

// 取消
function handleCancelFilter() {
  window.$message.info('已取消修改')
  // TODO: 重置数据
}

function handleCancelNormalize() {
  window.$message.info('已取消修改')
  // TODO: 重置数据
}

function handleCancelDelay() {
  window.$message.info('已取消修改')
  // TODO: 重置数据
}

function handleCancelGroup() {
  window.$message.info('已取消修改')
  // TODO: 重置数据
}

function getRuleTypeName(type) {
  const names = {
    filter: '频道过滤规则',
    normalize: '频道名称规范化规则',
    delay: '延迟检测规则',
    group: '频道分组规则',
  }
  return names[type] || type
}

defineOptions({
  name: 'ChannelRules',
})
</script>

<style scoped>
.rules-collapse :deep(.n-collapse-item) {
  margin-bottom: 16px;
}

.rules-collapse :deep(.n-collapse-item__header) {
  padding: 12px 16px;
  font-size: 15px;
  font-weight: 500;
}

.rule-content {
  padding: 16px;
  background-color: var(--n-color-modal);
  border-radius: 4px;
}

.rule-header {
  margin-bottom: 16px;
  display: flex;
  justify-content: flex-end;
}

.rule-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--n-divider-color);
}
</style>
