package com.ting.ting.controller;

import com.ting.ting.dto.request.SignUpRequest;
import com.ting.ting.dto.response.LogInResponse;
import com.ting.ting.dto.response.Response;
import com.ting.ting.dto.response.SignUpResponse;
import com.ting.ting.exception.ServiceType;
import com.ting.ting.service.UserService;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/signUp")
    public Response<SignUpResponse> signUp(@RequestBody SignUpRequest request) {
        return success(userService.signUp(request));
    }
}
