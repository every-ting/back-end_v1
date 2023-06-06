package com.ting.ting.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class LogInResponse {

    private boolean isRegistered;
    private String token;

    public LogInResponse(boolean isRegistered) {
        this.isRegistered = isRegistered;
    }
}
