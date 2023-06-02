package com.ting.ting.controller;

import com.ting.ting.dto.kakao.KakaoLogOutResponse;
import com.ting.ting.dto.kakao.KakaoTokenResponse;
import com.ting.ting.dto.kakao.KakaoUserInfoResponse;
import com.ting.ting.dto.response.Response;
import com.ting.ting.exception.ServiceType;
import com.ting.ting.util.KakaoInfoGenerator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@RequestMapping("/kakao")
@RestController
public class KakaoController extends AbstractController {

    private final KakaoInfoGenerator kakaoInfoGenerator;

    public KakaoController(KakaoInfoGenerator kakaoInfoGenerator) {
        super(ServiceType.KAKAO);
        this.kakaoInfoGenerator = kakaoInfoGenerator;
    }

    @GetMapping("/oauth")
    public Response<String> oauth(@RequestParam String code) {
        return success(code);
    }

    @GetMapping("/token")
    public Response<KakaoTokenResponse> getToken(@RequestParam String code) {
        return success(kakaoInfoGenerator.getKakaoTokenResponse(code));
    }

    @GetMapping("/userInfo")
    public Response<KakaoUserInfoResponse> getUserInfo(@RequestParam String accessToken) {
        return success(kakaoInfoGenerator.getKakaoUserInfoResponse(accessToken));
    }

    @GetMapping("/logout")
    public Response<KakaoLogOutResponse> logOut(@RequestParam String accessToken, HttpSession session) {
        session.invalidate();
        return success(kakaoInfoGenerator.kakaoLogOut(accessToken));
    }
}
