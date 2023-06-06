package com.ting.ting.controller;

import com.ting.ting.dto.request.SignUpRequest;
import com.ting.ting.dto.response.LogInResponse;
import com.ting.ting.dto.response.Response;
import com.ting.ting.dto.response.SignUpResponse;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/ting")
public interface UserController {

    /**
     * 테스트 용
     */
    @GetMapping("/{userId}")
    Response<LogInResponse> logIn(@PathVariable Long userId);

    /**
     * 로그인 구현
     */
    @GetMapping("/logIn")
    Response<LogInResponse> logIn(@RequestParam String code);

    /**
     * 회원가입 구현
     */
    @PostMapping("/signUp")
    Response<SignUpResponse> signUp(@RequestBody SignUpRequest request);
}
