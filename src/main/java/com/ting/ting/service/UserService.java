package com.ting.ting.service;

import com.ting.ting.domain.User;
import com.ting.ting.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> usersInfo() {
        return userRepository.findAll();
    }
}
