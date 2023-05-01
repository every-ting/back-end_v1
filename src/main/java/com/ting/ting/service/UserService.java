package com.ting.ting.service;

import com.ting.ting.domain.constant.Gender;
import com.ting.ting.dto.response.BlindUsersInfoResponse;
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

    public Page<BlindUsersInfoResponse> womenUsersInfo(Pageable pageable) {
        return userRepository.findAllByGender(Gender.W, pageable).map(BlindUsersInfoResponse::from);
    }

    public Page<BlindUsersInfoResponse> menUsersInfo(Pageable pageable) {
        return userRepository.findAllByGender(Gender.M, pageable).map(BlindUsersInfoResponse::from);
    }
}
