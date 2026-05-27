import { request } from '@/utils'

export default {
  // 获取订阅用户列表
  getAll: (params) => request.get('/distribution/users', { params }),
  // 获取订阅用户详情
  getById: (id) => request.get(`/distribution/users/${id}`),
  // 创建订阅用户
  create: (data) => request.post('/distribution/users', data),
  // 更新订阅用户
  update: (id, data) => request.put(`/distribution/users/${id}`, data),
  // 删除订阅用户
  delete: (id) => request.delete(`/distribution/users/${id}`),
  // 重置访问密钥
  resetAccessKey: (id) => request.post(`/distribution/users/${id}/reset-key`),
  // 复制订阅链接
  copySubscriptionUrl: (id) => request.get(`/distribution/users/${id}/subscription-url`),
}
