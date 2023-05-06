package com.ting.ting.controller;

import com.ting.ting.dto.request.SendBlindRequest;
import com.ting.ting.dto.response.BlindUsersInfoResponse;
import com.ting.ting.dto.response.Response;
import com.ting.ting.service.BlindRequestService;
import com.ting.ting.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/blind")
public class BlindDateController {

    private final UserService userService;
    private final BlindRequestService blindRequestService;

    public BlindDateController(UserService userService, BlindRequestService blindRequestService) {
        this.userService = userService;
        this.blindRequestService = blindRequestService;
    }

    /**
     * 소개팅 상대편 조회(자신의 성별에 따라 조회 결과가 다름)
     */
    @GetMapping("/users")
    public Response<Page<BlindUsersInfoResponse>> blindUsersInfo(@ParameterObject Pageable pageable) {
        Long userId = 9L; // userId를 임의로 설정 TODO: user 구현 후 수정
        return Response.success(userService.usersInfo(userId, pageable));
    }

    /**
     * 소개팅 상대에게 요청
     */
    @PostMapping("/request")
    public Response<Void> sendJoinRequest(@RequestBody SendBlindRequest request) {
        Long fromUserId = 9L;  // userId를 임의로 설정 TODO: user 구현 후 수정
        blindRequestService.createJoinRequest(fromUserId, request.getToUserId());
        return Response.success();
    }

    /**
     * 소개팅 상대에게 한 요청 취소
     */
    @DeleteMapping("/request/{blindRequestId}")
    public Response<Void> deleteJoinRequest(@PathVariable long blindRequestId) {
        blindRequestService.deleteRequest(blindRequestId);
        return Response.success();
    }

    /**
     * 자신에게 온 요청 수락 -> 추가 구현 필요
     */
    @PutMapping("/request/accept/{blindRequestId}")
    public Response<Void> acceptedRequest(@PathVariable long blindRequestId) {
        blindRequestService.acceptRequest(blindRequestId);
        return Response.success();
    }

    /**
     * 자신에게 온 요청 거절 -> 추가 구현 필요
     */
    @PutMapping("/request/reject/{blindRequestId}")
    public Response<Void> rejectRequest(@PathVariable long blindRequestId) {
        blindRequestService.rejectRequest(blindRequestId);
        return Response.success();
    }
}
