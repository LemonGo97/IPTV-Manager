import { request } from '@/utils'

export default {
  // 获取所有EPG源
  getAll: (params) => request.get('/epg/source', { params }),
  // 获取单个EPG源
  getById: (id) => request.get(`/epg/source/${id}`),
  // 创建EPG源
  create: (data) => request.post('/epg/source', data),
  // 更新EPG源
  update: (id, data) => request.put(`/epg/source/${id}`, data),
  // 删除EPG源
  delete: (id) => request.delete(`/epg/source/${id}`),
  // 刷新EPG源
  refresh: (id) => request.post(`/epg/source/${id}/refresh`),
}
