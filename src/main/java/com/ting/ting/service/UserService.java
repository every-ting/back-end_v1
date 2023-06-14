package com.ting.ting.service;

import com.ting.ting.dto.UserDto;
import com.ting.ting.dto.request.SignUpRequest;
import com.ting.ting.dto.response.LogInResponse;
import com.ting.ting.dto.response.SignUpResponse;

public interface UserService {

    /**
     * user 조회
     */
    UserDto getUserDtoById(Long userId);

    /**
     * 로그인 로직
     */
    LogInResponse logIn(String code);

    /**
     * 테스트용
     */
    LogInResponse logInTest(Long userId);

    /**
     * 회원가입 로직
     */
    SignUpResponse signUp(SignUpRequest request);

    /**
     * 이상형 사진 업데이트 로직
     */
    void updateIdealPhoto(String idealPhoto);
}
