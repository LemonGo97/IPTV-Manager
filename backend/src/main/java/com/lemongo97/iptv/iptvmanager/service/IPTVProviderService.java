package com.lemongo97.iptv.iptvmanager.service;

import com.lemongo97.iptv.iptvmanager.common.BusinessException;
import com.lemongo97.iptv.iptvmanager.entity.IPTVProvider;
import com.lemongo97.iptv.iptvmanager.entity.IPTVProviderRefreshTask;
import com.lemongo97.iptv.iptvmanager.mapper.IPTVProviderMapper;
import com.lemongo97.iptv.iptvmanager.mapper.IPTVProviderRefreshTaskMapper;
import com.lemongo97.iptv.iptvmanager.quartz.ScheduledTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * IPTV 源服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IPTVProviderService {

    private final IPTVProviderMapper providerMapper;
    private final IPTVProviderRawDataService rawDataService;
    private final ScheduledTaskService scheduledTaskService;
    private final IPTVProviderRefreshTaskMapper taskMapper;

    @Value("${app.upload.m3u8-dir:tmp/uploads/m3u8}")
    private String uploadDir;

    /**
     * 获取所有 IPTV 源
     */
    public List<IPTVProvider> findAll() {
        return providerMapper.findAll();
    }

    /**
     * 根据 ID 获取 IPTV 源
     */
    public IPTVProvider findById(Long id) {
        return providerMapper.findById(id)
                .orElseThrow(() -> new BusinessException("IPTV provider not found: id=" + id));
    }

    /**
     * 根据类型获取 IPTV 源
     */
    public List<IPTVProvider> findByType(String type) {
        return providerMapper.findByType(type);
    }

    /**
     * 获取启用的 IPTV 源
     */
    public List<IPTVProvider> findEnabled() {
        return providerMapper.findEnabled();
    }

    /**
     * 创建 IPTV 源
     */
    @Transactional
    public IPTVProvider create(IPTVProvider provider) {
        log.info("Creating IPTV provider: {}", provider.getName());

        // 验证类型和字段
        validateProvider(provider);

        var now = LocalDateTime.now();
        var newProvider = new IPTVProvider(
                null,
                provider.getName(),
                provider.getType(),
                provider.getContentType(),
                provider.getUrl(),
                provider.getFilePath(),
                provider.getHeaders(),
                provider.getRefreshRate() != null ? provider.getRefreshRate() : 3600,
                provider.getEnabled() != null ? provider.getEnabled() : true,
                provider.getDescription(),
                now,
                now,
                false
        );

        providerMapper.insert(newProvider);
        try {
//            scheduledTaskService.scheduleOrUpdateJob(newProvider);
        } catch (Exception e) {
            log.warn("Failed to schedule job for provider: id={}", newProvider.getId(), e);
        }
        log.info("IPTV provider created: id={}", newProvider.getId());
        return newProvider;
    }

    /**
     * 更新 IPTV 源
     */
    @Transactional
    public IPTVProvider update(Long id, IPTVProvider provider) {
        var existing = findById(id);
        log.info("Updating IPTV provider: id={}", id);

        // 验证类型和字段
        validateProvider(provider);

        var updated = new IPTVProvider(
                id,
                provider.getName() != null ? provider.getName() : existing.getName(),
                provider.getType() != null ? provider.getType() : existing.getType(),
                provider.getContentType() != null ? provider.getContentType() : existing.getContentType(),
                provider.getUrl(),
                provider.getFilePath(),
                provider.getHeaders(),
                provider.getRefreshRate() != null ? provider.getRefreshRate() : existing.getRefreshRate(),
                provider.getEnabled() != null ? provider.getEnabled() : existing.getEnabled(),
                provider.getDescription(),
                existing.getCreatedAt(),
                LocalDateTime.now(),
                existing.getDeleted()
        );

        providerMapper.update(updated);
        try {
            scheduledTaskService.scheduleOrUpdateJob(updated);
        } catch (Exception e) {
            log.warn("Failed to schedule job for provider: id={}", id, e);
        }
        log.info("IPTV provider updated: id={}", id);
        return updated;
    }

    /**
     * 删除 IPTV 源
     */
    @Transactional
    public void deleteById(Long id) {
        findById(id);
        log.info("Deleting IPTV provider: id={}", id);
        // 先删除关联的定时任务
        scheduledTaskService.deleteJob(id);
        // 先删除关联的原始数据
        rawDataService.deleteByProviderId(id);
        providerMapper.deleteById(id);
    }

    /**
     * 刷新 IPTV 源（异步）
     * 创建任务记录并提交给 Quartz 执行，立即返回任务 ID
     *
     * @param id IPTV 提供者 ID
     * @return 任务 ID
     */
    @Transactional
    public Long refresh(Long id) {
        var provider = findById(id);
        log.info("Submitting IPTV provider refresh task: id={}, type={}", id, provider.getType());

        if (!provider.getEnabled()) {
            throw new BusinessException("Cannot refresh disabled provider: id=" + id);
        }

        // 创建任务记录
        long startTime = System.currentTimeMillis();
        var task = new IPTVProviderRefreshTask(
            null,
            provider.getId(),
            null, // providerName 通过关联查询获取
            "manual",
            "running",
            startTime,
            null,
            null,
            null,
            null,
            null,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        taskMapper.insert(task);
        log.info("Created refresh task: taskId={}, providerId={}", task.getId(), provider.getId());

        // 提交给 Quartz 执行
        scheduledTaskService.triggerManualJob(provider.getId(), task.getId());
        log.info("Submitted IPTV refresh to Quartz: taskId={}, providerId={}", task.getId(), provider.getId());

        return task.getId();
    }

    /**
     * 验证 IPTV 源
     */
    private void validateProvider(IPTVProvider provider) {
        IPTVProvider.Type type = provider.getType();
        if (type == null) {
            throw new BusinessException("Provider type is required");
        }

        if (type == IPTVProvider.Type.online && provider.getUrl() == null) {
            throw new BusinessException("URL is required for online provider");
        }

        if (type == IPTVProvider.Type.file && provider.getFilePath() == null) {
            throw new BusinessException("File path is required for local provider");
        }

        // Headers 验证暂时跳过，后续可以添加 JSON 格式验证
    }

    /**
     * 上传 IPTV 文件并创建本地类型的 Provider
     */
    @Transactional
    public IPTVProvider uploadFile(MultipartFile file, String name, String description) {
        if (file.isEmpty()) {
            throw new BusinessException("File is empty");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || (!originalFilename.endsWith(".m3u") && !originalFilename.endsWith(".m3u8"))) {
            throw new BusinessException("Invalid file format. Only .m3u and .m3u8 files are allowed");
        }

        try {
            // 创建上传目录
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 生成唯一文件名
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFilename = UUID.randomUUID() + fileExtension;
            Path targetPath = uploadPath.resolve(uniqueFilename);

            // 保存文件
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("File uploaded: {}", targetPath);

            // 使用文件名作为默认名称（如果未提供）
            String providerName = name != null && !name.isBlank()
                ? name
                : originalFilename.substring(0, originalFilename.lastIndexOf("."));

            // 创建本地类型的 Provider
            var provider = new IPTVProvider(
                null,
                providerName,
                IPTVProvider.Type.online,
                IPTVProvider.ContentType.M3U8,
                null,
                targetPath.toString(),
                null,
                null,
                true,
                description,
                null,
                null,
                false
            );

            return create(provider);

        } catch (IOException e) {
            log.error("Failed to upload file", e);
            throw new BusinessException("Failed to upload file: " + e.getMessage(), e);
        }
    }
}
