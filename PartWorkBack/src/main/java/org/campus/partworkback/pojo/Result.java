package org.campus.partworkback.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.campus.partworkback.constant.HttpCode;

@Data
@AllArgsConstructor
@NoArgsConstructor
// {code:number, message:string, data?:object}
public class Result {
    private Integer code;
    private String msg;
    private Object data;

    public static Result success() {
        Result result = new Result();
        result.code = HttpCode.SUCCESS.getCode();
        result.msg = "success";
        return result;
    }

    public static Result success(Object object) {
        Result result = new Result();
        result.data = object;
        result.code = HttpCode.SUCCESS.getCode();
        result.msg = "success";
        return result;
    }

    public static Result error(Integer c, String message) {
        Result result = new Result();
        result.msg = message;
        result.code = c;
        return result;
    }
}
