package com.ting.ting.dto.response;

import com.ting.ting.domain.constant.Gender;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SignUpResponse {

    private String username;

    private Gender gender;

    private String token;
}
