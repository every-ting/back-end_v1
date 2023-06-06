package com.ting.ting.service;

import com.ting.ting.dto.UserDto;
import com.ting.ting.dto.request.SignUpRequest;
import com.ting.ting.dto.response.LogInResponse;
import com.ting.ting.dto.response.SignUpResponse;

public interface UserService {

    /**
     * user 조회
     */
    UserDto getUserById(Long userId);

    /**
     * 로그인 로직 구현
     */
    LogInResponse logIn(String code);

    /**
     * 테스트용
     */
    LogInResponse logInTest(Long userId);

    /**
     * 회원가입 로직 구현
     */
    SignUpResponse signUp(SignUpRequest request);
}
