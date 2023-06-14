package com.ting.ting.controller;

import com.ting.ting.dto.request.SignUpRequest;
import com.ting.ting.dto.response.LogInResponse;
import com.ting.ting.dto.response.Response;
import com.ting.ting.dto.response.SignUpResponse;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

public interface UserController {

    /**
     * 테스트 용
     */
    @GetMapping("/ting/{userId}")
    Response<LogInResponse> logIn(@PathVariable Long userId);

    /**
     * 로그인 구현
     */
    @GetMapping("/ting/logIn")
    Response<LogInResponse> logIn(@RequestParam String code);

    /**
     * 회원가입 구현
     */
    @PostMapping("/ting/signUp")
    Response<SignUpResponse> signUp(@RequestBody SignUpRequest request);

    /**
     * 이상형 사진 업데이트
     */
    @PutMapping("/user/idealPhoto")
    Response<Void> updateIdealPhoto(@RequestParam @NotNull String idealPhoto);
}
