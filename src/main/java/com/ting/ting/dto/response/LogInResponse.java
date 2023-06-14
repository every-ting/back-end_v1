package com.ting.ting.dto.response;

import com.ting.ting.domain.constant.Gender;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class LogInResponse {

    private boolean isRegistered;
    private String socialEmail;
    private Gender gender;
    private String token;

    public LogInResponse(boolean isRegistered, String socialEmail) {
        this.isRegistered = isRegistered;
        this.socialEmail = socialEmail;
    }
}
