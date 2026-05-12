/**********************************
 * @Author: Ronnie Zhang
 * @LastEditor: Ronnie Zhang
 * @LastEditTime: 2023/12/05 21:29:51
 * @Email: zclzone@outlook.com
 * Copyright © 2023 Ronnie Zhang(大脸怪) | https://isme.top
 **********************************/

import { request, mockRequest } from '@/utils'

export default {
  create: data => mockRequest.post('/user', data),
  read: (params = {}) => mockRequest.get('/user', { params }),
  update: data => mockRequest.patch(`/user/${data.id}`, data),
  delete: id => mockRequest.delete(`/user/${id}`),
  resetPwd: (id, data) => mockRequest.patch(`/user/password/reset/${id}`, data),

  getAllRoles: () => mockRequest.get('/role?enable=1'),
}
