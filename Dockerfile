# IPTV-Manager Dockerfile
# 多阶段构建：使用 Gradle 统一编译前后端

# ============================================
# Stage 1: 使用 Gradle 构建前后端
# ============================================
FROM gradle:jdk21-corretto AS builder

# 设置工作目录
WORKDIR /app

# 复制 Gradle wrapper 和配置文件
COPY gradlew ./
COPY gradle/ gradle/
COPY build.gradle settings.gradle ./

# 复制所有子模块
COPY frontend/ ./frontend/
COPY backend/ ./backend/
COPY module/ ./module/

# 赋予执行权限
RUN chmod +x ./gradlew

ENV NODE_OPTIONS="--max-old-space-size=4096"
# 使用 Gradle 统一构建前后端
# - frontend:build 生成前端静态文件到 frontend/dist
# - backend:bootJar 生成后端 JAR 到 backend/build/libs
# -x test 跳过测试以加快构建速度
# --no-daemon 避免守护进程
RUN gradle clean build -x test --no-daemon

# ============================================
# Stage 2: 运行时镜像
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
COPY --from=builder /app/frontend/dist /usr/share/nginx/html

# 从构建阶段复制后端 JAR
COPY --from=builder /app/backend/build/libs/*.jar /app/backend.jar

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
