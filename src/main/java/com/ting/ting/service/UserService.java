package com.ting.ting.service;

import com.ting.ting.domain.User;
import com.ting.ting.dto.response.BlindUsersInfoResponse;
import com.ting.ting.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<BlindUsersInfoResponse> usersInfo() {
        List<User> users = userRepository.findAll();
        return users.stream().map(BlindUsersInfoResponse::new).collect(Collectors.toList());
    }
}
