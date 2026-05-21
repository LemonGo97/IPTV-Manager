import { request } from '@/utils'

export default {
  // 获取支持的处理引擎
  listEngine: () => request.get('/channel/cleanup/engines'),

  // 获取所有规则（支持按 ruleType 过滤）
  getAll: (params) => request.get('/channel/cleanup/rules', { params }),

  // 获取单个规则详情
  getById: (id) => request.get(`/channel/cleanup/rules/${id}`),

  // 创建规则
  create: (data) => request.post('/channel/cleanup/rules', data),

  // 更新规则
  update: (id, data) => request.put(`/channel/cleanup/rules/${id}`, data),

  // 删除规则
  delete: (id) => request.delete(`/channel/cleanup/rules/${id}`),

  // 重排序规则
  reorder: (ruleType, ruleIds) => request.put('/channel/cleanup/rules/reorder', {
    ruleType,
    ruleIds,
  }),
}
