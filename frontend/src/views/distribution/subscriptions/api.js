import { request } from '@/utils'

export default {
  // 获取分发订阅列表
  getList: (params) => request.get('/distribution/subscriptions', { params }),
  // 获取分发订阅总数
  count: () => request.get('/distribution/subscriptions/count'),
  // 获取分发订阅详情
  getById: (id) => request.get(`/distribution/subscriptions/${id}`),
  // 创建分发订阅
  create: (data) => request.post('/distribution/subscriptions', data),
  // 更新分发订阅
  update: (id, data) => request.put(`/distribution/subscriptions/${id}`, data),
  // 删除分发订阅
  delete: (id) => request.delete(`/distribution/subscriptions/${id}`),
  // 获取订阅链接
  getSubscriptionUrl: (id) => request.get(`/distribution/subscriptions/${id}/subscription-url`),
}
