package com.ting.ting.service;

import com.ting.ting.domain.User;
import com.ting.ting.dto.UserDto;
import com.ting.ting.dto.request.SignUpRequest;
import com.ting.ting.dto.response.LogInResponse;
import com.ting.ting.dto.response.SignUpResponse;
import com.ting.ting.exception.ErrorCode;
import com.ting.ting.exception.ServiceType;
import com.ting.ting.repository.UserRepository;
import com.ting.ting.util.JwtTokenUtil;
import com.ting.ting.util.KakaoManger;
import org.springframework.stereotype.Component;

@Component
public class UserServiceImpl extends AbstractService implements UserService {

    private final UserRepository userRepository;
    private final KakaoManger kakaoManger;
    private final JwtTokenUtil jwtTokenUtil;

    public UserServiceImpl(UserRepository userRepository, KakaoManger kakaoManger, JwtTokenUtil jwtTokenUtil) {
        super(ServiceType.USER);
        this.userRepository = userRepository;
        this.kakaoManger = kakaoManger;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    public UserDto getUserById(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                throwException(ErrorCode.REQUEST_NOT_FOUND, String.format("User(id: %d) not found", userId))
        );

        return UserDto.from(user);
    }

    @Override
    public LogInResponse logInTest(Long userId) {
        return new LogInResponse(true, jwtTokenUtil.createTokenById(userId));
    }

    @Override
    public LogInResponse logIn(String code) {
        String socialEmail = getSocialEmailByCode(code);

        return userRepository.findBySocialEmail(socialEmail)
                .map(response -> {
                    User user = getUserBySocialEmail(socialEmail);
                    return new LogInResponse(true, jwtTokenUtil.createTokenById(user.getId()));
                })
                .orElse(new LogInResponse(false, socialEmail));
    }

    @Override
    public SignUpResponse signUp(SignUpRequest request) {
        userRepository.findByUsername(request.getUsername()).ifPresent(username ->
                throwException(ErrorCode.DUPLICATE_USERNAME)
        );

        userRepository.findByEmail(request.getEmail()).ifPresent(email ->
                throwException(ErrorCode.DUPLICATE_EMAIL));

        User newUser = User.from(request);
        userRepository.save(newUser);

        return new SignUpResponse(request.getUsername(), jwtTokenUtil.createTokenById(newUser.getId()));
    }

    private String getSocialEmailByCode(String code) {
        String accessToken = kakaoManger.getKakaoTokenResponse(code).getAccess_token();
        return kakaoManger.getKakaoUserInfoResponse(accessToken).getKakao_account().getEmail();
    }

    private User getUserBySocialEmail(String socialEmail) {
        return userRepository.findBySocialEmail(socialEmail).orElseThrow(() ->
                throwException(ErrorCode.USER_NOT_FOUND, String.format("[%s]의 유저 정보가 존재하지 않습니다.", socialEmail)));
    }
}
