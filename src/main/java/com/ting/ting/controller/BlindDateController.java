package com.ting.ting.controller;

import com.ting.ting.dto.BlindUsersInfoResponse;
import com.ting.ting.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/blind")
public class BlindDateController {

    private final UserService userService;

    public BlindDateController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("users")
    public List<BlindUsersInfoResponse> blindUsersInfo() {
        return userService.usersInfo();
    }
}
