package org.campus.partworkback.constant;

public enum Status {
    PUBLISHING(1, "发布中"),
    APPLYING(2, "申请中"),
    IN_PROGRESS(3, "进行中"),
    COMPLETED(5, "完成"),
    CANCELLED(6, "取消");


    private final int code;           // 状态码数字
    private final String message;     // 状态消息

    private Status(int code, String message) {
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
