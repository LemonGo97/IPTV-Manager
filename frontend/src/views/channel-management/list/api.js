import { request } from '@/utils'

export default {
  // 获取统计信息
  getStatistic: () => request.get('/channel/statistic'),

  // 获取频道 EPG 时间轴
  getTimeline: (id) => request.get(`/channel/${id}/timeline`),
}
