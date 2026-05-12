import { request } from '@/utils'

export default {
  // 获取所有频道组
  getAll: (params) => request.get('/channel/group', { params }),
  // 获取单个频道组
  getById: (id) => request.get(`/channel/group/${id}`),
  // 创建频道组
  create: (data) => request.post('/channel/group', data),
  // 更新频道组
  update: (id, data) => request.put(`/channel/group/${id}`, data),
  // 删除频道组
  delete: (id) => request.delete(`/channel/group/${id}`),
}
