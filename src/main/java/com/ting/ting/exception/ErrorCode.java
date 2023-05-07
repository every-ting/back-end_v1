package com.ting.ting.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User is not founded"),
    DUPLICATED_USER_REQUEST(HttpStatus.BAD_REQUEST, "It's a request between the same users."),
    DUPLICATED_REQUEST(HttpStatus.BAD_REQUEST, "Request information that already exists."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"),
    REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "Request information is not founded"),
    GENDER_NOT_MATCH(HttpStatus.FORBIDDEN, "gender values do not match"),
    REQUEST_NOT_MINE(HttpStatus.BAD_REQUEST,"This is not the request information that came to me");

    private final HttpStatus httpStatus;
    private final String message;

    public int getHttpStatus() {
        return httpStatus.value();
    }
}