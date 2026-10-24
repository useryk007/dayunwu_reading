package org.example.common;

import lombok.Data;

/**
 * 统一的接口响应封装。
 * 所有 Controller 方法的返回值都用这个类包装，前端可以根据 code 判断请求是否成功。
 *
 * 字段约定：
 *   SUCCESS = 1  —— 业务成功（HTTP 状态码 200）
 *   FAIL    = 0  —— 业务失败（比如校验错误、业务异常）
 *   其他 code    —— 自定义状态，如 401 表示未登录
 *
 * message 是给人看的提示语，data 是返回的业务数据（泛型 T 支持任意类型）。
 *
 * 设计上提供了多组静态工厂方法 success() / error()，
 * 避免业务代码到处写 new Result<>(...)，也能统一默认 message。
 */
@Data
public class Result<T> {

    public static final int SUCCESS = 1;
    public static final int FAIL = 0;

    private Integer code;
    private String message;
    private T data;

    Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /** 成功，返回 data，默认 message 为 "success" */
    public static <T> Result<T> success(T data) {
        return new Result<>(SUCCESS, "success", data);
    }

    /** 成功，同时自定义 message 和 data */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(SUCCESS, message, data);
    }

    /** 失败，使用默认的 FAIL 状态码 */
    public static <T> Result<T> error(String message) {
        return new Result<>(FAIL, message, null);
    }

    /** 失败，自定义状态码（常用于 401 未登录等特殊场景） */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }
}