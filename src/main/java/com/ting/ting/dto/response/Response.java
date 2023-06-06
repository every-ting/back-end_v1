package com.ting.ting.dto.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ting.ting.exception.ErrorCode;
import com.ting.ting.exception.ServiceType;
import com.ting.ting.exception.TingApplicationException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Response<T> {

    private final ResultObject result;
    private T data;

    public Response(ResultObject result) {
        this.result = result;
    }

    public Response(TingApplicationException e) {
        this.result = new ResultObject(e);
    }

    public static <T> Response<T> success(ServiceType serviceType) {
        return new Response<>(ResultObject.success(serviceType));
    }

    public static <T> Response<T> success(T data, ServiceType serviceType) {
        return new Response<>(ResultObject.success(serviceType), data);
    }

    public static <T> Response<T> error(ErrorCode errorCode) {
        return new Response<>(new ResultObject(errorCode));
    }

    public String toStream() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "";
        }
    }
}
