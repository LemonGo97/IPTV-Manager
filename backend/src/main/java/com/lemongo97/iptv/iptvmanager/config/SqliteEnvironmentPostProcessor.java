package com.lemongo97.iptv.iptvmanager.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

import lombok.extern.slf4j.Slf4j;

/**
 * SQLite 环境后处理器
 * 在 Spring 启动前确保数据库目录存在
 */
@Slf4j
public class SqliteEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String jdbcUrl = environment.getProperty("spring.datasource.url");

        if (jdbcUrl == null || !jdbcUrl.startsWith("jdbc:sqlite:")) {
            return;
        }

        // 提取文件路径
        String filePath = jdbcUrl.substring("jdbc:sqlite:".length());

        // 如果是内存数据库，不需要处理
        if (":memory:".equals(filePath)) {
            log.info("Using in-memory SQLite database");
            return;
        }

        Path path = Paths.get(filePath).toAbsolutePath();

        try {
            // 确保父目录存在
            Path parentDir = path.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
                log.info("Created SQLite database directory: {}", parentDir);
            }

            log.info("SQLite database path: {}", path);
        } catch (Exception e) {
            log.error("Failed to create SQLite database directory: {}", path, e);
            throw new RuntimeException("Failed to initialize SQLite database directory", e);
        }
    }
}
