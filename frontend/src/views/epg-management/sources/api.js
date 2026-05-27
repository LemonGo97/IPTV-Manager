import { request } from '@/utils'

export default {
  // 获取所有EPG源
  getAll: (params) => request.get('/epg/provider', { params }),
  // 获取单个EPG源
  getById: (id) => request.get(`/epg/provider/${id}`),
  // 创建EPG源
  create: (data) => request.post('/epg/provider', data),
  // 更新EPG源
  update: (id, data) => request.put(`/epg/provider/${id}`, data),
  // 删除EPG源
  delete: (id) => request.delete(`/epg/provider/${id}`),
  // 刷新EPG源
  refresh: (id) => request.post(`/epg/provider/${id}/refresh`),
}
