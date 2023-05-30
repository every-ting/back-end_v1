package com.ting.ting.controller;

import com.ting.ting.dto.KakaoTokenResponse;
import com.ting.ting.dto.response.Response;
import com.ting.ting.exception.ServiceType;
import com.ting.ting.util.KakaoTokenGenerator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/ting")
@RestController
public class UserController extends AbstractController {

    private final KakaoTokenGenerator kakaoTokenGenerator;

    public UserController(KakaoTokenGenerator kakaoTokenGenerator) {
        super(ServiceType.USER);
        this.kakaoTokenGenerator = kakaoTokenGenerator;
    }

    @GetMapping("/oauth")
    public Response<KakaoTokenResponse> oauth(@RequestParam String code) {
        return success(kakaoTokenGenerator.getToken(code));
    }
}
