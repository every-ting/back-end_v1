package com.ting.ting.service;

import com.ting.ting.dto.response.LogInResponse;
import com.ting.ting.repository.UserRepository;
import com.ting.ting.util.KakaoInfoGenerator;
import org.springframework.stereotype.Component;

@Component
public class UserService {

    private final UserRepository userRepository;
    private final KakaoInfoGenerator kakaoInfoGenerator;

    public UserService(UserRepository userRepository, KakaoInfoGenerator kakaoInfoGenerator) {
        this.userRepository = userRepository;
        this.kakaoInfoGenerator = kakaoInfoGenerator;
    }

    public LogInResponse logIn(String code) {
        String socialEmail = getSocialEmailByCode(code);

        return userRepository.findBySocialEmail(socialEmail)
                .map(response -> new LogInResponse(true))
                .orElse(new LogInResponse(false));
    }

    public String getSocialEmailByCode(String code) {
        String accessToken = kakaoInfoGenerator.getKakaoTokenResponse(code).getAccess_token();
        return kakaoInfoGenerator.getKakaoUserInfoResponse(accessToken).getKakao_account().getEmail();
    }
}
