package com.ting.ting.controller;

import com.ting.ting.dto.request.SendBlindRequest;
import com.ting.ting.dto.response.BlindUsersInfoResponse;
import com.ting.ting.dto.response.Response;
import com.ting.ting.exception.ServiceType;
import com.ting.ting.service.BlindRequestService;
import com.ting.ting.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
public class BlindDateControllerImpl extends AbstractController implements BlindDateController {

    private final UserService userService;
    private final BlindRequestService blindRequestService;

    public BlindDateControllerImpl(UserService userService, BlindRequestService blindRequestService) {
        super(ServiceType.BLIND);
        this.userService = userService;
        this.blindRequestService = blindRequestService;
    }

    @Override
    public Response<Page<BlindUsersInfoResponse>> blindUsersInfo(@ParameterObject Pageable pageable) {
        Long userId = 9L; // userId를 임의로 설정 TODO: user 구현 후 수정
        return success(userService.usersInfo(userId, pageable));
    }

    @Override
    public Response<Void> sendJoinRequest(@RequestBody SendBlindRequest request) {
        Long fromUserId = 9L;  // userId를 임의로 설정 TODO: user 구현 후 수정
        blindRequestService.createJoinRequest(fromUserId, request.getToUserId());
        return success();
    }

    @Override
    public Response<Void> deleteJoinRequest(@PathVariable long blindRequestId) {
        blindRequestService.deleteRequest(blindRequestId);
        return success();
    }

    @Override
    public Response<Void> acceptedRequest(@PathVariable long blindRequestId) {
        blindRequestService.acceptRequest(blindRequestId);
        return success();
    }

    @Override
    public Response<Void> rejectRequest(@PathVariable long blindRequestId) {
        blindRequestService.rejectRequest(blindRequestId);
        return success();
    }
}
