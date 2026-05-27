# IPTV-Manager

> IPTV 播放列表管理系统 - 支持 M3U8/TXT 源管理、频道清洗、EPG 电子节目单功能

## 项目简介

IPTV-Manager 是一个基于 Spring Boot 的 IPTV 播放列表管理系统，支持：
- **IPTV 源管理**：支持 M3U8/TXT 格式的在线订阅源和本地文件
- **频道清洗**：基于规则引擎的频道数据清洗（过滤、规范化、延迟检测、分组）
- **频道管理**：频道列表查看、分页、搜索、排序
- **频道组管理**：频道分组管理
- **EPG 管理**：支持 XMLTV/XMLTV_GZIP 格式的 EPG 源管理和节目查询
- **任务进度管理**：异步任务执行和进度追踪
- **定时刷新**：Quartz 定时任务自动刷新
- **原始数据备份**：保留解析历史
- **逻辑删除**：所有数据支持软删除，可随时恢复
- **前后端分离**：Vue 3 + Spring Boot 架构

## 技术栈

### 后端

| 类别 | 技术 | 版本 |
|------|------|------|
| 语言 | Java | 21 |
| 框架 | Spring Boot | 4.0.5 (Milestone) |
| 数据库 ORM | MyBatis | 4.0.1 |
| 数据库 | SQLite | 3.51.3.0 |
| 数据库迁移 | Liquibase | 4.29.2 |
| M3U8 解析 | m3u8-parser | 0.30 |
| TXT 解析 | 自定义解析器 | - |
| JSON 处理 | Jackson | 3.1.2 |
| 代码简化 | Lombok | 1.18.42 |
| 定时任务 | Quartz | 2.5.0 |

### 前端

| 类别 | 技术 | 版本 |
|------|------|------|
| 框架 | Vue | 3.5.29 |
| 构建工具 | Vite | 7.3.1 |
| 状态管理 | Pinia | 3.0.4 |
| UI 组件 | Naive UI | 2.43.2 |
| 原子 CSS | UnoCSS | 66.6.2 |
| 路由 | Vue Router | 5.0.3 |
| HTTP 客户端 | Axios | 1.13.5 |
| 包管理器 | pnpm | 10.33.2 |

## 项目结构

```
IPTV-Manager/
├── backend/                          # 后端模块
│   ├── src/main/
│   │   ├── java/com/lemongo97/iptv/iptvmanager/
│   │   │   ├── Start.java                   # 启动类
│   │   │   ├── cleanup/                    # 数据清洗引擎
│   │   │   │   ├── engine/                 # 清洗引擎实现
│   │   │   │   ├── rule/                   # 清洗规则
│   │   │   │   └── config/                 # 清洗配置
│   │   │   ├── common/                     # 通用组件
│   │   │   │   ├── ApiResponse.java        # 统一响应格式
│   │   │   │   ├── BusinessException.java # 业务异常
│   │   │   │   ├── PageQuery.java          # 分页查询
│   │   │   │   └── PageResult.java         # 分页结果
│   │   │   ├── configuration/              # 配置类
│   │   │   │   ├── mybatis/                # MyBatis 配置
│   │   │   │   └── websocket/              # WebSocket 配置
│   │   │   ├── dto/                        # 数据传输对象
│   │   │   ├── entity/                     # 实体类
│   │   │   ├── endpoint/                   # 端点
│   │   │   │   ├── controller/             # REST 控制器
│   │   │   │   └── websocket/              # WebSocket 端点
│   │   │   ├── mapper/                     # MyBatis Mapper
│   │   │   ├── parser/                     # 解析器
│   │   │   │   ├── m3u8/                   # M3U8 解析器
│   │   │   │   ├── txt/                    # TXT 解析器
│   │   │   │   └── epg/                   # EPG 解析器
│   │   │   ├── quartz/                     # Quartz 定时任务
│   │   │   │   └── job/                    # 任务实现
│   │   │   ├── service/                    # 业务服务
│   │   │   └── utils/                      # 工具类
│   │   └── resources/
│   │       ├── application.yml             # 应用配置
│   │       ├── db/                         # 数据库迁移
│   │       │   ├── changelog.yml
│   │       │   └── sql/
│   │       └── mapper/                     # MyBatis XML
│   ├── doc/                                # API 文档
│   └── build.gradle
├── frontend/                            # 前端模块
│   ├── src/
│   │   ├── components/                     # 组件
│   │   │   ├── common/                     # 通用组件
│   │   │   └── me/                         # 业务组件
│   │   ├── composables/                    # 组合式函数
│   │   ├── layouts/                        # 布局组件
│   │   ├── router/                         # 路由配置
│   │   ├── store/                          # Pinia 状态管理
│   │   ├── utils/                          # 工具函数
│   │   ├── views/                          # 页面视图（API 与页面同目录）
│   │   │   ├── source/                     # 源管理
│   │   │   │   ├── subscribe/              # 订阅配置
│   │   │   │   └── history/                # 任务历史
│   │   │   ├── channel-management/         # 频道管理
│   │   │   │   ├── list/                   # 频道列表
│   │   │   │   ├── groups/                 # 频道组管理
│   │   │   │   └── rules/                  # 频道处理规则
│   │   │   └── epg-management/             # EPG 管理
│   │   │       ├── sources/                # EPG 源管理
│   │   │       └── viewer/                 # EPG 查看
│   │   ├── App.vue                         # 根组件
│   │   ├── main.js                         # 应用入口
│   │   └── settings.js                     # 应用配置
│   ├── build.gradle                        # Gradle 构建配置
│   ├── package.json                        # npm 依赖
│   ├── vite.config.js                      # Vite 配置
│   └── uno.config.js                       # UnoCSS 配置
├── gradle/
│   ├── libs.versions.toml                  # 版本目录
│   └── repositories.gradle
├── build.gradle                            # 根构建文件
└── settings.gradle                         # 项目设置
```

## 核心功能

### 1. IPTV 源管理

支持两种源类型和两种内容格式：

| 类型 | 内容格式 | 说明 |
|------|---------|------|
| **online** | M3U8/TXT | 在线订阅源 |
| **file** | M3U8/TXT | 本地文件上传 |

### 2. 频道清洗引擎

基于规则引擎的频道数据清洗，支持四种清洗规则：

| 规则类型 | 说明 | 可用引擎 |
|---------|------|---------|
| **FILTER** | 频道过滤 | BlackListEngine |
| **NAME_NORMALIZE** | 名称规范化 | CaseConversionEngine, RegexReplaceEngine, StringReplaceEngine, StringRemoveEngine, OpenCCEngine |
| **DELAY_DETECT** | 延迟检测 | HttpCheckEngine, FFProbeCheckEngine, FFMpegCheckEngine |
| **GROUPING** | 频道分组 | GroupingEngine |

### 3. EPG 管理

- 支持 XMLTV 和 XMLTV_GZIP 格式
- 树形展示频道和节目
- 支持懒加载节目列表
- 异步刷新任务和进度追踪

### 4. 任务进度管理

- 异步任务执行（IPTV 源刷新、EPG 源刷新）
- 实时进度追踪
- WebSocket 实时推送

### 5. 逻辑删除

所有表都使用逻辑删除（软删除）：
- 删除操作只标记 `deleted = true`
- 数据永久保留，可随时恢复
- 支持审计追踪

## 数据库设计

### 核心表结构

| 表名 | 说明 |
|------|------|
| `iptv_providers` | IPTV 源提供者 |
| `iptv_provider_raw_data` | IPTV 原始数据历史 |
| `iptv_provider_refresh_tasks` | IPTV 刷新任务 |
| `original_channels` | 原始频道元数据 |
| `channels` | 清洗后的频道 |
| `channel_groups` | 频道分组 |
| `cleanup_rules` | 数据清洗规则 |
| `cleanup_engines` | 清洗引擎配置 |
| `epg_providers` | EPG 源 |
| `epg_channels` | EPG 频道 |
| `epg_programs` | EPG 节目 |
| `task_progress` | 任务进度 |

### 实体关系

```
iptv_providers → iptv_provider_raw_data → iptv_provider_refresh_tasks
     ↓                    ↓                          ↓
original_channels → channels (清洗后)     task_progress
                       ↓
                 channel_groups

epg_providers → epg_channels → epg_programs

cleanup_rules → cleanup_engines
```

## API 端点

### IPTV 源管理

```
GET    /iptv/provider                # 获取所有 IPTV 源
GET    /iptv/provider/{id}           # 获取单个 IPTV 源
POST   /iptv/provider                # 创建 IPTV 源（支持文件上传）
PUT    /iptv/provider/{id}           # 更新 IPTV 源（支持文件上传）
DELETE /iptv/provider/{id}           # 删除 IPTV 源（逻辑删除）
POST   /iptv/provider/{id}/refresh  # 手动刷新 IPTV 源（异步）
```

### IPTV 任务历史

```
GET    /iptv/task/history           # 分页查询任务历史
GET    /iptv/task/history/count     # 获取任务总数
GET    /iptv/task/history/{id}      # 获取任务详情
GET    /iptv/task/history/{id}/channels  # 获取任务解析的频道列表
```

### 频道管理

```
GET    /channel                     # 获取频道列表（分页、搜索、排序）
GET    /channel/statistic           # 获取统计信息
GET    /channel/options             # 获取过滤器选项
GET    /channel/{id}/timeline       # 获取频道 EPG 时间轴
POST   /channel/clean/{step}        # 执行数据清洗
```

### 频道组管理

```
GET    /channel/group                # 获取所有频道组
POST   /channel/group                # 创建频道组
PUT    /channel/group/{id}           # 更新频道组
DELETE /channel/group/{id}           # 删除频道组（逻辑删除）
```

### 频道清洗规则

```
GET    /channel/cleanup/engines      # 获取支持的处理引擎列表
GET    /channel/cleanup/rules        # 获取所有清洗规则
GET    /channel/cleanup/rules/{id}   # 获取单个规则详情
POST   /channel/cleanup/rules        # 创建清洗规则
PUT    /channel/cleanup/rules/{id}   # 更新清洗规则
DELETE /channel/cleanup/rules/{id}   # 删除清洗规则（逻辑删除）
PUT    /channel/cleanup/rules/reorder  # 重排序规则
```

### EPG 源管理

```
GET    /epg/provider                 # 获取所有 EPG 源
GET    /epg/provider/{id}            # 获取单个 EPG 源
POST   /epg/provider                 # 创建 EPG 源
PUT    /epg/provider/{id}            # 更新 EPG 源
DELETE /epg/provider/{id}            # 删除 EPG 源（逻辑删除）
POST   /epg/provider/{id}/refresh    # 手动刷新 EPG 源（异步）
```

### EPG 节目查询

```
GET    /epg/channels                 # 获取指定源的频道列表
GET    /epg/programs                 # 获取指定频道的节目列表
```

### 任务进度

```
GET    /task/progress/{taskId}      # 获取任务进度
GET    /task/progress/latest/{taskType}  # 获取最新任务进度
```

完整的 API 文档请查看：`backend/doc/openapi.yaml`

## 快速开始

### 环境要求

**后端**：
- Java 21+
- Gradle 9.4.1+

**前端**：
- Node.js 24.15.0+
- pnpm 10.33.2+

### 启动后端

```bash
# 开发环境
./gradlew :backend:bootRun

# 运行测试
./gradlew :backend:test

# 构建项目
./gradlew :backend:build
```

后端默认地址：`http://localhost:8080`

### 启动前端

```bash
# 通过 Gradle（推荐）
./gradlew :frontend:dev

# 直接使用 pnpm
cd frontend && pnpm install
pnpm dev
```

前端默认地址：`http://localhost:3200`

### 构建前端

```bash
# 通过 Gradle（推荐）
./gradlew :frontend:build

# 直接使用 pnpm
cd frontend && pnpm build
```

## 开发指南

### 代码规范

**后端**：
- 使用 Lombok 简化代码（`@Slf4j`, `@Data`, `@RequiredArgsConstructor`）
- 使用构造器注入依赖
- 使用 `@Transactional` 管理事务
- 统一使用 `ApiResponse` 返回
- 所有实体使用逻辑删除

**前端**：
- API 与页面同目录，便于维护
- 使用 `useCrud` 管理 CRUD 操作
- 使用 UnoCSS 原子化 CSS
- 使用 Hash 路由模式

### API 对接

当前使用 Apifox 云端 Mock 进行开发。对接真实后端时，修改 `frontend/.env.development`：

```bash
# 使用代理连接本地后端
VITE_AXIOS_BASE_URL = '/api'
VITE_PROXY_TARGET = 'http://localhost:8080'
```

### 调试指南

**重要**：使用 Chrome DevTools MCP 调试时，如遇登录页面，请点击页面上的"一键登录"按钮进入系统。

本项目使用 Hash 路由模式（`VITE_USE_HASH = 'true'`），调试时需使用正确的 URL 格式：

| 错误格式 | 正确格式 |
|---------|---------|
| `http://localhost:3200/source/subscribe` | `http://localhost:3200/#/source/subscribe` |

## 配置说明

### 依赖版本管理

项目使用 Gradle 版本目录（Version Catalog），所有依赖版本在 `gradle/libs.versions.toml` 中集中管理。

### 依赖版本说明

**注意**：项目使用了一些里程碑版本的依赖：

- **Spring Boot 4.0.5**：里程碑版本，需要 Spring milestone 仓库
- **Jackson 3.1.2**：包名已从 `com.fasterxml.jackson` 改为 `tools.jackson`
- **m3u8-parser 0.30**：API 有重大变化，注意查阅最新文档

### 数据库

使用 SQLite 作为嵌入式数据库，无需额外安装数据库服务。

## 许可证

MIT License
