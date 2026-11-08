package com.zhanglin.homepage.service.storage.impl;

import com.zhanglin.homepage.common.ResultCode;
import com.zhanglin.homepage.common.exception.BusinessException;
import com.zhanglin.homepage.config.StorageProperties;
import com.zhanglin.homepage.service.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 本地磁盘存储：开发环境默认用这个，不需要任何云服务账号。
 *
 * <p>文件写到 {@code app.storage.local.dir}（默认 ./uploads），
 * 通过 WebMvcConfig 里映射的 {@code /uploads/**} 对外访问。
 *
 * @author 张林
 */
@Slf4j
@RequiredArgsConstructor
public class LocalStorageService implements StorageService {

    private static final DateTimeFormatter DATE_PATH = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final StorageProperties properties;

    @Override
    public String upload(byte[] bytes, String extension, String dir) {
        String key = buildKey(dir, extension);
        Path target = root().resolve(key);
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, bytes);
            log.debug("文件已写入本地: {}", target.toAbsolutePath());
        } catch (IOException e) {
            log.error("本地文件写入失败: {}", target.toAbsolutePath(), e);
            throw new BusinessException(ResultCode.ERROR.getCode(), "文件保存失败，请检查磁盘权限");
        }
        return properties.getLocal().getUrlPrefix() + "/" + key;
    }

    @Override
    public void deleteQuietly(String url) {
        if (url == null || !url.startsWith(properties.getLocal().getUrlPrefix() + "/")) {
            // 不是本地存储的地址（比如之前用过阿里云），直接跳过
            return;
        }
        String key = url.substring(properties.getLocal().getUrlPrefix().length() + 1);
        try {
            Files.deleteIfExists(root().resolve(key));
        } catch (IOException e) {
            log.warn("本地文件删除失败，忽略: {}", key, e);
        }
    }

    @Override
    public String type() {
        return "local";
    }

    private Path root() {
        return Paths.get(properties.getLocal().getDir()).toAbsolutePath().normalize();
    }

    private String buildKey(String dir, String extension) {
        String safeDir = (dir == null || dir.isBlank()) ? "common" : dir.replaceAll("[^a-zA-Z0-9_-]", "");
        return safeDir + "/" + LocalDate.now().format(DATE_PATH) + "/"
                + UUID.randomUUID().toString().replace("-", "").substring(0, 16) + "." + extension;
    }

}
