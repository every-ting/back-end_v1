package com.ting.ting.service;

import com.ting.ting.domain.User;
import com.ting.ting.domain.constant.Gender;
import com.ting.ting.dto.response.BlindUsersInfoResponse;
import com.ting.ting.exception.UserException;
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

    public Page<BlindUsersInfoResponse> usersInfo(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserException("유저가 존재하지 않습니다."));
        if (user.getGender().equals(Gender.M)) {
            return womenUsersInfo(pageable);
        }
        return menUsersInfo(pageable);
    }

    private Page<BlindUsersInfoResponse> womenUsersInfo(Pageable pageable) {
        return userRepository.findAllByGender(Gender.W, pageable).map(BlindUsersInfoResponse::from);
    }

    private Page<BlindUsersInfoResponse> menUsersInfo(Pageable pageable) {
        return userRepository.findAllByGender(Gender.M, pageable).map(BlindUsersInfoResponse::from);
    }
}
