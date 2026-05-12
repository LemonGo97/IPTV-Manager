/**********************************
 * @Author: Ronnie Zhang
 * @LastEditor: Ronnie Zhang
 * @LastEditTime: 2023/12/05 21:29:27
 * @Email: zclzone@outlook.com
 * Copyright © 2023 Ronnie Zhang(大脸怪) | https://isme.top
 **********************************/

import { request, mockRequest } from '@/utils'

export default {
  create: data => mockRequest.post('/role', data),
  read: (params = {}) => mockRequest.get('/role/page', { params }),
  update: data => mockRequest.patch(`/role/${data.id}`, data),
  delete: id => mockRequest.delete(`/role/${id}`),

  getAllPermissionTree: () => mockRequest.get('/permission/tree'),
  getAllUsers: (params = {}) => mockRequest.get('/user', { params }),
  addRoleUsers: (roleId, data) => mockRequest.patch(`/role/users/add/${roleId}`, data),
  removeRoleUsers: (roleId, data) => mockRequest.patch(`/role/users/remove/${roleId}`, data),
}
