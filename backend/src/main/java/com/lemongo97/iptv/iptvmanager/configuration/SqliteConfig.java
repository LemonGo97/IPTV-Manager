package com.lemongo97.iptv.iptvmanager.configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

/**
 * SQLite 配置类
 * 确保数据库文件和目录在应用启动前就存在
 */
@Slf4j
@Configuration
@AllArgsConstructor
public class SqliteConfig {

    private final Environment environment;

    /**
     * 在应用启动早期创建数据库文件（在 Liquibase 之前）
     */
    @EventListener(ApplicationStartingEvent.class)
    public void createDatabaseFile() {
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

            // 如果文件不存在，创建空文件
            if (!Files.exists(path)) {
                Files.createFile(path);
                log.info("Created SQLite database file: {}", path);
            } else {
                log.info("SQLite database file exists: {}", path);
            }
        } catch (Exception e) {
            log.error("Failed to initialize SQLite database file: {}", path, e);
            throw new RuntimeException("Failed to initialize SQLite database file", e);
        }
    }
}
