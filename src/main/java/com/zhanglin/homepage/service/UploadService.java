package com.zhanglin.homepage.service;

import com.zhanglin.homepage.vo.UploadResultVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 上传业务
 *
 * @author 张林
 */
public interface UploadService {

    /** 单张图片大小上限：5MB */
    long MAX_IMAGE_SIZE = 5 * 1024 * 1024L;

    /**
     * 校验并上传一张图片
     *
     * @param file 上传的文件
     * @param dir  业务子目录，如 avatar / gallery
     */
    UploadResultVO uploadImage(MultipartFile file, String dir);

    /** 当前使用的存储类型：local / aliyun */
    String storageType();

}
