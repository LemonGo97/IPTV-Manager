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
                  <n-list v-if="filterRules.length > 0" bordered>
                    <n-list-item v-for="rule in filterRules" :key="rule.id">
                      <div class="rule-item">
                        <div class="rule-main">
                          <div class="rule-header-row">
                            <h4 class="rule-name">{{ rule.name }}</h4>
                            <n-tag :type="rule.enabled ? 'success' : 'default'" size="small">
                              {{ rule.enabled ? '启用' : '停用' }}
                            </n-tag>
                          </div>
                          <div class="rule-engine">引擎: {{ getEngineLabel(rule.engine) }}</div>
                          <div v-if="getFormattedParams(rule).length > 0" class="rule-params">
                            <div v-for="param in getFormattedParams(rule)" :key="param.label" class="rule-param">
                              <span class="param-label">{{ param.label }}:</span>
                              <span class="param-value">{{ param.value }}</span>
                            </div>
                          </div>
                        </div>
                        <div class="rule-actions">
                          <n-button size="small" @click="handleEdit(rule, 'FILTER')">编辑</n-button>
                          <n-button size="small" type="error" @click="handleDelete(rule)">删除</n-button>
                        </div>
                      </div>
                    </n-list-item>
                  </n-list>
                  <n-empty v-else description="暂无频道过滤规则" size="small" />
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
              <n-list v-if="normalizeRules.length > 0" bordered>
                <n-list-item v-for="rule in normalizeRules" :key="rule.id">
                  <div class="rule-item">
                    <div class="rule-main">
                      <div class="rule-header-row">
                        <h4 class="rule-name">{{ rule.name }}</h4>
                        <n-tag :type="rule.enabled ? 'success' : 'default'" size="small">
                          {{ rule.enabled ? '启用' : '停用' }}
                        </n-tag>
                      </div>
                      <div class="rule-engine">引擎: {{ getEngineLabel(rule.engine) }}</div>
                      <div v-if="getFormattedParams(rule).length > 0" class="rule-params">
                        <div v-for="param in getFormattedParams(rule)" :key="param.label" class="rule-param">
                          <span class="param-label">{{ param.label }}:</span>
                          <span class="param-value">{{ param.value }}</span>
                        </div>
                      </div>
                    </div>
                    <div class="rule-actions">
                      <n-button size="small" @click="handleEdit(rule, 'NAME')">编辑</n-button>
                      <n-button size="small" type="error" @click="handleDelete(rule)">删除</n-button>
                    </div>
                  </div>
                </n-list-item>
              </n-list>
              <n-empty v-else description="暂无名称规范化规则" size="small" />
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
              <n-list v-if="mergeRules.length > 0" bordered>
                <n-list-item v-for="rule in mergeRules" :key="rule.id">
                  <div class="rule-item">
                    <div class="rule-main">
                      <div class="rule-header-row">
                        <h4 class="rule-name">{{ rule.name }}</h4>
                        <n-tag :type="rule.enabled ? 'success' : 'default'" size="small">
                          {{ rule.enabled ? '启用' : '停用' }}
                        </n-tag>
                      </div>
                      <div class="rule-engine">引擎: {{ getEngineLabel(rule.engine) }}</div>
                      <div v-if="getFormattedParams(rule).length > 0" class="rule-params">
                        <div v-for="param in getFormattedParams(rule)" :key="param.label" class="rule-param">
                          <span class="param-label">{{ param.label }}:</span>
                          <span class="param-value">{{ param.value }}</span>
                        </div>
                      </div>
                    </div>
                    <div class="rule-actions">
                      <n-button size="small" @click="handleEdit(rule, 'MERGE')">编辑</n-button>
                      <n-button size="small" type="error" @click="handleDelete(rule)">删除</n-button>
                    </div>
                  </div>
                </n-list-item>
              </n-list>
              <n-empty v-else description="暂无频道合并规则" size="small" />
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
              <n-list v-if="delayRules.length > 0" bordered>
                <n-list-item v-for="rule in delayRules" :key="rule.id">
                  <div class="rule-item">
                    <div class="rule-main">
                      <div class="rule-header-row">
                        <h4 class="rule-name">{{ rule.name }}</h4>
                        <n-tag :type="rule.enabled ? 'success' : 'default'" size="small">
                          {{ rule.enabled ? '启用' : '停用' }}
                        </n-tag>
                      </div>
                      <div class="rule-engine">引擎: {{ getEngineLabel(rule.engine) }}</div>
                      <div v-if="getFormattedParams(rule).length > 0" class="rule-params">
                        <div v-for="param in getFormattedParams(rule)" :key="param.label" class="rule-param">
                          <span class="param-label">{{ param.label }}:</span>
                          <span class="param-value">{{ param.value }}</span>
                        </div>
                      </div>
                    </div>
                    <div class="rule-actions">
                      <n-button size="small" @click="handleEdit(rule, 'DELAY')">编辑</n-button>
                      <n-button size="small" type="error" @click="handleDelete(rule)">删除</n-button>
                    </div>
                  </div>
                </n-list-item>
              </n-list>
              <n-empty v-else description="暂无延迟检测规则" size="small" />
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
              <n-list v-if="groupRules.length > 0" bordered>
                <n-list-item v-for="rule in groupRules" :key="rule.id">
                  <div class="rule-item">
                    <div class="rule-main">
                      <div class="rule-header-row">
                        <h4 class="rule-name">{{ rule.name }}</h4>
                        <n-tag :type="rule.enabled ? 'success' : 'default'" size="small">
                          {{ rule.enabled ? '启用' : '停用' }}
                        </n-tag>
                      </div>
                      <div class="rule-engine">引擎: {{ getEngineLabel(rule.engine) }}</div>
                      <div v-if="getFormattedParams(rule).length > 0" class="rule-params">
                        <div v-for="param in getFormattedParams(rule)" :key="param.label" class="rule-param">
                          <span class="param-label">{{ param.label }}:</span>
                          <span class="param-value">{{ param.value }}</span>
                        </div>
                      </div>
                    </div>
                    <div class="rule-actions">
                      <n-button size="small" @click="handleEdit(rule, 'GROUP')">编辑</n-button>
                      <n-button size="small" type="error" @click="handleDelete(rule)">删除</n-button>
                    </div>
                  </div>
                </n-list-item>
              </n-list>
              <n-empty v-else description="暂无频道分组规则" size="small" />
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
import { NButton, NList, NListItem, NTag, NEmpty } from 'naive-ui'
import { CommonPage } from '@/components'
import { h, onMounted, ref } from 'vue'
import RuleModal from './RuleModal.vue'
import api from './api'

const modalRef = ref(null)
const engines = ref([])

// 检查规则类型是否有可用引擎
function hasEngineForType(ruleType) {
  return engines.value.some(e => e.ruleType === ruleType)
}

// 格式化规则参数
function formatRuleParams(rule, engine) {
  if (!rule.params || !engine.params) return []

  try {
    const params = JSON.parse(rule.params)
    const engineParams = JSON.parse(engine.params)
    const formatted = []

    engineParams.forEach(param => {
      const value = params[param.field]
      if (value === undefined || value === null) return

      let displayValue = value
      let label = param.label

      switch (param.type) {
        case 'SWITCH':
          displayValue = value ? '是' : '否'
          break
        case 'SELECT':
          const option = param.options?.find(opt => opt.value === value)
          displayValue = option?.label || value
          break
        case 'DYNAMIC_INPUT':
          if (Array.isArray(value) && value.length > 0) {
            const filtered = value.filter(v => v && v.trim())
            if (filtered.length === 0) return
            displayValue = filtered.join(', ')
          } else {
            return
          }
          break
        case 'DYNAMIC_PAIR_INPUT':
          if (Array.isArray(value) && value.length > 0) {
            const pairs = value
              .filter(pair => pair.key && pair.key.trim())
              .map(pair => `${pair.key}: ${pair.value || ''}`)
            if (pairs.length === 0) return
            displayValue = pairs.join(' | ')
          } else {
            return
          }
          break
        case 'NUMBER':
          displayValue = String(value)
          break
        case 'INPUT':
        default:
          displayValue = String(value)
          break
      }

      // Skip empty string values
      if (displayValue === '' || displayValue === '[]') return

      formatted.push({ label, value: displayValue })
    })

    return formatted
  } catch (error) {
    console.error('Failed to format params:', error)
    return []
  }
}

// 获取引擎中文名称
function getEngineLabel(engineCode) {
  const engine = engines.value.find(e => e.engine === engineCode)
  return engine?.name || engineCode
}

// 获取格式化后的参数
function getFormattedParams(rule) {
  const engine = engines.value.find(e => e.engine === rule.engine)
  if (!engine) return []
  return formatRuleParams(rule, engine)
}

// 规则数据（从后端获取）
const filterRules = ref([])
const normalizeRules = ref([])
const mergeRules = ref([])
const delayRules = ref([])
const groupRules = ref([])

// 新增规则
function handleAdd(ruleType) {
  modalRef.value?.handleOpen(ruleType)
}

// 编辑规则
function handleEdit(row, ruleType) {
  modalRef.value?.handleOpen(ruleType, row)
}

// 删除规则
async function handleDelete(row) {
  window.$dialog.warning({
    content: `确定删除规则"${row.name}"吗？`,
    title: '提示',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await api.delete(row.id)
        window.$message.success('删除成功')
        await fetchRules() // 刷新列表
      } catch (error) {
        window.$message.error('删除失败')
      }
    },
  })
}

// 保存规则
async function handleSave(data) {
  try {
    if (data.id) {
      await api.update(data.id, data)
      window.$message.success('规则已更新')
    } else {
      await api.create(data)
      window.$message.success('规则已创建')
    }
    await fetchRules() // 刷新列表
  } catch (error) {
    window.$message.error('保存失败: ' + (error.message || '未知错误'))
  }
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

// 获取规则列表
async function fetchRules() {
  try {
    const res = await api.getAll()
    const allRules = res.data || []

    // 按 ruleType 分组
    filterRules.value = allRules.filter(r => r.ruleType === 'FILTER')
    normalizeRules.value = allRules.filter(r => r.ruleType === 'NAME')
    mergeRules.value = allRules.filter(r => r.ruleType === 'MERGE')
    delayRules.value = allRules.filter(r => r.ruleType === 'DELAY')
    groupRules.value = allRules.filter(r => r.ruleType === 'GROUP')
  } catch (error) {
    window.$message.error('获取规则列表失败')
    console.error(error)
  }
}

onMounted(() => {
  fetchSupportEngineList()
  fetchRules()
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

/* 列表项样式 */
.rule-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.rule-main {
  flex: 1;
  min-width: 0;
}

.rule-header-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.rule-name {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
}

.rule-engine {
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
}

.rule-params {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.rule-param {
  display: flex;
  font-size: 12px;
  line-height: 1.5;
}

.param-label {
  color: #666;
  flex-shrink: 0;
  min-width: 80px;
}

.param-value {
  color: #333;
  flex: 1;
  word-break: break-all;
}

.rule-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}
</style>
