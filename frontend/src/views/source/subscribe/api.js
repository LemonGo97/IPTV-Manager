import { request } from '@/utils'

export default {
  // 获取订阅源列表
  getAll: (params) => request.get('/iptv/provider', { params }),
  // 获取单个订阅源
  getById: (id) => request.get(`/iptv/provider/${id}`),
  // 创建订阅源
  create: (data) => request.post('/iptv/provider', data, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  }),
  // 更新订阅源
  update: (id, data) => request.put(`/iptv/provider/${id}`, data, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  }),
  // 删除订阅源
  delete: (id) => request.delete(`/iptv/provider/${id}`),
  // 手动刷新订阅源
  refresh: (id) => request.post(`/iptv/provider/${id}/refresh`),
}
