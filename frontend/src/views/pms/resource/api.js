/**********************************
 * @Author: Ronnie Zhang
 * @LastEditor: Ronnie Zhang
 * @LastEditTime: 2024/04/01 15:52:04
 * @Email: zclzone@outlook.com
 * Copyright © 2023 Ronnie Zhang(大脸怪) | https://isme.top
 **********************************/

import axios from 'axios'
import { request, mockRequest } from '@/utils'

export default {
  getMenuTree: () => mockRequest.get('/permission/menu/tree'),
  getButtons: ({ parentId }) => mockRequest.get(`/permission/button/${parentId}`),
  getComponents: () => axios.get(`${import.meta.env.VITE_PUBLIC_PATH}components.json`),
  addPermission: data => mockRequest.post('/permission', data),
  savePermission: (id, data) => mockRequest.patch(`/permission/${id}`, data),
  deletePermission: id => mockRequest.delete(`permission/${id}`),
}
