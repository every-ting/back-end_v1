package com.ting.ting.controller;

import com.ting.ting.dto.request.SendBlindRequest;
import com.ting.ting.dto.response.BlindUsersInfoResponse;
import com.ting.ting.exception.UserException;
import com.ting.ting.service.BlindRequestService;
import com.ting.ting.service.UserService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("users/m")
    public List<BlindUsersInfoResponse> blindMenUsersInfo(Pageable pageable) {
        return userService.menUsersInfo(pageable);
    }

    @GetMapping("users/w")
    public List<BlindUsersInfoResponse> blindWomenUsersInfo(Pageable pageable) {
        return userService.womenUsersInfo(pageable);
    }

    @PostMapping("/request")
    public ResponseEntity<String> sendBlindRequest(@RequestBody SendBlindRequest request) {
        blindRequestService.createJoinRequest(request.getFromUserId(), request.getToUserId());
        return ResponseEntity.ok("success");
    }

    @PutMapping("/request/{blindRequestId}")
    public ResponseEntity<String> rejectRequest(@PathVariable long blindRequestId) {
        blindRequestService.rejectRequest(blindRequestId);
        return ResponseEntity.ok("success");
    }

    @DeleteMapping("/request/{blindRequestId}")
    public ResponseEntity<String> deleteRequest(@PathVariable long blindRequestId) {
        blindRequestService.deleteRequest(blindRequestId);
        return ResponseEntity.ok("success");
    }

    @ExceptionHandler(UserException.class)
    public ResponseEntity<?> dbeHandler() {
        return ResponseEntity.badRequest().body("잘못된 정보를 입력하였습니다.");
    }
}
