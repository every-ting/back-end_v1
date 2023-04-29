package com.ting.ting.controller;

import com.ting.ting.dto.request.SendBlindRequest;
import com.ting.ting.dto.response.BlindUsersInfoResponse;
import com.ting.ting.service.BlindRequestService;
import com.ting.ting.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/blind")
public class BlindDateController {

    private final UserService userService;
    private final BlindRequestService blindRequestService;

    public BlindDateController(UserService userService, BlindRequestService blindRequestService) {
        this.userService = userService;
        this.blindRequestService = blindRequestService;
    }

    @GetMapping("users")
    public List<BlindUsersInfoResponse> blindUsersInfo() {
        return userService.usersInfo();
    }

    @PostMapping("/request")
    public String sendBlindRequest(@RequestBody SendBlindRequest request) {
        blindRequestService.createJoinRequest(request.getFromUserId(), request.getToUserId());
        return "success";
    }
}
