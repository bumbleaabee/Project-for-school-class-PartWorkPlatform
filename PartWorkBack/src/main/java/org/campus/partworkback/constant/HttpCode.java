package org.campus.partworkback.constant;

public enum HttpCode {
    SUCCESS(0, "成功"),
    PARAM_ERROR(1001, "参数错误"),
    UNAUTHORIZED(1002, "未登录/Token 失效"),
    FORBIDDEN(1003, "权限不足"),
    LOGIN_FAILED(2001, "登录失败"),
    TASK_NOT_FOUND(3001, "任务不存在"),
    TASK_STATUS_INVALID(3002, "任务状态不允许该操作"),
    REVIEW_CONFLICT(4001, "评价冲突"),
    UNKNOWN_ERROR(5001, "服务器错误");

    private final int code;           // 状态码数字
    private final String message;     // 状态消息

    private HttpCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
