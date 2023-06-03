package com.ting.ting.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SignUpResponse {

    private String username;

    private String token;
}
