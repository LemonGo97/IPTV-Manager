/**********************************
 * @Author: Ronnie Zhang
 * @LastEditor: Ronnie Zhang
 * @LastEditTime: 2023/12/05 21:28:30
 * @Email: zclzone@outlook.com
 * Copyright © 2023 Ronnie Zhang(大脸怪) | https://isme.top
 **********************************/

import { request, mockRequest } from '@/utils'

export default {
  toggleRole: data => mockRequest.post('/auth/role/toggle', data),
  login: data => mockRequest.post('/auth/login', data, { needToken: false }),
  getUser: () => mockRequest.get('/user/detail'),
}
