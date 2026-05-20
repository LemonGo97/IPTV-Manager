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
                  <div class="rule-header">
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
              <div class="rule-header">
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
              <div class="rule-header">
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
              <div class="rule-header">
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
              <div class="rule-header">
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
    <MeModal ref="modalRef" width="600px" :title="modalTitle" :onOk="handleSave">
      <n-form
        ref="modalFormRef"
        label-placement="left"
        label-align="left"
        :label-width="120"
        :model="modalForm"
      >
        <!-- 规则名称 -->
        <n-form-item
          label="规则名称"
          path="name"
          :rule="{ required: true, message: '请输入规则名称', trigger: ['input', 'blur'] }"
        >
          <n-input v-model:value="modalForm.name" placeholder="请输入规则名称" />
        </n-form-item>

        <!-- 引擎选择（当有多个引擎时显示） -->
        <n-form-item
          v-if="availableEngines.length > 1"
          label="处理引擎"
          path="engine"
          :rule="{ required: true, message: '请选择处理引擎', trigger: ['change', 'blur'] }"
        >
          <n-select
            v-model:value="modalForm.engine"
            :options="engineOptions"
            placeholder="请选择处理引擎"
            @update:value="handleEngineChange"
          />
        </n-form-item>

        <!-- 动态参数表单 -->
        <component :is="renderDynamicParams" />
      </n-form>
    </MeModal>
  </CommonPage>
</template>

<script setup>
import { NButton, NStep, NSteps, NSwitch, NDynamicInput, NInput, NInputNumber, NSelect, NFormItem } from 'naive-ui'
import { CommonPage, MeCrud, MeModal } from '@/components'
import { h, computed, onMounted, ref, render } from 'vue'
import api from './api'

const modalRef = ref(null)
const modalFormRef = ref(null)
const modalForm = ref({})
const modalTitle = ref('')
const currentRuleType = ref('')
const engines = ref([])

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

// 根据规则类型过滤可用引擎
const availableEngines = computed(() => {
  if (!currentRuleType.value) return []
  return engines.value.filter(e => e.ruleType === currentRuleType.value)
})

// 引擎选项
const engineOptions = computed(() => {
  return availableEngines.value.map(e => ({
    label: e.name,
    value: e.engine,
  }))
})

// 当前选中引擎的参数配置
const currentEngineParams = computed(() => {
  if (!modalForm.value.engine) return []
  const engine = availableEngines.value.find(e => e.engine === modalForm.value.engine)
  if (!engine) return []
  try {
    return JSON.parse(engine.params)
  } catch {
    return []
  }
})

// 渲染参数输入组件
function renderParamInput(param) {
  const fieldName = param.field
  const label = param.label
  const type = param.type
  const placeholder = param.placeholder || `请输入${label}`

  switch (type) {
    case 'INPUT':
      return h(NInput, {
        placeholder,
        value: modalForm.value[fieldName],
        'onUpdate:value': val => { modalForm.value[fieldName] = val },
      })

    case 'NUMBER':
      return h(NInputNumber, {
        placeholder,
        value: modalForm.value[fieldName],
        'onUpdate:value': val => { modalForm.value[fieldName] = val },
        style: { width: '100%' },
      })

    case 'SWITCH':
      return h(NSwitch, {
        value: modalForm.value[fieldName],
        'onUpdate:value': val => { modalForm.value[fieldName] = val },
      })

    case 'SELECT':
      return h(NSelect, {
        placeholder,
        options: param.options || [],
        value: modalForm.value[fieldName],
        'onUpdate:value': val => { modalForm.value[fieldName] = val },
      })

    case 'DYNAMIC_INPUT':
      return h(NDynamicInput, {
        placeholder,
        value: modalForm.value[fieldName] || [''],
        'onUpdate:value': val => { modalForm.value[fieldName] = val },
        onCreate: () => '',
      })

    case 'DYNAMIC_PAIR_INPUT':
      return h(NDynamicInput, {
        preset: 'pair',
        keyPlaceholder: param.keyPlaceholder || '键',
        valuePlaceholder: param.valuePlaceholder || '值',
        value: modalForm.value[fieldName] || [{ key: '', value: '' }],
        'onUpdate:value': val => { modalForm.value[fieldName] = val },
        onCreate: () => ({ key: '', value: '' }),
      })

    default:
      return h(NInput, {
        placeholder,
        value: modalForm.value[fieldName],
        'onUpdate:value': val => { modalForm.value[fieldName] = val },
      })
  }
}

// 动态渲染参数表单
const renderDynamicParams = computed(() => {
  const params = currentEngineParams.value

  if (params.length === 0) {
    return h('div', { class: 'text-gray-400 text-center py-4' }, '该引擎无需配置参数')
  }

  return h('div', { class: 'dynamic-params-container' }, params.map(param =>
    h(NFormItem, {
      key: param.field,
      label: param.label,
      path: param.field,
    }, {
      default: () => renderParamInput(param),
    })
  ))
})

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
  currentRuleType.value = ruleType
  modalTitle.value = `新增${getRuleTypeName(ruleType)}`

  // 过滤可用引擎
  const filtered = engines.value.filter(e => e.ruleType === ruleType)

  // 初始化表单
  modalForm.value = {
    name: '',
    engine: filtered.length === 1 ? filtered[0].engine : null,
    enabled: true,
  }

  // 初始化参数字段
  if (filtered.length === 1) {
    initEngineParams(filtered[0])
  }

  modalRef.value?.open()
}

// 编辑规则
function handleEdit(row, ruleType) {
  currentRuleType.value = ruleType
  modalTitle.value = `编辑${getRuleTypeName(ruleType)}`
  modalForm.value = { ...row }
  modalRef.value?.open()
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

// 引擎切换处理
function handleEngineChange(engine) {
  const selectedEngine = availableEngines.value.find(e => e.engine === engine)
  if (selectedEngine) {
    initEngineParams(selectedEngine)
  }
}

// 初始化引擎参数字段
function initEngineParams(engine) {
  try {
    const params = JSON.parse(engine.params)
    params.forEach(param => {
      if (modalForm.value[param.field] === undefined) {
        // 根据类型设置默认值
        switch (param.type) {
          case 'INPUT':
          case 'NUMBER':
            modalForm.value[param.field] = ''
            break
          case 'SWITCH':
            modalForm.value[param.field] = true
            break
          case 'SELECT':
            // 默认选中第一个选项
            modalForm.value[param.field] = (param.options?.[0]?.value) || null
            break
          case 'DYNAMIC_INPUT':
            modalForm.value[param.field] = ['']
            break
          case 'DYNAMIC_PAIR_INPUT':
            modalForm.value[param.field] = [{ key: '', value: '' }]
            break
          default:
            modalForm.value[param.field] = ''
        }
      }
    })
  } catch (error) {
    console.error('Failed to parse engine params:', error)
  }
}

// 保存规则
function handleSave() {
  // 构建参数对象（键值对）
  const paramsObj = {}
  currentEngineParams.value.forEach(param => {
    paramsObj[param.field] = modalForm.value[param.field]
  })

  // 构建提交数据结构
  const submitData = {
    name: modalForm.value.name,
    engine: modalForm.value.engine,
    ruleType: currentRuleType.value,
    enabled: modalForm.value.enabled ?? true,
    params: JSON.stringify(paramsObj),
  }

  console.log('保存规则:', submitData)

  window.$message.success('规则已保存（仅控制台输出，未调用API）')
  modalRef.value?.close()

  // TODO: 调用保存API
}

function getRuleTypeName(type) {
  const names = {
    FILTER: '频道过滤规则',
    NAME: '频道名称规范化规则',
    MERGE: '相同频道合并规则',
    DELAY: '延迟检测规则',
    GROUP: '频道分组规则',
  }
  return names[type] || type
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
.rules-collapse :deep(.n-collapse-item) {
  margin-bottom: 16px;
}

.rules-collapse :deep(.n-collapse-item__header) {
  padding: 12px 16px;
  font-size: 15px;
  font-weight: 500;
}

.rule-content {
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

/* 动态表单样式 */
.n-form-item {
  margin-bottom: 16px;
}

.n-form-item-label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
}

.n-form-item-blank {
  width: 100%;
}
</style>
