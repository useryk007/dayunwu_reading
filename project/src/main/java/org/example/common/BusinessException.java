package org.example.common;

import lombok.Getter;

/**
 * 业务异常。
 *
 * 继承 RuntimeException（非受检异常），Service 层在可预期的业务校验失败时主动抛出，
 * 由 GlobalExceptionHandler 统一捕获并转换为 Result.error() 返回给前端。
 *
 * 这比在 Controller 里逐个 if 校验、手写 Result.error() 要清爽：
 *   1. Service 可以专注业务逻辑，校验失败直接 throw
 *   2. Controller 方法签名上不用声明 throws，代码更干净
 *   3. 异常处理集中在一处，前端拿到的错误格式永远一致
 *
 * code 默认 0（业务失败），如需自定义状态码可自行扩展构造器。
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 业务错误码，默认 0；HTTP 状态码语义由 GlobalExceptionHandler 决定 */
    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = 0;
    }
}