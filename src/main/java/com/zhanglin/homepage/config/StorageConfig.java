package com.zhanglin.homepage.config;

import com.zhanglin.homepage.service.storage.StorageService;
import com.zhanglin.homepage.service.storage.impl.AliyunOssStorageService;
import com.zhanglin.homepage.service.storage.impl.LocalStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 存储实现的选择。
 *
 * <p>只在这里决定「用阿里云还是本地」，业务代码注入 {@link StorageService} 即可，完全无感。
 * 以后想加七牛云、MinIO，实现一个 StorageService 在这里多一个分支就行。
 *
 * @author 张林
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class StorageConfig {

    @Bean
    public StorageService storageService(StorageProperties properties) {
        String type = properties.resolveType();

        if ("aliyun".equals(type)) {
            if (!properties.isAliyunReady()) {
                throw new IllegalStateException("""
                        app.storage.type=aliyun，但阿里云配置不完整。
                        请检查 app.storage.aliyun 下的 endpoint / access-key-id / access-key-secret / bucket 四项。""");
            }
            return new AliyunOssStorageService(properties);
        }

        if (!"local".equals(type)) {
            log.warn("app.storage.type={} 不是有效值，已回退到本地存储", type);
        }
        log.info("图片存储使用【本地磁盘】目录={}，访问前缀={}；" +
                        "要切到阿里云 OSS：复制 config/application-local.yml.example 为 config/application-local.yml 并填入 OSS 配置（或注入 OSS_* 环境变量）",
                properties.getLocal().getDir(), properties.getLocal().getUrlPrefix());
        return new LocalStorageService(properties);
    }

}
