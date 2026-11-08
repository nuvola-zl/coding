package com.zhanglin.homepage.service.storage.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.zhanglin.homepage.common.ResultCode;
import com.zhanglin.homepage.common.exception.BusinessException;
import com.zhanglin.homepage.config.StorageProperties;
import com.zhanglin.homepage.service.storage.StorageService;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 阿里云 OSS 存储。
 *
 * <p>上线后图片放 OSS 才合理：不占服务器磁盘、不怕容器重启丢文件、走 CDN 加载快。
 *
 * <p><b>三个容易踩的坑（都已在下面处理）：</b>
 * <ol>
 *   <li>endpoint 不能带 http(s):// 前缀，SDK 会直接报错 —— {@link #normalizeEndpoint} 帮你兜住；</li>
 *   <li>OSSClient 是线程安全的，全局一个就够，每次 new 会造成连接泄漏 —— 这里只建一次；</li>
 *   <li>必须设置 Content-Type，否则浏览器访问图片会变成下载而不是显示 —— 见 {@code ObjectMetadata}。</li>
 * </ol>
 *
 * @author 张林
 */
@Slf4j
public class AliyunOssStorageService implements StorageService {

    private static final DateTimeFormatter DATE_PATH = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    /** 用于匿名探活，不需要连接池调优，全局一个即可 */
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private final StorageProperties properties;
    private final StorageProperties.Aliyun cfg;
    private final OSS client;

    public AliyunOssStorageService(StorageProperties properties) {
        this.properties = properties;
        this.cfg = properties.getAliyun();
        this.client = new OSSClientBuilder().build(normalizeEndpoint(cfg.getEndpoint()),
                cfg.getAccessKeyId(), cfg.getAccessKeySecret());
        log.info("阿里云 OSS 已启用 bucket={} endpoint={}", cfg.getBucket(), normalizeEndpoint(cfg.getEndpoint()));
    }

    @Override
    public String upload(byte[] bytes, String extension, String dir) {
        String key = buildKey(dir, extension);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(bytes.length);
        metadata.setContentType(guessContentType(extension));
        // 图片内容不变，让浏览器/CDN 缓存一年，省流量也省费用
        metadata.setCacheControl("public, max-age=31536000");

        try {
            client.putObject(cfg.getBucket(), key,
                    new ByteArrayInputStream(bytes), metadata);
        } catch (Exception e) {
            log.error("OSS 上传失败 bucket={} key={}", cfg.getBucket(), key, e);
            throw new BusinessException(ResultCode.ERROR.getCode(), "图片上传失败，请检查 OSS 配置");
        }

        String url = buildUrl(key);
        // 上传成功 ≠ 浏览器能打开。私有 Bucket 的图片只能带签名访问，
        // 页面上的 <img> 是匿名请求，会直接 403，这里主动探一次把原因喊出来
        checkAnonymousReadable(url);
        return url;
    }

    @Override
    public void deleteQuietly(String url) {
        if (url == null || url.isBlank()) {
            return;
        }
        String key = extractKey(url);
        if (key == null) {
            return;
        }
        try {
            client.deleteObject(cfg.getBucket(), key);
        } catch (Exception e) {
            log.warn("OSS 删除失败，忽略: {}", key, e);
        }
    }

    @Override
    public String type() {
        return "aliyun";
    }

    @PreDestroy
    public void shutdown() {
        // 不关会留下后台线程，应用关闭时偶尔卡住
        if (client != null) {
            client.shutdown();
        }
    }

    // ==================== 内部工具 ====================

    /**
     * 用「不带任何签名」的请求探一下图片地址，模拟浏览器的访问方式。
     *
     * <p>这是本项目踩过的真实坑：Bucket 是「私有」时，上传（带 AK 签名）完全成功，
     * 但页面上 {@code <img src>} 是匿名 GET，OSS 直接返回 403 AccessDenied，
     * 表现为「图片在后台传上去了，主页却是一片空白」，而且没有任何报错。
     *
     * <p>这里只做体检、不阻断：图片已经传成功了，不该因为权限配置把上传流程搞失败。
     * 真正的修复在 OSS 控制台，日志里直接写清楚怎么改。
     */
    private void checkAnonymousReadable(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .timeout(Duration.ofSeconds(3))
                    .build();
            int code = HTTP.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
            if (code >= 200 && code < 300) {
                return;
            }
            if (code == 403) {
                log.warn("""
                        图片已上传成功，但浏览器匿名访问被拒绝（403 AccessDenied）。
                          地址: {}
                          原因: Bucket [{}] 的读写权限是「私有」，页面上的 <img> 请求不带签名，所以显示不出来。
                          修复: OSS 控制台 → Bucket → 权限控制 → 读写权限 → 改为「公共读」；
                                如果开启了「阻止公共访问」，需要先把它关掉，否则改权限也不生效。
                          如果你确实要让 Bucket 保持私有，请改用「预签名 URL」方案（见 README）。
                        """, url, cfg.getBucket());
            } else {
                log.warn("图片已上传成功，但地址访问异常 HTTP {}，请检查 endpoint / 自定义域名配置: {}", code, url);
            }
        } catch (Exception e) {
            // 体检失败（断网、超时等）不影响主流程，记一条 debug 就够了
            log.debug("图片可访问性探测跳过: {}", url, e);
        }
    }

    /**
     * endpoint 允许用户填带协议的写法，这里统一去掉，SDK 只认纯域名
     */
    private String normalizeEndpoint(String endpoint) {
        if (endpoint == null) {
            return null;
        }
        return endpoint.trim()
                .replaceFirst("^https?://", "")
                .replaceAll("/+$", "");
    }

    private String buildUrl(String key) {
        if (cfg.getDomain() != null && !cfg.getDomain().isBlank()) {
            String domain = cfg.getDomain().trim().replaceAll("/+$", "");
            if (!domain.startsWith("http")) {
                domain = "https://" + domain;
            }
            return domain + "/" + key;
        }
        // 默认公网地址格式：https://bucket.endpoint/key
        return "https://" + cfg.getBucket() + "." + normalizeEndpoint(cfg.getEndpoint()) + "/" + key;
    }

    /**
     * 从访问地址反推出对象 key，用于删除。
     * 兼容「自定义域名」和「默认 bucket.域名」两种写法。
     */
    private String extractKey(String url) {
        String prefix = buildUrl("").replaceAll("/+$", "");
        if (url.startsWith(prefix + "/")) {
            return url.substring(prefix.length() + 1);
        }
        int idx = url.indexOf('/');
        if (url.startsWith("http") && idx > 0) {
            int hostEnd = url.indexOf('/', idx + 2);
            return hostEnd > 0 ? url.substring(hostEnd + 1) : null;
        }
        return null;
    }

    private String buildKey(String dir, String extension) {
        String baseDir = (cfg.getDir() == null || cfg.getDir().isBlank()) ? "" : cfg.getDir().replaceAll("/+$", "") + "/";
        String safeDir = (dir == null || dir.isBlank()) ? "common" : dir.replaceAll("[^a-zA-Z0-9_-]", "");
        return baseDir + safeDir + "/" + LocalDate.now().format(DATE_PATH) + "/"
                + UUID.randomUUID().toString().replace("-", "").substring(0, 16) + "." + extension;
    }

    private String guessContentType(String extension) {
        return switch (extension == null ? "" : extension.toLowerCase()) {
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            case "bmp" -> "image/bmp";
            default -> "image/jpeg";
        };
    }

    /** 只是为了让 properties 字段被 IDE 识别为使用中，实际配置读的是 cfg */
    @SuppressWarnings("unused")
    private StorageProperties getProperties() {
        return properties;
    }

}
