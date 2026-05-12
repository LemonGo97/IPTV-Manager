/**********************************
 * @Author: Ronnie Zhang
 * @LastEditor: Ronnie Zhang
 * @LastEditTime: 2023/12/04 22:50:38
 * @Email: zclzone@outlook.com
 * Copyright © 2023 Ronnie Zhang(大脸怪) | https://isme.top
 **********************************/

import { request, mockRequest } from '@/utils'

export default {
  // 获取用户信息
  getUser: () => mockRequest.get('/user/detail'),
  // 刷新token
  refreshToken: () => mockRequest.get('/auth/refresh/token'),
  // 登出
  logout: () => mockRequest.post('/auth/logout', {}, { needTip: false }),
  // 切换当前角色
  switchCurrentRole: role => mockRequest.post(`/auth/current-role/switch/${role}`),
  // 获取角色权限
  getRolePermissions: () => mockRequest.get('/role/permissions/tree'),
  // 验证菜单路径
  validateMenuPath: path => mockRequest.get(`/permission/menu/validate?path=${path}`),
}
