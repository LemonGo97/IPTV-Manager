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
│   └── build.gradle
├── frontend/                     # 前端模块（当前未启用）
│   └── build.gradle
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

## 开发指南

### 构建项目

```bash
./gradlew build
```

### 运行后端

```bash
./gradlew :backend:bootRun
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

前端模块当前在 `settings.gradle` 中被注释，需要时可启用。

配置：
- Node.js: 24.15.0
- pnpm: 10.33.2
- 镜像源: 中科大镜像 (https://mirrors.ustc.edu.cn/node/)

## 最新更新

### 2025-01-12

- ✅ 添加 M3U8 原始数据历史备份功能
- ✅ 实现逻辑删除（所有表）
- ✅ 更新所有 Mapper 使用逻辑删除
- ✅ 添加 Spring milestone 仓库支持
- ✅ 迁移到 Jackson 3.x 和 m3u8-parser 0.30
- ✅ 使用 Lombok 简化代码
