package com.ting.ting.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class LogInResponse {

    private boolean isRegistered;
    private String socialEmail;
    private String token;

    public LogInResponse(boolean isRegistered, String socialEmail) {
        this.isRegistered = isRegistered;
        this.socialEmail = socialEmail;
    }
}
