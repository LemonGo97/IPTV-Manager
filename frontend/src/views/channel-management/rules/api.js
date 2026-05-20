import { request } from '@/utils'

export default {
  // 获取支持的处理引擎
  listEngine: () => request.get('/channel/cleanup/engines'),
}
