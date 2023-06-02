package com.ting.ting.controller;

import com.ting.ting.dto.response.LogInResponse;
import com.ting.ting.dto.response.Response;
import com.ting.ting.exception.ServiceType;
import com.ting.ting.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/ting")
@RestController
public class UserController extends AbstractController {

    private final UserService userService;

    public UserController(UserService userService) {
        super(ServiceType.USER);
        this.userService = userService;
    }

    @GetMapping("/logIn")
    public Response<LogInResponse> logIn(@RequestParam String code) {
        return success(userService.logIn(code));
    }
}
