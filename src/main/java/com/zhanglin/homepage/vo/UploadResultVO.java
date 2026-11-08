package com.zhanglin.homepage.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 上传结果
 *
 * @author 张林
 */
@Data
@Builder
@Schema(description = "图片上传结果")
public class UploadResultVO {

    @Schema(description = "可访问地址", example = "https://bucket.oss-cn-hangzhou.aliyuncs.com/homepage/avatar/2026/11/08/ab12cd34.jpg")
    private String url;

    @Schema(description = "原始文件名", example = "avatar.jpg")
    private String name;

    @Schema(description = "文件大小（字节）")
    private Long size;

    @Schema(description = "图片类型", example = "jpg")
    private String extension;

    @Schema(description = "存储位置：local / aliyun")
    private String storage;

}
