package com.ting.ting.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public class TingApplicationException extends RuntimeException {

    private ErrorCode errorCode;
    private ServiceType serviceType;
    private String message;

    public TingApplicationException(ErrorCode errorCode, ServiceType serviceType) {
        this.errorCode = errorCode;
        this.serviceType = serviceType;
    }

    public String getMessageForServer() {
        if (message == null) {
            message = errorCode.getMessage();
        }

        return String.format("[%s :: %s] : %s", errorCode, serviceType, message);
    }

    @Override
    public String getMessage() {
        if (message == null) {
            message = errorCode.getMessage();
        }

        return message;
    }

    public HttpStatus getHttpStatus() {
        return errorCode.getHttpStatus();
    }
}
