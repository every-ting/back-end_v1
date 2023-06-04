package com.ting.ting.util;

import com.ting.ting.dto.kakao.KakaoLogOutResponse;
import com.ting.ting.dto.kakao.KakaoTokenResponse;
import com.ting.ting.dto.kakao.KakaoUserInfoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class KakaoManger {

    @Value("${kakao.client-id}")
    String clientId;

    @Value("${kakao.grant-type}")
    String grantType;

    @Value("${kakao.redirect-url}")
    String redirectUrl;

    @Value("${kakao.token-url}")
    String tokenUrl;

    @Value("${kakao.user-info-url}")
    String userInfoUrl;

    @Value("${kakao.logout-url}")
    String logOutURL;

    private final WebClient webClient = WebClient.create();

    public KakaoTokenResponse getKakaoTokenResponse(String code) {
        return webClient.post()
                .uri(tokenUrl)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .body(BodyInserters
                        .fromFormData("grant_type", grantType)
                        .with("client_id", clientId)
                        .with("redirect_uri", redirectUrl)
                        .with("code", code)
                )
                .retrieve()
                .bodyToFlux(KakaoTokenResponse.class)
                .blockFirst();
    }

    public KakaoUserInfoResponse getKakaoUserInfoResponse(String accessToken) {
        return webClient.get()
                .uri(userInfoUrl)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToFlux(KakaoUserInfoResponse.class)
                .blockFirst();
    }

    public KakaoLogOutResponse kakaoLogOut(String accessToken) {
        return webClient.post()
                .uri(logOutURL)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToFlux(KakaoLogOutResponse.class)
                .blockFirst();
    }
}
