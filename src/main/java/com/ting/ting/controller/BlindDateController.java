package com.ting.ting.controller;

import com.ting.ting.dto.request.SendBlindRequest;
import com.ting.ting.dto.response.BlindUsersInfoResponse;
import com.ting.ting.exception.UserException;
import com.ting.ting.service.BlindRequestService;
import com.ting.ting.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/blind")
public class BlindDateController {

    private final UserService userService;
    private final BlindRequestService blindRequestService;

    public BlindDateController(UserService userService, BlindRequestService blindRequestService) {
        this.userService = userService;
        this.blindRequestService = blindRequestService;
    }

    @GetMapping("/users")
    public Page<BlindUsersInfoResponse> blindUsersInfo(Pageable pageable) {
        Long userId = 9L; // userId를 임의로 설정 TODO: user 구현 후 수정
        return userService.usersInfo(userId, pageable);
    }

    @PostMapping("/request")
    public ResponseEntity<String> sendJoinRequest(@RequestBody SendBlindRequest request) {
        Long fromUserId = 9L;  // userId를 임의로 설정 TODO: user 구현 후 수정
        blindRequestService.createJoinRequest(fromUserId, request.getToUserId());
        return ResponseEntity.ok("success");
    }

    @PutMapping("/request/accept/{blindRequestId}")
    public ResponseEntity<String> acceptedRequest(@PathVariable long blindRequestId) {
        blindRequestService.acceptRequest(blindRequestId);
        return ResponseEntity.ok("success");
    }

    @PutMapping("/request/reject/{blindRequestId}")
    public ResponseEntity<String> rejectRequest(@PathVariable long blindRequestId) {
        blindRequestService.rejectRequest(blindRequestId);
        return ResponseEntity.ok("success");
    }

    @DeleteMapping("/request/{blindRequestId}")
    public ResponseEntity<String> deleteJoinRequest(@PathVariable long blindRequestId) {
        blindRequestService.deleteRequest(blindRequestId);
        return ResponseEntity.ok("success");
    }

    @ExceptionHandler(UserException.class)
    public ResponseEntity<?> userExceptionHandler() {
        return ResponseEntity.badRequest().body("잘못된 정보를 입력하였습니다.");
    }
}
