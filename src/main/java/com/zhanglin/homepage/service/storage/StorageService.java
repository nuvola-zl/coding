package com.zhanglin.homepage.service.storage;

/**
 * 文件存储接口。
 *
 * <p>业务代码只依赖这个接口，不关心文件到底存在阿里云还是本机磁盘。
 * 想看具体实现：{@link com.zhanglin.homepage.service.storage.impl.AliyunOssStorageService}
 * 和 {@link com.zhanglin.homepage.service.storage.impl.LocalStorageService}。
 *
 * @author 张林
 */
public interface StorageService {

    /**
     * 上传一个文件，返回可直接放进 img src 的访问地址。
     *
     * @param bytes      文件内容
     * @param extension  小写扩展名，不含点，例如 jpg
     * @param dir        业务子目录，例如 avatar / gallery
     * @return 访问地址（阿里云是完整 https URL，本地是 /uploads/xxx）
     */
    String upload(byte[] bytes, String extension, String dir);

    /**
     * 删除文件。传的是 {@link #upload} 返回的地址。
     *
     * <p>删除失败只记日志不抛异常 —— 换头像时旧图删不掉不该让整个请求失败。
     */
    void deleteQuietly(String url);

    /**
     * 存储类型标识：local / aliyun，给页面和接口回显用
     */
    String type();

}
