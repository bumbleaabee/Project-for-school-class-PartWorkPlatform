package org.campus.partworkback.exception;

import org.campus.partworkback.constant.HttpCode;
import org.campus.partworkback.pojo.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BizException.class)
    public Result bizException(BizException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(FileException.class)
    public Result fileException(FileException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    // 处理所有未捕获的异常
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception ex) {
        return Result.error(HttpCode.UNKNOWN_ERROR.getCode(), HttpCode.UNKNOWN_ERROR.getMessage());
    }
}
