package com.ting.ting.controller;

import com.ting.ting.dto.response.Response;
import com.ting.ting.exception.ServiceType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/ting")
@RestController
public class UserController extends AbstractController {

    public UserController() {
        super(ServiceType.USER);
    }

    @GetMapping("/oauth")
    public Response<Void> oauth(@RequestParam String code) {
        System.out.println(code);
        return success();
    }
}
