import { request } from '@/utils'

export default {
  // 获取订阅源列表
  getAll: (params) => request.get('/api/m3u8/provider', { params }),
  // 获取单个订阅源
  getById: (id) => request.get(`/api/m3u8/provider/${id}`),
  // 创建订阅源
  create: (data) => request.post('/api/m3u8/provider', data),
  // 更新订阅源
  update: (id, data) => request.put(`/api/m3u8/provider/${id}`, data),
  // 删除订阅源
  delete: (id) => request.delete(`/api/m3u8/provider/${id}`),
  // 手动刷新订阅源
  refresh: (id) => request.post(`/api/m3u8/provider/${id}/refresh`),
  // 上传本地文件
  upload: (file) => {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/api/m3u8/provider/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
}
