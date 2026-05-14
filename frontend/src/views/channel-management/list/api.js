import { request } from '@/utils'

export default {
  // 获取统计信息
  getStatistic: () => request.get('/channel/statistic'),

  // 获取频道列表（分页、搜索、排序）
  getList: (params) => request.get('/channel', { params }),

  // 获取频道 EPG 时间轴
  getTimeline: (id) => request.get(`/channel/${id}/timeline`),

  // 数据清洗
  dataClean: () => request.post(`/channel/clean`),
}
