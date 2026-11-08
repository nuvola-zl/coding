package com.zhanglin.homepage.common;

import lombok.Getter;

/**
 * 统一响应状态码
 *
 * @author 张林
 */
@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数有误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "没有操作权限"),
    NOT_FOUND(404, "请求的资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方式不支持"),
    ERROR(500, "系统异常，请稍后重试"),
    BUSINESS_ERROR(600, "业务处理失败");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

}
