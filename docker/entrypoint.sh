#!/bin/bash
# IPTV-Manager 容器启动脚本
# 同时启动 Nginx 和 Spring Boot 后端

set -e

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  IPTV-Manager Docker 容器启动${NC}"
echo -e "${GREEN}========================================${NC}"

# JVM 参数配置
JVM_OPTS="${JVM_OPTS:- -Xms512m -Xmx1024m}"
SPRING_OPTS="${SPRING_OPTS:- --server.port=8080}"

echo -e "${YELLOW}JVM 参数: $JVM_OPTS${NC}"
echo -e "${YELLOW}Spring 参数: $SPRING_OPTS${NC}"

# 等待函数
wait_for_backend() {
    echo -e "${YELLOW}等待后端服务启动...${NC}"
    local max_attempts=60
    local attempt=0

    while [ $attempt -lt $max_attempts ]; do
        if curl -sf http://localhost:8080/api/health > /dev/null 2>&1; then
            echo -e "${GREEN}后端服务已就绪！${NC}"
            return 0
        fi
        attempt=$((attempt + 1))
        sleep 2
    done

    echo -e "${RED}后端服务启动超时！${NC}"
    return 1
}

# 启动 Spring Boot 后端
echo -e "${GREEN}启动 Spring Boot 后端...${NC}"
java $JVM_OPTS -jar /app/backend.jar $SPRING_OPTS \
    > /var/log/backend.log 2>&1 &

BACKEND_PID=$!
echo -e "${GREEN}后端 PID: $BACKEND_PID${NC}"

# 等待后端就绪
if ! wait_for_backend; then
    echo -e "${RED}后端启动失败，查看日志:${NC}"
    tail -n 50 /var/log/backend.log
    exit 1
fi

# 启动 Nginx
echo -e "${GREEN}启动 Nginx...${NC}"
nginx -g 'daemon off;' &
NGINX_PID=$!
echo -e "${GREEN}Nginx PID: $NGINX_PID${NC}"

# 优雅退出处理
shutdown() {
    echo -e "${YELLOW}接收到关闭信号，正在优雅关闭...${NC}"
    kill -TERM $NGINX_PID 2>/dev/null || true
    kill -TERM $BACKEND_PID 2>/dev/null || true
    wait $NGINX_PID 2>/dev/null || true
    wait $BACKEND_PID 2>/dev/null || true
    echo -e "${GREEN}容器已关闭${NC}"
    exit 0
}

trap shutdown SIGTERM SIGINT

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  所有服务已启动${NC}"
echo -e "${GREEN}  前端: http://localhost:80${NC}"
echo -e "${GREEN}  后端: http://localhost:80/api${NC}"
echo -e "${GREEN}========================================${NC}"

# 保持容器运行
wait $NGINX_PID $BACKEND_PID
