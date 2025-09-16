package org.campus.partworkback.exception;

public class BizException extends RuntimeException {
    private Integer code;

    public BizException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
