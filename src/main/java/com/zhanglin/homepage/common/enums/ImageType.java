package com.zhanglin.homepage.common.enums;

/**
 * 允许上传的图片类型。
 *
 * <p>判断依据是<b>文件头魔数</b>，不是扩展名 —— 有人把 .jsp 改名成 .jpg 上传，
 * 只看后缀的话就漏过去了。魔术数字写在这里，一眼能看出每种格式的起始字节。
 *
 * @author 张林
 */
public enum ImageType {

    /** JPEG: FF D8 FF */
    JPEG("jpg", "image/jpeg", 0xFF, 0xD8, 0xFF),

    /** PNG: 89 50 4E 47 0D 0A 1A 0A */
    PNG("png", "image/png", 0x89, 0x50, 0x4E, 0x47),

    /** GIF: 47 49 46 38 ("GIF8") */
    GIF("gif", "image/gif", 0x47, 0x49, 0x46, 0x38),

    /** WEBP: 52 49 46 46 ("RIFF") ... 57 45 42 50 ("WEBP")，中间 4 字节是长度，单独处理 */
    WEBP("webp", "image/webp", 0x52, 0x49, 0x46, 0x46),

    /** BMP: 42 4D ("BM") */
    BMP("bmp", "image/bmp", 0x42, 0x4D);

    private final String extension;
    private final String contentType;
    private final int[] magic;

    ImageType(String extension, String contentType, int... magic) {
        this.extension = extension;
        this.contentType = contentType;
        this.magic = magic;
    }

    public String getExtension() {
        return extension;
    }

    public String getContentType() {
        return contentType;
    }

    /**
     * 根据文件头判断类型，认不出来返回 null
     */
    public static ImageType sniff(byte[] bytes) {
        for (ImageType type : values()) {
            if (matches(bytes, type.magic)) {
                // WEBP 还要额外校验第 8-11 字节是 "WEBP"，否则只是个普通 RIFF 文件
                if (type == WEBP && !matches(bytes, 8, 0x57, 0x45, 0x42, 0x50)) {
                    continue;
                }
                return type;
            }
        }
        return null;
    }

    private static boolean matches(byte[] bytes, int[] magic) {
        return matches(bytes, 0, magic);
    }

    private static boolean matches(byte[] bytes, int offset, int... magic) {
        if (bytes == null || bytes.length < offset + magic.length) {
            return false;
        }
        for (int i = 0; i < magic.length; i++) {
            if ((bytes[offset + i] & 0xFF) != magic[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * 人类可读的类型列表，用于错误提示
     */
    public static String supportedNames() {
        StringBuilder sb = new StringBuilder();
        for (ImageType type : values()) {
            if (!sb.isEmpty()) {
                sb.append("、");
            }
            sb.append(type.extension.toUpperCase());
        }
        return sb.toString();
    }

}
