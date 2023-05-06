package com.ting.ting.dto.response;

import com.ting.ting.exception.ErrorCode;
import com.ting.ting.exception.TingApplicationException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Response<T> {

    private ResultObject result;
    private T data;

    public Response(ResultObject result) {
        this.result = result;
    }

    public static <T> Response<T> success() {
        return new Response<>(ResultObject.success());
    }

    public static <T> Response<T> success(T data) {
        return new Response<>(ResultObject.success(), data);
    }

    public static <T> Response<T> error(ErrorCode errorCode) {
        return new Response<>(new ResultObject(errorCode));
    }

    public Response(TingApplicationException e) {
        this.result = new ResultObject(e);
    }
}
