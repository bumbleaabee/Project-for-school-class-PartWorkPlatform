package org.campus.partworkback.exception;

public class FileException extends RuntimeException {
    private Integer code;

    public FileException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
      return code;
    }
}
