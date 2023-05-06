package com.ting.ting.exception;

import com.ting.ting.service.ServiceType;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public class TingApplicationException extends RuntimeException {

    private ErrorCode errorCode;
    private ServiceType serviceType;
    private String message;

    public TingApplicationException(ErrorCode errorCode, ServiceType serviceType) {
        this.errorCode = errorCode;
        this.serviceType = serviceType;
    }

    @Override
    public String getMessage() {
        if (message == null) {
            return errorCode.getMessage();
        }

        return String.format("[%s :: %s] : %s", errorCode, serviceType, message);
    }

    public HttpStatus getHttpStatus() {
        return errorCode.getHttpStatus();
    }
}
