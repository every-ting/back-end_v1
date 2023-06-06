package com.ting.ting.controller;

import com.ting.ting.dto.request.SignUpRequest;
import com.ting.ting.dto.response.LogInResponse;
import com.ting.ting.dto.response.Response;
import com.ting.ting.dto.response.SignUpResponse;
import com.ting.ting.exception.ServiceType;
import com.ting.ting.service.UserService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/ting")
@RestController
public class UserControllerImpl extends AbstractController implements UserController {

    private final UserService userService;

    public UserControllerImpl(UserService userService) {
        super(ServiceType.USER);
        this.userService = userService;
    }

    public Response<LogInResponse> logIn(String code) {
        return success(userService.logIn(code));
    }

    public Response<SignUpResponse> signUp(SignUpRequest request) {
        return success(userService.signUp(request));
    }
}
