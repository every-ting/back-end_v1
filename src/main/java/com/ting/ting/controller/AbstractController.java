package com.ting.ting.controller;

import com.ting.ting.dto.response.Response;
import com.ting.ting.exception.ServiceType;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class AbstractController {

    protected ServiceType serviceType;

    protected <T> Response<T> success() {
        return Response.success(serviceType);
    }

    protected <T> Response<T> success(T data) {
        return Response.success(data, serviceType);
    }
}
