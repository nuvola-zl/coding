package com.zhanglin.homepage.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

/**
 * 文件存储配置，对应 application.yml 里的 app.storage.*
 *
 * <p>两种存储方式：
 * <ul>
 *   <li>{@code local}  —— 存在本机磁盘，适合本地开发（不需要任何账号）</li>
 *   <li>{@code aliyun} —— 存到阿里云 OSS，适合真正部署上线</li>
 * </ul>
 *
 * <p><b>怎么切？</b>看 {@link #resolveType()}：只要填了 accessKeyId 和 bucket 就走阿里云，
 * 没填就自动落到本地磁盘。所以换存储不需要改代码，改 yml 就行。
 *
 * @author 张林
 */
@Data
@ConfigurationProperties(prefix = "app.storage")
public class StorageProperties {

    /**
     * 强制指定存储类型：local / aliyun。
     * 留空则按 accessKeyId 是否填写自动判断。
     */
    private String type;

    private Local local = new Local();

    private Aliyun aliyun = new Aliyun();

    @Data
    public static class Local {

        /** 本地文件存放目录，相对路径以项目根目录为基准 */
        private String dir = "./uploads";

        /** 对外访问前缀，配合 WebMvcConfig 里的静态资源映射 */
        private String urlPrefix = "/uploads";
    }

    @Data
    public static class Aliyun {

        /** 地域节点，例如 oss-cn-hangzhou.aliyuncs.com */
        private String endpoint;

        /** 访问密钥 ID（RAM 用户，建议只授予该 Bucket 的读写权限） */
        private String accessKeyId;

        /** 访问密钥 Secret */
        private String accessKeySecret;

        /** Bucket 名称 */
        private String bucket;

        /** 上传到 Bucket 下的哪个目录，便于和别的文件区分 */
        private String dir = "homepage";

        /** 可选：绑定的自定义域名 / CDN 域名，填了就用它拼访问地址 */
        private String domain;
    }

    /**
     * 实际生效的存储类型。
     *
     * <p>显式配置 type 时以配置为准；否则看阿里云的三个必填项是否齐全。
     */
    public String resolveType() {
        if (StringUtils.hasText(type)) {
            return type.trim().toLowerCase();
        }
        return isAliyunReady() ? "aliyun" : "local";
    }

    public boolean isAliyunReady() {
        return StringUtils.hasText(aliyun.getEndpoint())
                && StringUtils.hasText(aliyun.getAccessKeyId())
                && StringUtils.hasText(aliyun.getAccessKeySecret())
                && StringUtils.hasText(aliyun.getBucket());
    }

}
