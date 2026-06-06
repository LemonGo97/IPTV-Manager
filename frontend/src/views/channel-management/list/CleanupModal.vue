<template>
  <n-modal
    :show="props.visible"
    preset="card"
    title='数据清洗'
    :style="{ width: '500px' }"
    @update:show="(val) => emit('update:visible', val)"
  >
    <n-form
      ref="modalFormRef"
      label-placement="left"
      label-align="left"
      :label-width="100"
      :model="modalForm"
    >
      <n-form-item
        label="清洗操作类型"
        path="ruleType"
      >
        <n-select
          v-model:value="modalForm.ruleType"
          :options="modalFormConfig.ruleType"
          @update:value="modalForm.ruleId = []"
          placeholder="未选择任何项时则执行全部"
          multiple
          clearable>
          <template #header>
            未选择任何项时则执行全部
          </template>
        </n-select>
      </n-form-item>
      <n-form-item
        label="清洗规则"
        path="ruleId"
      >
        <n-tree-select
          multiple
          cascade
          checkable
          block-line
          clearable
          placeholder="未选择任何项时则表示选择全部符合条件的规则"
          v-model:value="modalForm.ruleId"
          :override-default-node-click-behavior="override"
          check-strategy="child"
          :options="ruleTree"
        />
      </n-form-item>
      <n-form-item
        label="订阅源"
        path="channelProviderId"
      >
        <n-select
          v-model:value="modalForm.channelProviderId"
          :options="channelProviderList"
          placeholder="未选择任何项时则执行全部"
          multiple
          clearable>
          <template #header>
            未选择任何项时则执行全部
          </template>
        </n-select>
      </n-form-item>
      <n-form-item
        label="频道状态"
        path="status"
      >
        <n-select
          v-model:value="modalForm.status"
          :options="modalFormConfig.status"
          placeholder="未选择任何项时则执行全部"
          multiple
          clearable>
          <template #header>
            未选择任何项时则执行全部
          </template>
        </n-select>
      </n-form-item>
    </n-form>
    <template #action>
      <n-space class="mt-4 flex justify-end" :size="16">
        <n-button @click="handleClose">
          取消
        </n-button>
        <n-button type="primary" @click="handleStartCleanup">
          开始执行
        </n-button>
      </n-space>
    </template>
  </n-modal>
</template>

<script setup>
import {NModal, NSpace, NButton, NForm, NInput, NFormItem} from 'naive-ui'
import {computed, onMounted, reactive, ref} from "vue";
import api from './api'
import cleanupRuleApi from '@/views/channel-management/rules/api'
import channelProviderApi from '@/views/source/subscribe/api'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['update:visible'])

const modalFormRef = ref(null)


const modalFormConfig = {
  ruleType: [
    {
      label: '频道过滤',
      value: 'FILTER',
    },
    {
      label: '名称规范化',
      value: 'NAME',
    },
    {
      label: '延迟检测',
      value: 'DELAY',
    },
    {
      label: '频道分组',
      value: 'GROUP',
    },
    {
      label: '相同频道合并',
      value: 'MERGE',
    },

  ],
  status: [
    {
      label: '未知',
      value: 'unknown',
    },
    {
      label: '无效',
      value: 'invalid',
    },
    {
      label: '有效',
      value: 'valid',
    },
  ]
}


const ruleList = ref([])
const channelProviderList = ref([])

// 表单数据
const modalForm = reactive({
  ruleType: [],
  ruleId: [],
  channelProviderId: [],
  channelId: [],
  status: [],
})

const override = ({ option }) => {
  if (option.children) {
    return "toggleExpand";
  }
  return "default";
};


function open() {
  emit('update:visible', true)
  fetchCleanupRuleList()
  fetchChannelProviderList()
}

function fetchCleanupRuleList(){
  cleanupRuleApi.getAll().then(res => {
    ruleList.value = res.data || []
  })
}

function fetchChannelProviderList() {
  channelProviderApi.getAll().then(res => {
    let arr = res.data || []
    channelProviderList.value = arr.map((item) => {return {label: item.name, value: item.id}})
  })
}

const ruleTree = computed(() => {
  let arr = ruleList.value
  if (modalForm.ruleType && modalForm.ruleType.length > 0) {
    arr = arr.filter(item => {
      for (let ruleType of modalForm.ruleType) {
        if (item.ruleType === ruleType) {
          return true
        }
      }
      return false
    })
  }

  let tree = []
  for (let ruleType of modalFormConfig.ruleType) {
    let contains = false
    for (let ruleType1 of modalForm.ruleType) {
      if (ruleType.value === ruleType1) {
        contains = true
        break
      }
    }
    let parentNode = {
      label: ruleType.label,
      key: ruleType.value,
      isLeaf: false
    }
    if (modalForm.ruleType && modalForm.ruleType.length > 0) {
      parentNode.disabled = !contains
      if (contains) {
        parentNode.children = arr.filter(r => r.ruleType === ruleType.value).map(item => {
          return {label: item.name, key: item.id, isLeaf: true}
        })
      } else {
        parentNode.children = []
      }
    } else {
      parentNode.children = arr.filter(r => r.ruleType === ruleType.value).map(item => {
        return {label: item.name, key: item.id, isLeaf: true}
      })
    }
    tree.push(parentNode)
  }
  return tree
})


// 关闭弹窗
function handleClose() {
  emit('update:visible', false)
}

function handleStartCleanup() {
  let _emit = emit
  api.dataClean(modalForm).then((res) => {
    $message.info('频道列表开始数据清洗并生成新的频道列表中...')
    _emit('update:visible', false)
  })
}

// 暴露方法给父组件
defineExpose({
  open,
})
</script>
