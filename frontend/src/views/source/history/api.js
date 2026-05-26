import { request } from '@/utils'

export default {
  // 获取任务历史列表
  getAll: (params) => request.get('/iptv/task/history', { params }),
  // 获取任务详情
  getById: (id) => request.get(`/iptv/task/history/${id}`),
  // 获取任务解析的频道列表
  getChannels: (id) => request.get(`/iptv/task/history/${id}/channels`),
}
