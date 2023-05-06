package com.ting.ting.service;

import com.ting.ting.domain.User;
import com.ting.ting.domain.constant.Gender;
import com.ting.ting.dto.response.BlindUsersInfoResponse;
import com.ting.ting.exception.ErrorCode;
import com.ting.ting.exception.TingApplicationException;
import com.ting.ting.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 소개팅 상대편 조회(자신의 성별에 따라 조회 결과가 다름)
     */
    public Page<BlindUsersInfoResponse> usersInfo(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new TingApplicationException(ErrorCode.USER_NOT_FOUND, ServiceType.BLIND, String.format("[%d]의 유저 정보가 존재하지 않습니다.", userId)));

        if (user.getGender().equals(Gender.M)) {
            return womenUsersInfo(pageable);
        }
        
        return menUsersInfo(pageable);
    }

    /**
     * 소개팅 상대편 조회 - 자신이 남자일 경우
     */
    private Page<BlindUsersInfoResponse> womenUsersInfo(Pageable pageable) {
        return userRepository.findAllByGender(Gender.WOMEN, pageable).map(BlindUsersInfoResponse::from);
    }

    /**
     * 소개팅 상대편 조회 - 자신이 여자일 경우가
     */
    private Page<BlindUsersInfoResponse> menUsersInfo(Pageable pageable) {
        return userRepository.findAllByGender(Gender.MEN, pageable).map(BlindUsersInfoResponse::from);
    }
}
