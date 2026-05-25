<template>
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
        <n-input v-model:value="modalForm.name" :input-props="{ name: 'name', autocomplete: 'off' }" placeholder="请输入规则名称" />
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
          :input-props="{ name: 'engine', autocomplete: 'off' }"
          placeholder="请选择处理引擎"
          @update:value="handleEngineChange"
        />
      </n-form-item>

      <!-- 动态参数表单 -->
      <component :is="renderDynamicParams" />
    </n-form>
  </MeModal>
</template>

<script setup>
import { NSwitch, NDynamicInput, NInput, NInputNumber, NSelect, NFormItem } from 'naive-ui'
import { MeModal } from '@/components'
import { h, computed, ref } from 'vue'

const props = defineProps({
  engines: {
    type: Array,
    default: () => [],
  },
})

const emit = defineEmits(['save'])

const modalRef = ref(null)
const modalFormRef = ref(null)
const modalForm = ref({})
const modalTitle = ref('')
const currentRuleType = ref('')

// 根据规则类型过滤可用引擎
const availableEngines = computed(() => {
  if (!currentRuleType.value) return []
  return props.engines.filter(e => e.ruleType === currentRuleType.value)
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
        inputProps: { name: fieldName, autocomplete: 'off' },
        'onUpdate:value': val => { modalForm.value[fieldName] = val },
      })

    case 'NUMBER':
      return h(NInputNumber, {
        placeholder,
        value: modalForm.value[fieldName],
        inputProps: { name: fieldName, autocomplete: 'off' },
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
        inputProps: { name: fieldName, autocomplete: 'off' },
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

// 新增/编辑规则
function handleOpen(ruleType, rowData = null) {
  currentRuleType.value = ruleType
  const isEdit = rowData !== null
  const title = `${isEdit ? '编辑' : '新增'}${getRuleTypeName(ruleType)}`

  // 过滤可用引擎
  const filtered = props.engines.filter(e => e.ruleType === ruleType)

  if (isEdit) {
    // 编辑模式：初始化表单
    modalForm.value = {
      name: '',
      engine: filtered.length === 1 ? filtered[0].engine : null,
      enabled: true,
    }

    // 初始化引擎参数字段
    const engine = filtered.find(e => e.engine === rowData.engine)
    if (engine) {
      initEngineParams(engine)
    }

    // 填充现有数据
    modalForm.value = {
      ...modalForm.value,
      id: rowData.id,
      name: rowData.name,
      engine: rowData.engine,
      enabled: rowData.enabled,
    }

    // 解析并填充参数字段
    if (rowData.params) {
      try {
        const paramsObj = JSON.parse(rowData.params)
        Object.assign(modalForm.value, paramsObj)
      } catch (error) {
        console.error('Failed to parse params:', error)
      }
    }
  } else {
    // 新增模式：初始化表单
    modalForm.value = {
      name: '',
      engine: filtered.length === 1 ? filtered[0].engine : null,
      enabled: true,
    }

    // 初始化参数字段
    if (filtered.length === 1) {
      initEngineParams(filtered[0])
    }
  }

  // 传入 title 确保 title 正确显示
  modalRef.value?.open({ title })
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
            modalForm.value[param.field] = ''
            break
          case 'NUMBER':
            modalForm.value[param.field] = null
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
            modalForm.value[param.field] = null
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
    id: modalForm.value.id,
    name: modalForm.value.name,
    engine: modalForm.value.engine,
    ruleType: currentRuleType.value,
    enabled: modalForm.value.enabled ?? true,
    params: JSON.stringify(paramsObj),
  }

  emit('save', submitData)
  modalRef.value?.close()
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

// 暴露方法供父组件调用
defineExpose({
  handleOpen,
})
</script>

<style scoped>
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
