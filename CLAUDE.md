# IPTV-Manager

## 项目概述

IPTV 管理系统 - 基于 Spring Boot 的 M3U8 播放列表管理服务，支持 IPTV 频道解析、管理和播放。

## 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 语言 | Java | 21 |
| 框架 | Spring Boot | 4.0.5 (Milestone) |
| 数据库 ORM | MyBatis | 4.0.1 |
| 数据库 | SQLite | 3.51.3.0 |
| 构建工具 | Gradle | 9.4.1 |
| M3U8 解析 | m3u8-parser | 0.30 |
| JSON 处理 | Jackson | 3.1.2 |
| 代码生成 | Lombok | 1.18.42 |
| 定时任务 | Quartz | 2.5.0 |
| 测试框架 | JUnit + Spring Boot Test | 6.1.0-M1 |

## 项目结构

```
IPTV-Manager/
├── backend/                      # 后端模块
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/            # Java 源码
│   │   │   │   └── com/lemongo97/iptv/iptvmanager/
│   │   │   └── resources/       # 配置文件
│   │   │       ├── application.yml
│   │   │       ├── db/           # 数据库迁移
│   │   │       └── mapper/       # MyBatis XML
│   │   └── test/                # 测试代码
│   ├── doc/                      # API 文档
│   └── build.gradle
├── frontend/                     # 前端模块
│   ├── src/
│   │   ├── api/                 # API 接口定义
│   │   ├── assets/              # 静态资源
│   │   ├── components/          # 组件
│   │   │   ├── common/          # 通用组件
│   │   │   └── me/             # 业务组件（CRUD、Modal等）
│   │   ├── composables/         # 组合式函数
│   │   ├── layouts/             # 布局组件
│   │   │   ├── full/           # 完整布局（带侧边栏）
│   │   │   ├── normal/         # 标准布局
│   │   │   ├── simple/         # 简洁布局
│   │   │   └── empty/          # 空白布局
│   │   ├── router/             # 路由配置
│   │   ├── store/              # Pinia 状态管理
│   │   │   └── modules/        # Store 模块
│   │   ├── styles/             # 全局样式
│   │   ├── utils/              # 工具函数
│   │   │   ├── http/           # HTTP 请求封装
│   │   │   └── storage/        # 本地存储
│   │   ├── views/              # 页面视图
│   │   │   ├── login/          # 登录页
│   │   │   ├── home/           # 首页
│   │   │   ├── pms/            # 权限管理
│   │   │   └── error-page/     # 错误页
│   │   ├── App.vue             # 根组件
│   │   ├── main.js             # 入口文件
│   │   └── settings.js         # 应用配置
│   ├── build/                  # 构建插件
│   ├── public/                 # 公共资源
│   ├── build.gradle            # Gradle 构建配置
│   ├── package.json            # npm 依赖
│   ├── vite.config.js          # Vite 配置
│   └── uno.config.js           # UnoCSS 配置
├── gradle/
│   ├── libs.versions.toml       # 版本目录（依赖版本管理）
│   └── repositories.gradle
├── build.gradle                  # 根构建文件
└── settings.gradle               # 项目设置
```

## 核心功能

### 1. M3U8 源管理

- 在线订阅源（通过 URL）
- 本地 M3U8 文件
- 源刷新和自动解析

### 2. M3U8 原始数据历史

- **自动备份**：每次解析前自动保存原始 M3U8 内容
- **保留策略**：每个提供者保留最近 3 次的历史记录
- **数据用途**：
  - 解析失败时的备用数据
  - 调试和问题排查
  - 历史对比分析

### 3. 逻辑删除

所有表都使用逻辑删除（软删除）：
- 删除操作只标记 `deleted = 1`
- 数据永久保留，可随时恢复
- 支持审计追踪

### 4. 定时任务调度（Quartz）

- **自动刷新**：M3U8 提供者可配置定时自动刷新
- **Quartz 集成**：使用 Quartz 2.5.0 实现任务调度
- **灵活配置**：通过 `refreshRate` 字段配置刷新间隔（秒）
- **动态管理**：创建/更新/删除提供者时自动管理定时任务
- **任务持久化**：使用内存存储（RAMJobStore），重启后需要重新调度

## API 端点

### M3U8 提供者管理

| 方法 | 端点 | 描述 |
|------|------|------|
| GET | `/api/m3u8/provider` | 获取所有 M3U8 源 |
| GET | `/api/m3u8/provider/{id}` | 获取指定 M3U8 源 |
| POST | `/api/m3u8/provider` | 创建 M3U8 源 |
| PUT | `/api/m3u8/provider/{id}` | 更新 M3U8 源 |
| DELETE | `/api/m3u8/provider/{id}` | 删除 M3U8 源（逻辑删除） |
| POST | `/api/m3u8/provider/{id}/refresh` | 手动刷新 M3U8 源 |

### 频道管理

| 方法 | 端点 | 描述 |
|------|------|------|
| GET | `/api/channel` | 获取所有频道 |
| GET | `/api/channel/{id}` | 获取指定频道 |
| GET | `/api/channel/group/{group}` | 根据分组获取频道 |
| POST | `/api/channel` | 创建频道 |
| PUT | `/api/channel/{id}` | 更新频道 |
| DELETE | `/api/channel/{id}` | 删除频道（逻辑删除） |

### 频道组管理

| 方法 | 端点 | 描述 |
|------|------|------|
| GET | `/api/channel/group` | 获取所有频道组 |
| GET | `/api/channel/group/{id}` | 获取指定频道组 |
| POST | `/api/channel/group` | 创建频道组 |
| PUT | `/api/channel/group/{id}` | 更新频道组 |
| DELETE | `/api/channel/group/{id}` | 删除频道组（逻辑删除） |

### EPG 源管理

| 方法 | 端点 | 描述 |
|------|------|------|
| GET | `/api/epg/source` | 获取所有 EPG 源 |
| GET | `/api/epg/source/{id}` | 获取指定 EPG 源 |
| POST | `/api/epg/source` | 创建 EPG 源 |
| PUT | `/api/epg/source/{id}` | 更新 EPG 源 |
| DELETE | `/api/epg/source/{id}` | 删除 EPG 源（逻辑删除） |
| POST | `/api/epg/source/{id}/refresh` | 手动刷新 EPG 源 |

### 请求示例

创建 M3U8 提供者（带定时刷新）：
```json
{
  "name": "示例源",
  "type": "online",
  "url": "https://example.com/playlist.m3u8",
  "refreshRate": 3600,
  "enabled": true
}
```

## 开发指南

### 构建项目

```bash
./gradlew build
```

### 运行后端

```bash
./gradlew :backend:bootRun
```

### 运行前端

```bash
# 通过 Gradle（推荐）
./gradlew :frontend:dev

# 直接使用 pnpm
cd frontend && pnpm dev
```

### 构建前端

```bash
# 通过 Gradle（推荐）
./gradlew :frontend:build

# 直接使用 pnpm
cd frontend && pnpm build
```

### 运行测试

```bash
./gradlew :backend:test
```

### 清理构建

```bash
./gradlew clean
```

## 配置说明

### 依赖版本管理

项目使用 Gradle 版本目录（Version Catalog），所有依赖版本在 `gradle/libs.versions.toml` 中集中管理。

### 依赖版本说明

**注意**：项目使用了一些里程碑版本的依赖：

- **Spring Boot 4.0.5**：里程碑版本，需要 Spring milestone 仓库
- **Jackson 3.1.2**：包名已从 `com.fasterxml.jackson` 改为 `tools.jackson`
- **m3u8-parser 0.30**：API 有重大变化，注意查阅最新文档

### 日志配置

项目排除 Logback，使用 Log4j2 作为日志实现。

### 数据库

使用 SQLite 作为嵌入式数据库，无需额外安装数据库服务。

## 代码规范

### Lombok 使用

项目使用 Lombok 简化代码：

- **`@Slf4j`**：自动生成 SLF4J Logger，替代手动声明
- **`@RequiredArgsConstructor`**：自动生成构造器，用于依赖注入

示例：
```java
@Slf4j
@Service
@RequiredArgsConstructor
public class MyService {
    private final MyRepository repository;
    // 自动生成构造器：public MyService(MyRepository repository) {...}
    
    public void doSomething() {
        log.info("Using @Slf4j annotation"); // 直接使用 log
    }
}
```

### 实体类设计

- 使用 `record` 定义不可变实体
- 所有实体包含通用字段：`id`, `createdAt`, `updatedAt`, `deleted`
- `deleted` 字段：`Boolean` 类型，`false` 表示正常，`true` 表示已删除

示例：
```java
public record MyEntity(
    Long id,
    String name,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Boolean deleted
) {}
```

### 服务层设计

- 使用构造器注入（通过 `@RequiredArgsConstructor`）
- 所有更新操作保留 `deleted` 状态
- 删除操作执行逻辑删除

示例：
```java
public Entity create(Entity entity) {
    var now = LocalDateTime.now();
    var newEntity = new Entity(
        null,
        entity.name(),
        now,
        now,
        false  // 新建数据默认未删除
    );
    mapper.insert(newEntity);
    return newEntity;
}

public Entity update(Long id, Entity entity) {
    var existing = findById(id);
    var updated = new Entity(
        id,
        entity.name() != null ? entity.name() : existing.name(),
        existing.createdAt(),
        LocalDateTime.now(),
        existing.deleted()  // 保留删除状态
    );
    mapper.update(updated);
    return updated;
}
```

### MyBatis Mapper 设计

- **SELECT 语句**：必须包含 `WHERE deleted = 0` 过滤条件
- **INSERT 语句**：`deleted` 字段默认值为 `0`
- **UPDATE 语句**：保留 `WHERE deleted = 0` 条件，防止更新已删除数据
- **DELETE 语句**：改为 `UPDATE SET deleted = 1`

XML 示例：
```xml
<select id="findAll" resultMap="ResultMap">
    SELECT id, name, created_at, updated_at, deleted
    FROM my_table
    WHERE deleted = 0
    ORDER BY id DESC
</select>

<update id="deleteById">
    UPDATE my_table
    SET deleted = 1
    WHERE id = #{id}
</update>
```

## API 版本兼容性说明

### m3u8-parser 0.30 API 变化

注意：m3u8-parser 0.30 版本有重大 API 变化：

| 旧 API (0.29 之前) | 新 API (0.30) |
|-------------------|---------------|
| `MasterPlaylist` | `MultivariantPlaylist` |
| `M3U8Parser` | `MultivariantPlaylistParser` |
| `mediaPlaylists()` | `variants()` |
| `JsonProcessingException` | `JacksonException` |

### Jackson 3.x 包名变化

Jackson 3.x 将包名从 `com.fasterxml.jackson` 改为 `tools.jackson`：

| 旧包名 | 新包名 |
|--------|--------|
| `com.fasterxml.jackson.core` | `tools.jackson.core` |
| `com.fasterxml.jackson.databind` | `tools.jackson.databind` |
| `com.fasterxml.jackson.dataformat` | `tools.jackson.dataformat` |

## 前端模块

### 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 框架 | Vue | 3.5.29 |
| 构建工具 | Vite | 7.3.1 |
| 状态管理 | Pinia | 3.0.4 |
| UI 组件 | Naive UI | 2.43.2 |
| 原子 CSS | UnoCSS | 66.6.2 |
| 路由 | Vue Router | 5.0.3 |
| HTTP 客户端 | Axios | 1.13.5 |
| 工具库 | @vueuse/core | 14.2.1 |
| 图表 | ECharts | 6.0.0 |
| 日期处理 | Day.js | 1.11.19 |
| 表格处理 | XLSX | 0.18.5 |
| Node.js | - | 24.15.0 |
| 包管理器 | pnpm | 10.33.2 |

### 项目模板

基于 [Vue Naive Admin](https://github.com/zclzone/vue-naive-admin) 模板：
- 极简风格的后台管理模板
- 扁平化路由设计，支持动态权限
- 完整的权限管理系统（RBAC）
- 封装常用业务组件（CRUD、Modal、Page）
- 支持多主题和国际化

### 开发环境配置

| 配置项 | 开发环境 | 生产环境 |
|--------|---------|---------|
| 路由模式 | History | Hash |
| 代理目标 | `http://localhost:8080` | - |
| API Mock | Apifox 云端 Mock | Apifox 云端 Mock |
| 开发服务器 | `0.0.0.0:3200` | - |

### 目录结构

```
frontend/src/
├── api/                  # API 接口定义
│   └── index.js         # 通用 API（用户、权限、认证）
├── assets/              # 静态资源
│   ├── icons/           # 图标资源
│   │   ├── isme/        # 自定义图标
│   │   └── feather/     # Feather 图标集
│   └── images/          # 图片资源
├── components/          # 组件
│   ├── common/          # 通用组件（Footer、Logo、Theme等）
│   └── me/              # 业务组件
│       ├── crud/        # CRUD 表格组件
│       └── modal/       # 弹窗组件
├── composables/         # 组合式函数
│   ├── useAliveData.js  # KeepAlive 状态管理
│   ├── useCrud.js       # CRUD 操作封装
│   ├── useForm.js       # 表单处理
│   └── useModal.js      # 弹窗管理
├── layouts/             # 布局组件
│   ├── full/           # 完整布局（侧边栏+头部+标签页）
│   ├── normal/         # 标准布局（侧边栏+头部）
│   ├── simple/         # 简洁布局（仅侧边栏）
│   └── empty/          # 空白布局
├── router/             # 路由配置
│   ├── basic-routes.js # 基础路由
│   ├── guards/         # 路由守卫
│   └── index.js        # 路由实例
├── store/              # Pinia 状态管理
│   └── modules/        # Store 模块
│       ├── app.js       # 应用状态
│       ├── auth.js      # 认证状态
│       ├── permission.js # 权限状态
│       ├── router.js    # 路由状态
│       ├── tab.js       # 标签页状态
│       └── user.js      # 用户状态
├── styles/             # 全局样式
│   ├── reset.css       # 样式重置
│   └── global.css      # 全局样式
├── utils/              # 工具函数
│   ├── http/           # HTTP 请求封装
│   │   ├── index.js    # Axios 实例
│   │   └── helpers.js  # 请求辅助函数
│   ├── storage/        # 本地存储封装
│   ├── naiveTools.js   # Naive UI 工具
│   └── common.js       # 通用工具
├── views/              # 页面视图（API 与页面同目录）
│   ├── login/          # 登录页
│   │   └── index.vue
│   ├── home/           # 首页
│   │   └── index.vue
│   ├── pms/            # 权限管理系统
│   │   ├── user/       # 用户管理
│   │   │   ├── index.vue
│   │   │   └── api.js
│   │   ├── role/       # 角色管理
│   │   │   ├── index.vue
│   │   │   └── api.js
│   │   └── resource/   # 资源/菜单管理
│   │       ├── index.vue
│   │       └── api.js
│   ├── error-page/     # 错误页（403、404）
│   └── profile/        # 个人中心
├── App.vue             # 根组件
├── main.js             # 应用入口
└── settings.js         # 应用配置
```

### 开发命令

```bash
# 进入前端目录
cd frontend

# 安装依赖
pnpm install

# 启动开发服务器（通过 Gradle）
./gradlew :frontend:dev

# 启动开发服务器（直接使用 pnpm）
pnpm dev

# 构建生产版本
pnpm build

# 预览生产构建
pnpm preview

# 代码检查
pnpm lint:fix
```

### 配置说明

#### Vite 配置 (`vite.config.js`)

- **端口**：3200
- **代理**：`/api` → 后端 `http://localhost:8080`
- **别名**：`@` → `src`，`~` → 项目根目录
- **插件**：
  - Vue DevTools
  - UnoCSS
  - 自动导入 Vue API
  - 自动导入 Naive UI 组件
  - 自定义页面路径和图标生成插件

#### UnoCSS 配置 (`uno.config.js`)

- **预设**：Wind3、Attributify、Icons、RemToPx
- **图标**：支持 `i-` 前缀，内置 `isme` 和 `feather` 图标集
- **快捷类**：预定义常用样式组合
- **主题色**：`#316C72`（可通过 CSS 变量自定义）

#### 环境变量

| 变量 | 说明 |
|------|------|
| `VITE_USE_HASH` | 是否使用 Hash 路由模式 |
| `VITE_PUBLIC_PATH` | 资源公共路径 |
| `VITE_AXIOS_BASE_URL` | Axios 基础路径 |
| `VITE_PROXY_TARGET` | 代理目标地址 |

### 权限系统

前端实现完整的 RBAC 权限系统：

- **基于角色动态生成路由**：无需手动配置路由
- **权限验证**：路由守卫自动检查权限
- **403/404 区分**：无权限跳 403，路由不存在跳 404
- **菜单资源管理**：后端控制菜单和按钮权限
- **基础权限**：`basePermissions` 配置无需权限控制的菜单

### 状态管理

使用 Pinia 进行状态管理，支持状态持久化：

- **app**：应用全局状态
- **auth**：认证和 token 管理
- **permission**：权限和菜单数据
- **router**：动态路由管理
- **tab**：多标签页管理
- **user**：当前用户信息

### 业务组件封装

#### CRUD 组件 (`components/me/crud`)

- **QueryItem**：查询条件项组件
- **自动分页**：内置分页逻辑
- **自动请求**：挂载时自动请求数据
- **批量操作**：支持批量删除等操作

#### Modal 组件 (`components/me/modal`)

- **表单模态框**：内置表单验证
- **确认模态框**：二次确认操作
- **工具方法**：简化的弹窗调用

### API 对接

当前使用 Apifox 云端 Mock 进行开发：

```javascript
// 开发环境 API 地址
VITE_AXIOS_BASE_URL = 'https://m1.apifoxmock.com/m1/3776410-3408296-default'
```

对接后端时，修改 `.env.development`：

```bash
# 使用代理连接本地后端
VITE_AXIOS_BASE_URL = '/api'
VITE_PROXY_TARGET = 'http://localhost:8080'
```

### 样式规范

- **UnoCSS 原子化 CSS**：优先使用原子类
- **主题定制**：通过 CSS 变量 `--primary-color` 自定义主题色
- **响应式设计**：使用 `rem-to-px` 预设，自动转换 rem 单位
- **深色模式**：内置 Naive UI 深色主题支持

## 前后端集成

> **注意**：暂时先不用管项目的用户登录与权限相关的功能，专注于核心业务功能开发。

### 调试指南

> **重要**：使用 Chrome DevTools MCP 调试时，如遇登录页面，请点击页面上的"一键登录"按钮进入系统，**不要修改** [`frontend/src/router/guards/permission-guard.js`](frontend/src/router/guards/permission-guard.js) 文件中的 `WHITE_LIST` 配置。

**原因**：
- 修改 `WHITE_LIST` 会破坏权限系统的完整性
- 使用"一键登录"是正确的调试方式
- 保持权限守卫的原始配置便于后续集成真实认证系统

#### Hash 路由模式

本项目使用 Hash 路由模式（`VITE_USE_HASH = 'true'`），调试时需使用正确的 URL 格式：

| 错误格式 | 正确格式 |
|---------|---------|
| `http://localhost:3200/source/subscribe` | `http://localhost:3200/#/source/subscribe` |
| `http://localhost:3200/source/subscribe#/source/subscribe` | `http://localhost:3200/#/source/subscribe` |

**规则**：所有路由路径前必须加上 `/#` 前缀。

#### 常见问题修复

**1. 编辑按钮数据不回显**

**现象**：点击编辑按钮后弹窗打开，但表单字段为空。

**原因**：后端返回的字段名与表单字段名不匹配，或 `useCrud` 的 `handleOpen` 函数接收参数格式不正确。

**修复示例**：
```javascript
// 后端返回 refreshRate（number），但表单使用 autoRefresh（boolean）
const { handleOpen: _handleOpen } = useCrud({ ... })

// 包装 handleOpen 以转换数据格式
function handleOpen(row) {
  const rowData = row ?? {}
  _handleOpen({
    action: 'edit',
    row: {
      ...rowData,
      autoRefresh: !!rowData.refreshRate,  // 转换字段
    },
  })
}
```

**2. 删除按钮报错**

**现象**：点击删除按钮后报错或确认对话框不显示。

**原因**：删除按钮传递了整个 `row` 对象，但 `handleDelete` 函数期望的是 `id`。

**修复示例**：
```javascript
// 错误写法
onClick: () => handleDelete(row)

// 正确写法
onClick: () => handleDelete(row.id)
```

**3. 编辑保存报错 - URL 包含 [object Object]**

**现象**：编辑后点击保存，请求 URL 显示 `/api/xxx/[object%20Object]`，返回 400 错误。

**原因**：API 的 `update` 函数需要两个参数 `(id, data)`，但 `useCrud` 的 `doUpdate` 只传递一个参数 `modalForm.value`。

**修复示例**：
```javascript
// 错误写法
doUpdate: api.update,

// 正确写法 - 包装函数提取 id
doUpdate: (data) => api.update(data.id, data),
```

### 开发环境配置

1. **启动后端**：`./gradlew :backend:bootRun` → `http://localhost:8080`
2. **启动前端**：`./gradlew :frontend:dev` → `http://localhost:3200`
3. **代理配置**：前端通过 Vite 代理将 `/api` 请求转发到后端

### API 文档

完整的 OpenAPI 3.0 格式 API 文档位于：
- `backend/doc/openapi.yaml`

### Mock 数据说明

当前使用 Apifox 云端 Mock。对接真实后端时，修改 `frontend/.env.development`：

```bash
# 使用代理连接本地后端
VITE_AXIOS_BASE_URL = '/api'
VITE_PROXY_TARGET = 'http://localhost:8080'
```

### 前端对接指南

> **重要**：暂时先不用管项目的用户登录与权限相关的功能，专注于核心业务功能开发。

#### 文件组织规范

**API 与页面同目录**：每个功能模块的 API 接口文件与页面组件放在同一目录下，便于维护：

```
frontend/src/views/
├── m3u8-source/              # M3U8 源管理
│   ├── index.vue             # 页面组件
│   └── api.js                # API 接口
├── channel/                  # 频道管理
│   ├── index.vue
│   └── api.js
└── channel-group/            # 频道组管理
    ├── index.vue
    └── api.js
```

#### 新增功能模块步骤

**1. 创建 API 文件** `frontend/src/views/功能目录/api.js`：
```javascript
import { request } from '@/utils'

export default {
  // 获取列表（带分页和查询参数）
  getAll: (params) => request.get('/api/xxx', { params }),
  // 获取单个详情
  getById: (id) => request.get(`/api/xxx/${id}`),
  // 创建
  create: (data) => request.post('/api/xxx', data),
  // 更新
  update: (id, data) => request.put(`/api/xxx/${id}`, data),
  // 删除
  delete: (id) => request.delete(`/api/xxx/${id}`),
}
```

**2. 创建页面组件** `frontend/src/views/功能目录/index.vue`：

页面组件必须正确导入所需的组件和工具：

```vue
<template>
  <CommonPage>
    <template #action>
      <NButton type="primary" @click="handleAdd()">
        <i class="i-material-symbols:add mr-4 text-18" />
        添加
      </NButton>
    </template>

    <MeCrud
      ref="$table"
      v-model:query-items="queryItems"
      :columns="columns"
      :get-data="api.getAll"
    >
      <!-- 查询条件 -->
      <MeQueryItem label="名称" :label-width="50">
        <n-input v-model:value="queryItems.name" placeholder="请输入" clearable />
      </MeQueryItem>
    </MeCrud>

    <!-- 表单弹窗 -->
    <MeModal ref="modalRef" width="600px">
      <n-form ref="modalFormRef" :model="modalForm" label-width="100">
        <!-- 表单内容 -->
      </n-form>
    </MeModal>
  </CommonPage>
</template>

<script setup>
// 导入 Naive UI 组件
import { NButton, NTag } from 'naive-ui'
// 导入业务组件
import { MeCrud, MeModal, MeQueryItem } from '@/components'
// 导入 composable
import { useCrud } from '@/composables'
// 导入 API
import api from './api'

const $table = ref(null)
const queryItems = reactive({
  name: '',
})

const columns = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '名称', key: 'name', ellipsis: { tooltip: true } },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    fixed: 'right',
    render: row =>
      h('div', { class: 'flex gap-8' }, [
        h(NButton, { size: 'small', onClick: () => handleOpen(row) }, { default: () => '编辑' }),
        h(NButton, { size: 'small', type: 'error', onClick: () => handleDelete(row) }, { default: () => '删除' }),
      ]),
  },
]

// 使用 useCrud 管理表单和弹窗
const {
  modalRef,
  modalFormRef,
  modalForm,
  modalAction,
  handleAdd,
  handleDelete,
  handleOpen,
  handleSave,
} = useCrud({
  name: '实体名称',
  initForm: { /* 初始表单值 */ },
  doCreate: api.create,
  doDelete: api.delete,
  doUpdate: api.update,
  refresh: () => $table.value?.handleSearch(),
})

onMounted(() => {
  $table.value?.handleSearch()
})

defineOptions({
  name: 'ComponentName',
})
</script>
```

**3. 添加菜单** `frontend/src/settings.js`：

菜单配置必须包含 `component` 字段，格式为 `/src/views/...`：

```javascript
export const basePermissions = [
  // 一级菜单（可选，用于分组）
  {
    code: 'ParentMenu',        // 唯一标识
    name: '父菜单',             // 显示名称
    type: 'MENU',               // 固定值
    icon: 'i-fe:list',          // 图标
    order: 10,                  // 排序（数值越小越靠前）
    enable: true,               // 是否启用
    show: true,                 // 是否显示
    children: [                 // 二级菜单数组
      {
        code: 'ChildMenu',              // 唯一标识
        name: '子菜单',                 // 显示名称
        type: 'MENU',                   // 固定值
        path: '/child-menu',            // URL 路径
        component: '/src/views/功能目录/index.vue',  // ⚠️ 必需：组件路径
        icon: 'i-fe:settings',          // 图标
        order: 1,                       // 排序
        enable: true,
        show: true,
      },
    ],
  },
]
```

#### 页面开发规范

**组件导入顺序**：
1. Naive UI 组件（按字母顺序）
2. 业务组件（`MeCrud`, `MeModal`, `MeQueryItem`）
3. Composables（`useCrud`）
4. API 接口
5. 其他工具函数

**表格列定义**：
- 使用 `render` 函数渲染复杂内容（标签、按钮等）
- 固定操作列在右侧：`fixed: 'right'`
- 长文本使用省略号：`ellipsis: { tooltip: true }`

**表单验证**：
```vue
<n-form-item
  label="字段名"
  path="fieldName"
  :rule="{
    required: true,
    message: '请输入字段名',
    trigger: ['input', 'blur'],
  }"
>
  <n-input v-model:value="modalForm.fieldName" />
</n-form-item>
```

## 最新更新

### 2026-05-13

#### M3U8 解析架构重构

**注解驱动的 M3U8 解析器** (`IPTVM3U8Parser`)
- 使用反射和注解实现灵活的 M3U8 解析
- 支持注解：`@ChannelName`、`@ChannelUrl`、`@MetadataAttribute("xxx")`
- 可解析到任何带有这些注解的目标类
- 兼容 IPTV 播放列表格式（#EXTINF 元数据）

**原始频道元数据存储** (`original_channels` 表)
- 存储解析后的原始 M3U8 频道元数据
- 字段：name, url, tvgId, tvgName, tvgLogo, groupTitle, tvgCountry, tvgLanguage
- 关联 `provider_id` 和 `task_id`，支持任务追踪
- 支持按任务查询解析结果

**实体关系**：
```
m3u8_raw_data → original_channels → channels
     ↓                ↓              ↓
  原始内容        原始元数据      处理后频道
```

#### 频道管理模块

**新增菜单**（`settings.js`）
- 源管理（订阅配置、任务历史）
- 频道管理
  - 频道列表（开发中）
  - 频道组管理 ✅
  - 频道处理规则 ✅

**频道组管理** (`/channel-management/groups`)
- 完整 CRUD 功能
- 支持按名称搜索
- 字段：id, name, sortOrder, description
- API: `GET /channel/group?name=xxx`

**频道处理规则** (`/channel-management/rules`)
- 四种规则类型（折叠面板排他性展示）
  - 频道过滤规则
  - 频道名称规范化规则
  - 延迟检测规则
  - 频道分组规则
- 每个规则：名称、匹配条件、匹配类型（包含/等于/正则）、操作值
- 启用/停用状态控制

#### 前端开发规范

**useCrud 注意事项**：
- `handleEdit(row)` - 编辑时使用 `handleEdit`，不是 `handleOpen`
- `handleDelete(row.id)` - 删除时传递 `id`，不是整个 `row` 对象
- `doUpdate: (data) => api.update(data.id, data)` - 更新时包装函数提取 id

**后端搜索支持**：
- Mapper 接口：`findByCondition(@Param("name") String name)`
- Service 方法：`findByCondition(String name)`
- Controller：`findAll(@RequestParam(required = false) String name)`

### 2026-05-12

- ✅ 创建源管理菜单和页面
  - 订阅配置页面：支持在线订阅和本地文件上传
  - 任务历史页面：查看刷新任务历史和详情
  - 更新前端对接指南，添加页面开发规范

### 2025-01-12

- ✅ 添加 M3U8 原始数据历史备份功能
- ✅ 实现逻辑删除（所有表）
- ✅ 更新所有 Mapper 使用逻辑删除
- ✅ 添加 Spring milestone 仓库支持
- ✅ 迁移到 Jackson 3.x 和 m3u8-parser 0.30
- ✅ 使用 Lombok 简化代码
- ✅ 集成 Quartz 实现定时任务调度
  - 创建 `M3U8RefreshJob` 定时任务
  - 创建 `ScheduledTaskService` 任务管理服务
  - `M3U8ProviderService` 集成自动调度
  - 支持通过 `refreshRate` 配置刷新间隔
- ✅ 生成 OpenAPI 3.0 格式 API 文档
- ✅ 添加前端模块文档
  - 基于 Vue Naive Admin 模板
  - Vue 3 + Vite + Pinia + UnoCSS + Naive UI
  - 完整的权限管理系统（RBAC）
  - 封装的业务组件（CRUD、Modal）
