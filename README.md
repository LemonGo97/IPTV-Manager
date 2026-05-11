# IPTV-Manager

> IPTV 播放列表管理系统 - 支持 M3U8 源管理、频道解析和 EPG 节目单功能

## 项目简介

IPTV-Manager 是一个基于 Spring Boot 的 IPTV 播放列表管理系统，支持：
- 在线/本地 M3U8 源管理
- 自动解析 M3U8 播放列表并入库
- 频道分组管理
- EPG 节目单源管理
- M3U8 原始数据历史备份（保留最近 3 次）
- 逻辑删除（数据可恢复）
- RESTful API 接口

## 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 语言 | Java | 21 |
| 框架 | Spring Boot | 4.0.5 (Milestone) |
| 数据库 ORM | MyBatis | 4.0.1 |
| 数据库 | SQLite | 开发环境内存 / 生产环境文件 |
| 数据库迁移 | Liquibase | 4.29.2 |
| M3U8 解析 | m3u8-parser | 0.30 |
| JSON 处理 | Jackson | 3.1.2 |
| 构建工具 | Gradle | 9.4.1 |
| 代码简化 | Lombok | 1.18.42 |

## 项目结构

```
IPTV-Manager/
├── backend/                    # 后端模块
│   └── src/main/
│       ├── java/com/lemongo97/iptv/iptvmanager/
│       │   ├── Start.java                 # 启动类
│       │   ├── common/                   # 通用组件
│       │   │   ├── ApiResponse.java        # 统一响应格式
│       │   │   ├── BusinessException.java  # 业务异常
│       │   │   └── GlobalExceptionHandler.java  # 全局异常处理
│       │   ├── config/                   # 配置类
│       │   │   ├── MybatisConfig.java     # MyBatis 配置
│       │   │   ├── LiquibaseConfig.java  # Liquibase 配置
│       │   │   └── RestTemplateConfig.java # RestTemplate 配置
│       │   ├── controller/               # REST 控制器
│       │   │   ├── ChannelController.java        # 频道管理
│       │   │   ├── ChannelGroupController.java  # 频道组管理
│       │   │   ├── EpgSourceController.java     # EPG 源管理
│       │   │   └── M3U8ProviderController.java   # M3U8 源管理
│       │   ├── entity/                   # 实体类
│       │   │   ├── Channel.java              # 频道实体
│       │   │   ├── ChannelGroup.java         # 频道组实体
│       │   │   ├── EpgSource.java            # EPG 源实体
│       │   │   ├── M3U8Provider.java          # M3U8 源实体
│       │   │   └── M3U8RawData.java           # M3U8 原始数据实体
│       │   ├── mapper/                   # MyBatis Mapper
│       │   │   ├── ChannelMapper.java
│       │   │   ├── ChannelGroupMapper.java
│       │   │   ├── EpgSourceMapper.java
│       │   │   ├── M3U8ProviderMapper.java
│       │   │   └── M3U8RawDataMapper.java
│       │   ├── service/                  # 业务服务
│       │   │   ├── ChannelService.java
│       │   │   ├── ChannelGroupService.java
│       │   │   ├── EpgSourceService.java
│       │   │   ├── M3U8ProviderService.java
│       │   │   └── M3U8RawDataService.java
│       │   └── parser/                   # 解析器
│       │       └── M3U8ParserService.java  # M3U8 解析服务
│       └── resources/
│           ├── application.yml            # 应用配置
│           ├── db/                        # 数据库迁移
│           │   ├── changelog.yml
│           │   └── sql/
│           │       ├── create-m3u8-providers-table.sql
│           │       ├── create-channel-groups-table.sql
│           │       ├── create-epg-sources-table.sql
│           │       ├── update-channels-table.sql
│           │       ├── create-m3u8-raw-data-table.sql
│           │       └── add-logical-delete-fields.sql
│           └── mapper/                   # MyBatis XML
│               ├── ChannelMapper.xml
│               ├── ChannelGroupMapper.xml
│               ├── EpgSourceMapper.xml
│               ├── M3U8ProviderMapper.xml
│               └── M3U8RawDataMapper.xml
└── frontend/                   # 前端模块（待开发）
```

## 核心功能

### 1. M3U8 源管理

支持两种 M3U8 源类型：

| 类型 | 说明 | 必需字段 |
|------|------|---------|
| **online** | 在线订阅源 | `url` |
| **local** | 本地 M3U8 文件 | `filePath` |

### 2. 自动解析 M3U8

使用 `io.lindstrom:m3u8-parser` 自动解析 M3U8 文件：
- 支持在线 URL 拉取
- 支持本地文件读取
- 自动提取频道信息并入库
- **原始数据备份**：每次解析前自动保存原始 M3U8 内容，保留最近 3 次

### 3. M3U8 原始数据历史

- **自动备份**：每次获取 M3U8 内容后自动保存
- **保留策略**：每个提供者保留最近 3 次的历史数据
- **用途**：
  - 解析失败时的备用数据
  - 调试和问题排查
  - 历史对比分析

### 4. 频道分组管理

- 创建/编辑/删除频道组
- 支持自定义排序
- 频道关联到分组

### 5. EPG 节目单管理

- EPG 源管理（XML/JSON 格式）
- 支持自定义频道 ID 映射

### 6. 逻辑删除

所有表都使用逻辑删除（软删除）：
- **数据保留**：删除操作只标记 `deleted = 1`，不物理删除数据
- **可恢复**：误删除后可通过 SQL 恢复
- **审计追踪**：保留完整的数据历史

## 数据库设计

### 核心表结构

| 表名 | 说明 |
|------|------|
| `m3u8_providers` | M3U8 源提供者 |
| `channel_groups` | 频道分组 |
| `channels` | 频道信息 |
| `epg_sources` | EPG 节目单源 |
| `m3u8_raw_data` | M3U8 原始数据历史 |

### 实体关系

```
m3u8_providers (1) ────< (*) channels
              (1)         (*)
              (1)         (1)
              (1)         (*)
m3u8_raw_data  (*)       (1)
              (1)         
channel_groups (1) ────< (*) channels ──> (1) epg_sources
```

### 通用字段

所有表都包含以下字段：

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | INTEGER | 主键，自增 |
| `created_at` | TIMESTAMP | 创建时间 |
| `updated_at` | TIMESTAMP | 更新时间 |
| `deleted` | INTEGER | 逻辑删除标记（0=正常，1=已删除） |

## API 端点

### 频道管理
```
GET    /api/channels              # 获取所有频道
GET    /api/channels/{id}         # 获取单个频道
GET    /api/channels/group/{group} # 按分组查询
POST   /api/channels              # 创建频道
PUT    /api/channels/{id}         # 更新频道
DELETE /api/channels/{id}         # 删除频道（逻辑删除）
```

### M3U8 源管理
```
GET    /api/m3u8/provider           # 获取所有 M3U8 源
GET    /api/m3u8/provider/{id}      # 获取单个 M3U8 源
POST   /api/m3u8/provider           # 创建 M3U8 源
PUT    /api/m3u8/provider/{id}      # 更新 M3U8 源
DELETE /api/m3u8/provider/{id}      # 删除 M3U8 源（逻辑删除）
POST   /api/m3u8/provider/{id}/refresh  # 刷新并解析 M3U8
```

### 频道组管理
```
GET    /api/channel/group           # 获取所有频道组
GET    /api/channel/group/{id}      # 获取单个频道组
POST   /api/channel/group           # 创建频道组
PUT    /api/channel/group/{id}      # 更新频道组
DELETE /api/channel/group/{id}      # 删除频道组（逻辑删除）
```

### EPG 源管理
```
GET    /api/epg/source             # 获取所有 EPG 源
GET    /api/epg/source/{id}        # 获取单个 EPG 源
POST   /api/epg/source             # 创建 EPG 源
PUT    /api/epg/source/{id}        # 更新 EPG 源
DELETE /api/epg/source/{id}        # 删除 EPG 源（逻辑删除）
POST   /api/epg/source/{id}/refresh # 刷新 EPG 源
```

## 快速开始

### 环境要求

- Java 21+
- Gradle 9.4.1+

### 启动项目

```bash
# 开发环境（内存数据库）
./gradlew :backend:bootRun

# 生产环境（文件数据库）
./gradlew :backend:bootRun --args='--spring.profiles.active=prod'
```

### API 访问

默认地址：`http://localhost:8080`

## 配置说明

### 环境配置

| 环境 | 数据库类型 | 连接字符串 |
|------|-----------|-----------|
| dev (默认) | 内存数据库 | `jdbc:sqlite::memory:` |
| prod | 文件数据库 | `jdbc:sqlite:iptv.db` |

### 日志配置

- 根日志级别：INFO
- 应用日志级别：DEBUG
- SQL 日志级别：TRACE

## 响应格式

所有 API 统一返回以下 JSON 格式：

```json
{
  "success": true,
  "data": { ... },
  "message": "操作成功",
  "timestamp": "2025-01-12T10:30:00"
}
```

## 开发指南

### 代码规范

- 使用 Record 定义实体类
- 使用 Lombok 简化代码（`@Slf4j`, `@RequiredArgsConstructor`）
- 使用构造器注入依赖
- 使用 `@Transactional` 管理事务
- 统一使用 `ApiResponse` 返回

### 测试

```bash
# 运行测试
./gradlew :backend:test

# 构建项目
./gradlew :backend:build
```

## 待实现功能

- [ ] EPG 解析和入库
- [ ] M3U8 定时自动刷新
- [ ] 频道收藏功能
- [ ] 频道搜索和过滤
- [ ] 前端界面开发

## 许可证

MIT License
