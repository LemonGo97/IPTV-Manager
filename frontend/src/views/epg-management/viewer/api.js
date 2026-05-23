import { request } from '@/utils'

export default {
  // 获取频道列表（用于树形结构的第一层）
  getChannels: (sourceId) => request.get('/epg/program/channels', { params: { sourceId } }),
  // 获取指定频道的节目列表
  getPrograms: (sourceId, channelName) =>
    request.get('/epg/program/list', { params: { sourceId, channelName } }),
}
