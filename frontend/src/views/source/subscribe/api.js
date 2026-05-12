import { request } from '@/utils'

export default {
  // 获取订阅源列表
  getAll: (params) => request.get('/m3u8/provider', { params }),
  // 获取单个订阅源
  getById: (id) => request.get(`/m3u8/provider/${id}`),
  // 创建订阅源
  create: (data) => request.post('/m3u8/provider', data),
  // 更新订阅源
  update: (id, data) => request.put(`/m3u8/provider/${id}`, data),
  // 删除订阅源
  delete: (id) => request.delete(`/m3u8/provider/${id}`),
  // 手动刷新订阅源
  refresh: (id) => request.post(`/m3u8/provider/${id}/refresh`),
  // 上传本地文件
  upload: (file, name, description) => {
    const formData = new FormData()
    formData.append('file', file)
    if (name) formData.append('name', name)
    if (description) formData.append('description', description)
    return request.post('/m3u8/provider/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
}
