package com.ting.ting.dto.response;

import com.ting.ting.exception.ErrorCode;
import com.ting.ting.exception.ServiceType;
import com.ting.ting.exception.TingApplicationException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@AllArgsConstructor
public class ResultObject {

    private int resultCode;
    private String serviceType;
    private String message;

    public ResultObject(ErrorCode errorCode) {
        this.resultCode = errorCode.getHttpStatus();
        this.message = errorCode.getMessage();
    }

    public ResultObject(ErrorCode errorCode, ServiceType serviceType) {
        this.resultCode = errorCode.getHttpStatus();
        this.serviceType = serviceType.name();
        this.message = errorCode.getMessage();
    }

    public ResultObject(TingApplicationException e) {
        this.resultCode = e.getErrorCode().getHttpStatus();
        this.serviceType = e.getServiceType().name();
        this.message = e.getMessage();
    }

    public static ResultObject success(ServiceType serviceType) {
        return new ResultObject(HttpStatus.OK.value(), serviceType.name(), "success");
    }

    public String toStream() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"resultCode\":").append(resultCode).append(",");
        sb.append("\"serviceType\":").append("\"").append(serviceType).append("\",");
        sb.append("\"message\":").append("\"").append(message).append("\"");
        sb.append("}");
        return sb.toString();
    }
}
