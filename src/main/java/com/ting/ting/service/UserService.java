package com.ting.ting.service;

import com.ting.ting.domain.User;
import com.ting.ting.dto.request.SignUpRequest;
import com.ting.ting.dto.response.LogInResponse;
import com.ting.ting.dto.response.SignUpResponse;
import com.ting.ting.exception.ErrorCode;
import com.ting.ting.exception.ServiceType;
import com.ting.ting.repository.UserRepository;
import com.ting.ting.util.JwtTokenGenerator;
import com.ting.ting.util.KakaoInfoGenerator;
import org.springframework.stereotype.Component;

@Component
public class UserService extends AbstractService {

    private final UserRepository userRepository;
    private final KakaoInfoGenerator kakaoInfoGenerator;
    private final JwtTokenGenerator jwtTokenGenerator;

    public UserService(UserRepository userRepository, KakaoInfoGenerator kakaoInfoGenerator, JwtTokenGenerator jwtTokenGenerator) {
        super(ServiceType.USER);
        this.userRepository = userRepository;
        this.kakaoInfoGenerator = kakaoInfoGenerator;
        this.jwtTokenGenerator = jwtTokenGenerator;
    }

    public LogInResponse logIn(String code) {
        String socialEmail = getSocialEmailByCode(code);

        return userRepository.findBySocialEmail(socialEmail)
                .map(response -> new LogInResponse(true, createTokenBySocialEmail(socialEmail)))
                .orElse(new LogInResponse(false));
    }

    public SignUpResponse signUp(SignUpRequest request) {
        userRepository.findByUsername(request.getUsername()).ifPresent(username ->
                throwException(ErrorCode.DUPLICATE_USERNAME)
        );

        userRepository.findByEmail(request.getEmail()).ifPresent(email ->
                throwException(ErrorCode.DUPLICATE_EMAIL));

        User newUser = User.from(request);
        userRepository.save(newUser);

        return new SignUpResponse(request.getUsername(), createTokenById(newUser.getId()));
    }

    private String createTokenById(Long id) {
        return jwtTokenGenerator.createTokenById(id);
    }

    private String createTokenBySocialEmail(String socialEmail) {
        User user = getUserBySocialEmail(socialEmail);
        return jwtTokenGenerator.createTokenById(user.getId());
    }

    private String getSocialEmailByCode(String code) {
        String accessToken = kakaoInfoGenerator.getKakaoTokenResponse(code).getAccess_token();
        return kakaoInfoGenerator.getKakaoUserInfoResponse(accessToken).getKakao_account().getEmail();
    }

    private User getUserBySocialEmail(String socialEmail) {
        return userRepository.findBySocialEmail(socialEmail).orElseThrow(() ->
                throwException(ErrorCode.USER_NOT_FOUND, String.format("[%s]의 유저 정보가 존재하지 않습니다.", socialEmail)));
    }
}
