package com.ting.ting.service;

import com.ting.ting.domain.User;
import com.ting.ting.domain.constant.Gender;
import com.ting.ting.dto.response.BlindUsersInfoResponse;
import com.ting.ting.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<BlindUsersInfoResponse> womenUsersInfo(Pageable pageable) {
        Page<User> users = userRepository.findAllByGender(Gender.W, pageable);
        return users.stream().map(BlindUsersInfoResponse::new).collect(Collectors.toList());
    }

    public List<BlindUsersInfoResponse> menUsersInfo(Pageable pageable) {
        Page<User> users = userRepository.findAllByGender(Gender.M, pageable);
        return users.stream().map(BlindUsersInfoResponse::new).collect(Collectors.toList());
    }
}
