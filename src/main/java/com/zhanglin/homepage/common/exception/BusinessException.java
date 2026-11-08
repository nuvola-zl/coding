package com.zhanglin.homepage.common.exception;

import com.zhanglin.homepage.common.ResultCode;
import lombok.Getter;

import java.io.Serial;

/**
 * 业务异常：Service 层校验不通过时抛出，由全局异常处理器统一转成 Result
 *
 * @author 张林
 */
@Getter
public class BusinessException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 业务状态码 */
    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.BUSINESS_ERROR.getCode();
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

}
