package com.ting.ting.exception;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ErrorCode {
    USER_NOT_FOUND("User is not founded"),
    DUPLICATED_USER_REQUEST("It's a request between the same users."),
    DUPLICATED_REQUEST("Request information that already exists."),
    INTERNAL_SERVER_ERROR("Internal server error"),
    REQUEST_NOT_FOUND("Request information is not founded");

    private final String message;

    public String getMessage() {
        return message;
    }
}