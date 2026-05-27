import { request } from '@/utils'

export default {
  // 获取所有分发订阅源
  getAll: (params) => request.get('/distribution/subscriptions', { params }),
  // 获取单个分发订阅源
  getById: (id) => request.get(`/distribution/subscriptions/${id}`),
  // 创建分发订阅源
  create: (data) => request.post('/distribution/subscriptions', data),
  // 更新分发订阅源
  update: (id, data) => request.put(`/distribution/subscriptions/${id}`, data),
  // 删除分发订阅源
  delete: (id) => request.delete(`/distribution/subscriptions/${id}`),
  // 启用/禁用分发订阅源
  toggleEnabled: (id) => request.post(`/distribution/subscriptions/${id}/toggle`),
  // 获取订阅 URL
  getSubscriptionUrl: (id) => request.get(`/distribution/subscriptions/${id}/url`),
  // 复制订阅 URL
  copySubscriptionUrl: (id) => request.post(`/distribution/subscriptions/${id}/copy`),
}
