/**********************************
 * @Author: Ronnie Zhang
 * @LastEditor: Ronnie Zhang
 * @LastEditTime: 2023/12/13 20:54:36
 * @Email: zclzone@outlook.com
 * Copyright © 2023 Ronnie Zhang(大脸怪) | https://isme.top
 **********************************/

export const defaultLayout = 'normal'

export const defaultPrimaryColor = '#316C72'

// 控制 LayoutSetting 组件是否可见
export const layoutSettingVisible = true

export const naiveThemeOverrides = {
  common: {
    primaryColor: '#316C72FF',
    primaryColorHover: '#316C72E3',
    primaryColorPressed: '#2B4C59FF',
    primaryColorSuppl: '#316C72E3',
  },
}

export const basePermissions = [
  {
    code: 'SourceManage',
    name: '源管理',
    type: 'MENU',
    icon: 'i-fe:list',
    order: -1,
    enable: true,
    show: true,
    children: [
      {
        code: 'SubscribeConfig',
        name: '订阅配置',
        type: 'MENU',
        path: '/source/subscribe',
        component: '/src/views/source/subscribe/index.vue',
        icon: 'i-fe:settings',
        order: 1,
        enable: true,
        show: true,
      },
      {
        code: 'TaskHistory',
        name: '任务历史',
        type: 'MENU',
        path: '/source/history',
        component: '/src/views/source/history/index.vue',
        icon: 'i-fe:clock',
        order: 2,
        enable: true,
        show: true,
      },
    ],
  },
  {
    code: 'ChannelManage',
    name: '频道管理',
    type: 'MENU',
    icon: 'i-fe:grid',
    order: 0,
    enable: true,
    show: true,
    children: [
      {
        code: 'ChannelList',
        name: '频道列表',
        type: 'MENU',
        path: '/channel-management/list',
        component: '/src/views/channel-management/list/index.vue',
        icon: 'i-fe:list',
        order: 1,
        enable: true,
        show: true,
      },
      {
        code: 'ChannelGroups',
        name: '频道组管理',
        type: 'MENU',
        path: '/channel-management/groups',
        component: '/src/views/channel-management/groups/index.vue',
        icon: 'i-fe:folder',
        order: 2,
        enable: true,
        show: true,
      },
      {
        code: 'ChannelRules',
        name: '频道处理规则',
        type: 'MENU',
        path: '/channel-management/rules',
        component: '/src/views/channel-management/rules/index.vue',
        icon: 'i-fe:sliders',
        order: 3,
        enable: true,
        show: true,
      },
    ],
  },
  {
    code: 'ExternalLink',
    name: '外链(可内嵌打开)',
    type: 'MENU',
    icon: 'i-fe:external-link',
    order: 98,
    enable: true,
    show: true,
    children: [
      {
        code: 'ShowDocs',
        name: '项目文档',
        type: 'MENU',
        path: 'https://isme.top',
        icon: 'i-me:docs',
        order: 1,
        enable: true,
        show: true,
      },
      {
        code: 'ApiFoxDocs',
        name: '接口文档',
        type: 'MENU',
        path: 'https://apifox.com/apidoc/shared-ff4a4d32-c0d1-4caf-b0ee-6abc130f734a',
        icon: 'i-me:apifox',
        order: 2,
        enable: true,
        show: true,
      },
      {
        code: 'NaiveUI',
        name: 'Naive UI',
        type: 'MENU',
        path: 'https://www.naiveui.com/zh-CN/os-theme',
        icon: 'i-me:naiveui',
        order: 3,
        enable: true,
        show: true,
      },
      {
        code: 'MyBlog',
        name: '博客-掘金',
        type: 'MENU',
        path: 'https://juejin.cn/user/1961184475483255/posts',
        icon: 'i-simple-icons:juejin',
        order: 4,
        enable: true,
        show: true,
      },
    ],
  },
]
