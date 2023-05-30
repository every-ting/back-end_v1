package com.ting.ting.util;

import com.ting.ting.dto.KakaoTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class KakaoTokenGenerator {

    @Value("${kakao.client-id}")
    String clientId;

    @Value("${kakao.grant-type}")
    String grantType;

    @Value("${kakao.redirect-url}")
    String redirectUrl;

    @Value("${kakao.token-url}")
    String tokenUrl;

    public KakaoTokenResponse getToken(String code) {
        return WebClient.create().post()
                .uri("https://kauth.kakao.com/oauth/token")
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
}
