# IPTV-Manager Dockerfile
# 多阶段构建：前端 + 后端 + Nginx

# ============================================
# Stage 1: 构建前端
# ============================================
FROM node:24.15.0-alpine AS frontend-builder

# 安装 pnpm
RUN npm install -g pnpm@10.33.2

# 设置工作目录
WORKDIR /app/frontend

# 复制前端依赖文件
COPY frontend/package.json frontend/pnpm-lock.yaml ./

# 安装依赖
RUN pnpm install --frozen-lockfile

# 复制前端源码
COPY frontend/ ./

# 构建前端生产版本
RUN pnpm build

# ============================================
# Stage 2: 构建后端
# ============================================
FROM eclipse-temurin:21-jdk-alpine AS backend-builder

# 安装必要的工具
RUN apk add --no-cache bash

# 设置工作目录
WORKDIR /app

# 复制 Gradle wrapper 文件
COPY gradlew gradle/
COPY gradle/ gradle/
COPY build.gradle settings.gradle ./

# 复制后端源码
COPY backend/ ./backend/

# 赋予执行权限
RUN chmod +x gradlew

# 构建后端 JAR（跳过测试以加快构建速度）
RUN ./gradlew :backend:bootJar -x test --no-daemon

# ============================================
# Stage 3: 运行时镜像
# ============================================
FROM nginx:alpine

# 安装 Java JRE 和必要工具
RUN apk add --no-cache \
    openjdk21-jre \
    bash \
    curl

# 创建应用目录
WORKDIR /app

# 从构建阶段复制前端构建产物
COPY --from=frontend-builder /app/frontend/dist /usr/share/nginx/html

# 从构建阶段复制后端 JAR
COPY --from=backend-builder /app/backend/build/libs/*.jar /app/backend.jar

# 复制 nginx 配置
COPY docker/nginx.conf /etc/nginx/nginx.conf

# 复制启动脚本
COPY docker/entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

# 暴露端口
EXPOSE 80 443

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:80/api/health || exit 1

# 设置时区（可选）
ENV TZ=Asia/Shanghai

# 启动脚本
ENTRYPOINT ["/app/entrypoint.sh"]
