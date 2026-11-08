package com.zhanglin.homepage.service.impl;

import com.zhanglin.homepage.common.ResultCode;
import com.zhanglin.homepage.common.enums.ImageType;
import com.zhanglin.homepage.common.exception.BusinessException;
import com.zhanglin.homepage.service.UploadService;
import com.zhanglin.homepage.service.storage.StorageService;
import com.zhanglin.homepage.vo.UploadResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 上传业务实现：校验 → 交给存储层 → 返回地址
 *
 * @author 张林
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UploadServiceImpl implements UploadService {

    private final StorageService storageService;

    @Override
    public UploadResultVO uploadImage(MultipartFile file, String dir) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "请选择要上传的图片");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new BusinessException(ResultCode.BAD_REQUEST,
                    "图片不能超过 " + (MAX_IMAGE_SIZE / 1024 / 1024) + "MB");
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            log.error("读取上传文件失败", e);
            throw new BusinessException(ResultCode.ERROR.getCode(), "图片读取失败，请重试");
        }

        // 核心校验：看文件头，不看后缀名
        ImageType type = ImageType.sniff(bytes);
        if (type == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST,
                    "只支持 " + ImageType.supportedNames() + " 格式的图片");
        }

        String url = storageService.upload(bytes, type.getExtension(), dir);
        log.info("图片上传成功 storage={} size={}B url={}", storageService.type(), bytes.length, url);

        return UploadResultVO.builder()
                .url(url)
                .name(file.getOriginalFilename())
                .size(file.getSize())
                .extension(type.getExtension())
                .storage(storageService.type())
                .build();
    }

    @Override
    public String storageType() {
        return storageService.type();
    }

}
